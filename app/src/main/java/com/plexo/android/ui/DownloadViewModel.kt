package com.plexo.android.ui

import android.app.Application
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plexo.android.engine.DownloadStatus
import com.plexo.android.engine.DownloadTask
import com.plexo.android.engine.MultiNetworkDownloader
import com.plexo.android.network.MultiNetworkManager
import com.plexo.android.network.NetworkBondingState
import com.plexo.android.service.DownloadForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class PlexoTab {
    DOWNLOADS,
    BROWSER,
    STATISTICS,
    PROFILE
}

data class DetectedDownload(
    val url: String,
    val source: String,
    val fileName: String = DownloadViewModel.extractFileNameFromUrl(url),
    val timestamp: Long = System.currentTimeMillis()
)

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    val networkManager = MultiNetworkManager(application)
    val downloader = MultiNetworkDownloader(application, networkManager)

    val networkState: StateFlow<NetworkBondingState> = networkManager.networkState
    val currentTask: StateFlow<DownloadTask?> = downloader.taskState

    private val _currentTab = MutableStateFlow(PlexoTab.DOWNLOADS)
    val currentTab: StateFlow<PlexoTab> = _currentTab.asStateFlow()

    private val _detectedDownload = MutableStateFlow<DetectedDownload?>(null)
    val detectedDownload: StateFlow<DetectedDownload?> = _detectedDownload.asStateFlow()

    val autoStartDownloads = MutableStateFlow(true)

    val browserNavigateUrl = MutableStateFlow<String?>(null)

    private var lastHandledUrl: String? = null
    private var lastClipboardText: String? = null

    init {
        networkManager.startListening()

        // Sync state with foreground service
        viewModelScope.launch {
            currentTask.collectLatest { task ->
                if (task != null && task.status == DownloadStatus.DOWNLOADING) {
                    val progressPercent = (task.progress * 100).toInt()
                    DownloadForegroundService.start(
                        getApplication(),
                        task.fileName,
                        progressPercent,
                        task.formattedTotalSpeed
                    )
                } else if (task?.status == DownloadStatus.COMPLETED || task?.status == DownloadStatus.ERROR || task == null) {
                    DownloadForegroundService.stop(getApplication())
                }
            }
        }
    }

    fun selectTab(tab: PlexoTab) {
        _currentTab.value = tab
    }

    fun openInBrowser(url: String) {
        browserNavigateUrl.value = url
        _currentTab.value = PlexoTab.BROWSER
    }

    fun onUrlReceived(rawUrl: String, source: String, forcePrompt: Boolean = false) {
        val cleanUrl = extractCleanUrl(rawUrl) ?: return
        if (cleanUrl == lastHandledUrl && currentTask.value?.status == DownloadStatus.DOWNLOADING) {
            return
        }
        lastHandledUrl = cleanUrl

        val detection = DetectedDownload(cleanUrl, source)

        if (autoStartDownloads.value && !forcePrompt) {
            _detectedDownload.value = null
            startDownload(cleanUrl)
            _currentTab.value = PlexoTab.DOWNLOADS
            Toast.makeText(
                getApplication(),
                "⚡ Plexo: Starting download from $source",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            _detectedDownload.value = detection
            _currentTab.value = PlexoTab.DOWNLOADS
        }
    }

    fun acceptDetectedDownload(segmentCount: Int = 4) {
        val detected = _detectedDownload.value ?: return
        _detectedDownload.value = null
        startDownload(detected.url, segmentCount)
        _currentTab.value = PlexoTab.DOWNLOADS
    }

    fun dismissDetectedDownload() {
        _detectedDownload.value = null
    }

    fun checkClipboard(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return
            if (!clipboard.hasPrimaryClip()) return

            val description = clipboard.primaryClipDescription
            if (description != null && (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {
                val item = clipboard.primaryClip?.getItemAt(0) ?: return
                val text = item.text?.toString()?.trim() ?: return

                if (text.isNotBlank() && text != lastClipboardText) {
                    lastClipboardText = text
                    val detected = extractCleanUrl(text)
                    if (detected != null && isDownloadableUrl(detected)) {
                        onUrlReceived(detected, "Clipboard (Copied Link)", forcePrompt = true)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("DownloadViewModel", "Clipboard check failed", e)
        }
    }

    fun startDownload(url: String, segmentCount: Int = 4) {
        if (url.isNotBlank()) {
            downloader.startDownload(url.trim(), segmentCount)
        }
    }

    fun pauseDownload() {
        downloader.pauseDownload()
    }

    fun cancelDownload() {
        downloader.cancelDownload()
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.stopListening()
    }

    companion object {
        fun extractCleanUrl(text: String): String? {
            val urlRegex = Regex("""(https?://[^\s<>"{}|\\^`]+)""", RegexOption.IGNORE_CASE)
            val match = urlRegex.find(text)
            return match?.value?.trim()
        }

        fun isDownloadableUrl(url: String): Boolean {
            val lower = url.lowercase()
            val commonExtensions = listOf(
                ".apk", ".zip", ".rar", ".7z", ".tar", ".gz", ".iso", ".bin",
                ".exe", ".dmg", ".mp4", ".mkv", ".mp3", ".pdf", ".mov", ".flac"
            )
            return commonExtensions.any { lower.contains(it) } || lower.contains("download")
        }

        fun extractFileNameFromUrl(url: String): String {
            return try {
                val uri = Uri.parse(url)
                val segment = uri.lastPathSegment
                if (!segment.isNullOrBlank()) segment else "download"
            } catch (e: Exception) {
                "download"
            }
        }
    }
}
