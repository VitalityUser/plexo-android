package com.plexo.android.engine

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.plexo.android.network.MultiNetworkManager
import com.plexo.android.network.NetworkInterfaceType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class MultiNetworkDownloader(
    private val context: Context,
    private val networkManager: MultiNetworkManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _taskState = MutableStateFlow<DownloadTask?>(null)
    val taskState: StateFlow<DownloadTask?> = _taskState.asStateFlow()

    private var activeJob: Job? = null
    private var speedJob: Job? = null
    private var outputFile: File? = null
    private var fileChannel: FileChannel? = null

    // Real-time speed calculation counters
    private val wifiBytesSample = AtomicLong(0)
    private val cellularBytesSample = AtomicLong(0)

    fun startDownload(url: String, segmentCount: Int = 4) {
        activeJob?.cancel()
        speedJob?.cancel()

        val taskId = UUID.randomUUID().toString()
        val rawFileName = extractFileName(url)

        val initialTask = DownloadTask(
            id = taskId,
            url = url,
            fileName = rawFileName,
            status = DownloadStatus.CONNECTING
        )
        _taskState.value = initialTask

        activeJob = scope.launch {
            try {
                executeDownload(url, segmentCount)
            } catch (e: CancellationException) {
                Log.d(TAG, "Download job cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                _taskState.update { it?.copy(status = DownloadStatus.ERROR, errorMessage = e.message) }
            } finally {
                closeFile()
            }
        }

        startSpeedMonitor()
    }

    private suspend fun executeDownload(url: String, segmentCount: Int) {
        val defaultClient = networkManager.getClientForType(NetworkInterfaceType.DEFAULT)

        // 1. Inspect server with HEAD request
        val headRequest = Request.Builder().url(url).head().build()
        var contentLength: Long = -1L
        var acceptsRanges = false
        var resolvedFileName = _taskState.value?.fileName ?: "download"

        try {
            defaultClient.newCall(headRequest).execute().use { response ->
                if (response.isSuccessful) {
                    contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
                    val acceptRangesHeader = response.header("Accept-Ranges")
                    acceptsRanges = acceptRangesHeader?.contains("bytes", ignoreCase = true) == true

                    val disposition = response.header("Content-Disposition")
                    if (!disposition.isNullOrBlank() && disposition.contains("filename=")) {
                        val parsed = disposition.substringAfter("filename=").replace("\"", "").trim()
                        if (parsed.isNotEmpty()) resolvedFileName = parsed
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "HEAD request failed, trying GET fallback", e)
        }

        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val targetFile = File(destDir, resolvedFileName)
        outputFile = targetFile

        val raf = RandomAccessFile(targetFile, "rw")
        if (contentLength > 0) {
            raf.setLength(contentLength)
        }
        fileChannel = raf.channel

        // If range requests are supported and size is known, perform multi-interface chunking
        if (contentLength > 0 && (acceptsRanges || segmentCount > 1)) {
            val chunks = createChunks(contentLength, segmentCount)
            _taskState.update {
                it?.copy(
                    fileName = resolvedFileName,
                    totalBytes = contentLength,
                    status = DownloadStatus.DOWNLOADING,
                    chunks = chunks
                )
            }

            // Launch parallel chunk download jobs
            coroutineScope {
                chunks.map { chunk ->
                    launch {
                        downloadChunk(url, chunk)
                    }
                }.joinAll()
            }

            _taskState.update {
                it?.copy(
                    status = DownloadStatus.COMPLETED,
                    downloadedBytes = contentLength,
                    totalSpeedBps = 0,
                    wifiSpeedBps = 0,
                    cellularSpeedBps = 0
                )
            }
        } else {
            // Fallback: single stream download
            downloadSingleStream(url, contentLength, resolvedFileName)
        }
    }

    private fun createChunks(totalBytes: Long, segments: Int): List<DownloadChunk> {
        val hasBonding = networkManager.hasBothNetworks()
        val chunkSize = totalBytes / segments
        val chunks = mutableListOf<DownloadChunk>()

        for (i in 0 until segments) {
            val start = i * chunkSize
            val end = if (i == segments - 1) totalBytes - 1 else (start + chunkSize - 1)
            val assignedInterface = when {
                !hasBonding -> NetworkInterfaceType.DEFAULT
                i % 2 == 0 -> NetworkInterfaceType.WIFI
                else -> NetworkInterfaceType.CELLULAR
            }
            chunks.add(
                DownloadChunk(
                    id = i,
                    startByte = start,
                    endByte = end,
                    assignedInterface = assignedInterface
                )
            )
        }
        return chunks
    }

    private suspend fun downloadChunk(url: String, chunk: DownloadChunk) = withContext(Dispatchers.IO) {
        val client = networkManager.getClientForType(chunk.assignedInterface)
        val request = Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=${chunk.startByte + chunk.downloadedBytes}-${chunk.endByte}")
            .build()

        updateChunkStatus(chunk.id, ChunkStatus.DOWNLOADING)

        try {
            val response: Response = client.newCall(request).execute()
            if (!response.isSuccessful && response.code != 206) {
                throw Exception("HTTP ${response.code} on chunk ${chunk.id}")
            }

            val body = response.body ?: throw Exception("Empty body for chunk ${chunk.id}")
            val inputStream = body.byteStream()
            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var currentWritePosition = chunk.startByte + chunk.downloadedBytes

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                ensureActive()

                val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                fileChannel?.write(byteBuffer, currentWritePosition)
                currentWritePosition += bytesRead

                when (chunk.assignedInterface) {
                    NetworkInterfaceType.WIFI -> wifiBytesSample.addAndGet(bytesRead.toLong())
                    NetworkInterfaceType.CELLULAR -> cellularBytesSample.addAndGet(bytesRead.toLong())
                    NetworkInterfaceType.DEFAULT -> wifiBytesSample.addAndGet(bytesRead.toLong())
                }

                updateChunkProgress(chunk.id, bytesRead.toLong())
            }

            updateChunkStatus(chunk.id, ChunkStatus.COMPLETED)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.e(TAG, "Chunk ${chunk.id} failed: ${e.message}")
                updateChunkStatus(chunk.id, ChunkStatus.FAILED)
                throw e
            }
        }
    }

    private suspend fun downloadSingleStream(url: String, totalBytes: Long, fileName: String) = withContext(Dispatchers.IO) {
        val client = networkManager.getClientForType(NetworkInterfaceType.DEFAULT)
        val request = Request.Builder().url(url).build()

        _taskState.update {
            it?.copy(
                fileName = fileName,
                totalBytes = totalBytes,
                status = DownloadStatus.DOWNLOADING
            )
        }

        val response = client.newCall(request).execute()
        val body = response.body ?: throw Exception("Empty response body")
        val inputStream = body.byteStream()
        val buffer = ByteArray(32 * 1024)
        var bytesRead: Int
        var totalRead = 0L

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            ensureActive()
            val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
            fileChannel?.write(byteBuffer, totalRead)
            totalRead += bytesRead
            wifiBytesSample.addAndGet(bytesRead.toLong())

            _taskState.update { current ->
                current?.copy(downloadedBytes = totalRead)
            }
        }

        _taskState.update {
            it?.copy(
                status = DownloadStatus.COMPLETED,
                downloadedBytes = totalRead,
                totalBytes = if (totalBytes <= 0) totalRead else totalBytes
            )
        }
    }

    private fun updateChunkStatus(chunkId: Int, status: ChunkStatus) {
        _taskState.update { current ->
            if (current == null) return@update null
            val updatedChunks = current.chunks.map {
                if (it.id == chunkId) it.copy(status = status) else it
            }
            current.copy(chunks = updatedChunks)
        }
    }

    private fun updateChunkProgress(chunkId: Int, newlyReadBytes: Long) {
        _taskState.update { current ->
            if (current == null) return@update null
            var totalDownloaded = 0L
            val updatedChunks = current.chunks.map { chunk ->
                if (chunk.id == chunkId) {
                    val updatedBytes = chunk.downloadedBytes + newlyReadBytes
                    totalDownloaded += updatedBytes
                    chunk.copy(downloadedBytes = updatedBytes)
                } else {
                    totalDownloaded += chunk.downloadedBytes
                    chunk
                }
            }
            current.copy(
                downloadedBytes = totalDownloaded,
                chunks = updatedChunks
            )
        }
    }

    private fun startSpeedMonitor() {
        speedJob = scope.launch {
            while (isActive) {
                delay(1000)
                val wifiDelta = wifiBytesSample.getAndSet(0)
                val cellularDelta = cellularBytesSample.getAndSet(0)
                val totalDelta = wifiDelta + cellularDelta

                _taskState.update { current ->
                    if (current?.status == DownloadStatus.DOWNLOADING) {
                        current.copy(
                            totalSpeedBps = totalDelta,
                            wifiSpeedBps = wifiDelta,
                            cellularSpeedBps = cellularDelta
                        )
                    } else current
                }
            }
        }
    }

    fun pauseDownload() {
        activeJob?.cancel()
        _taskState.update { it?.copy(status = DownloadStatus.PAUSED, totalSpeedBps = 0, wifiSpeedBps = 0, cellularSpeedBps = 0) }
        closeFile()
    }

    fun cancelDownload() {
        activeJob?.cancel()
        speedJob?.cancel()
        closeFile()
        outputFile?.delete()
        _taskState.value = null
    }

    private fun closeFile() {
        try {
            fileChannel?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing file channel", e)
        }
        fileChannel = null
    }

    private fun extractFileName(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val path = uri.lastPathSegment
            if (!path.isNullOrBlank()) path else "download_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }

    companion object {
        private const val TAG = "MultiNetworkDownloader"
    }
}
