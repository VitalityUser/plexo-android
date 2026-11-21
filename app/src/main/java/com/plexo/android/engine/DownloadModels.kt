package com.plexo.android.engine

import com.plexo.android.network.NetworkInterfaceType
import java.util.Locale

enum class ChunkStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class DownloadChunk(
    val id: Int,
    val startByte: Long,
    val endByte: Long,
    val downloadedBytes: Long = 0L,
    val status: ChunkStatus = ChunkStatus.PENDING,
    val assignedInterface: NetworkInterfaceType = NetworkInterfaceType.DEFAULT
) {
    val totalChunkBytes: Long get() = (endByte - startByte) + 1
    val progress: Float
        get() = if (totalChunkBytes > 0) {
            (downloadedBytes.toFloat() / totalChunkBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

enum class DownloadStatus {
    IDLE,
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class DownloadTask(
    val id: String,
    val url: String,
    val fileName: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val totalSpeedBps: Long = 0L,
    val wifiSpeedBps: Long = 0L,
    val cellularSpeedBps: Long = 0L,
    val ethernetSpeedBps: Long = 0L,
    val bluetoothSpeedBps: Long = 0L,
    val chunks: List<DownloadChunk> = emptyList(),
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (totalBytes > 0) {
            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val formattedTotalSpeed: String
        get() = formatSpeed(totalSpeedBps)

    val formattedWifiSpeed: String
        get() = formatSpeed(wifiSpeedBps)

    val formattedCellularSpeed: String
        get() = formatSpeed(cellularSpeedBps)

    val formattedEthernetSpeed: String
        get() = formatSpeed(ethernetSpeedBps)

    val formattedBluetoothSpeed: String
        get() = formatSpeed(bluetoothSpeedBps)

    val formattedSize: String
        get() {
            if (totalBytes <= 0) return "Unknown size"
            return "${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}"
        }

    companion object {
        fun formatSpeed(bytesPerSecond: Long): String {
            val mbps = bytesPerSecond.toDouble() / (1024 * 1024)
            return if (mbps >= 1.0) {
                String.format(Locale.US, "%.1f MB/s", mbps)
            } else {
                val kbps = bytesPerSecond.toDouble() / 1024
                String.format(Locale.US, "%.0f KB/s", kbps)
            }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes.toDouble() / 1024
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024
            if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
            val gb = mb / 1024
            return String.format(Locale.US, "%.2f GB", gb)
        }
    }
}
