package com.plexo.android.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.plexo.android.ui.theme.*
import java.net.URLEncoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlexoBrowserScreen(
    viewModel: DownloadViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    var urlInput by remember { mutableStateOf("https://www.google.com") }
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var pageTitle by remember { mutableStateOf("Google") }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val externalNavigateUrl by viewModel.browserNavigateUrl.collectAsState()

    // Handle external navigation requests
    LaunchedEffect(externalNavigateUrl) {
        externalNavigateUrl?.let { url ->
            webViewInstance?.loadUrl(url)
            urlInput = url
            viewModel.browserNavigateUrl.value = null
        }
    }

    // Handle Android system back gesture/button
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            viewModel.selectTab(PlexoTab.DOWNLOADS)
        }
    }

    fun navigateTo(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        val targetUrl = if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else if (trimmed.contains(".") && !trimmed.contains(" ")) {
            "https://$trimmed"
        } else {
            "https://www.google.com/search?q=${URLEncoder.encode(trimmed, "UTF-8")}"
        }

        urlInput = targetUrl
        webViewInstance?.loadUrl(targetUrl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VitalityBgTop,
                        VitalityBgMid,
                        VitalityBgBottom
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // Browser Top Controls in Frosted Sage Style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VitalityGlassCardDark)
                .border(1.dp, VitalityGlassBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                // Address Bar & Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (canGoBack) VitalityMint else SageTextMuted
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (canGoForward) VitalityMint else SageTextMuted
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = SageTextPrimary
                        )
                    }

                    // URL Input Box
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(horizontal = 4.dp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Search or type URL",
                                color = SageTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { navigateTo(urlInput) }),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = { urlInput = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = SageTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitalityMint,
                            unfocusedBorderColor = VitalityGlassBorder,
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedContainerColor = VitalityGlassCard,
                            unfocusedContainerColor = VitalityGlassCard
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = { navigateTo("https://www.google.com") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = VitalityOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Bookmark Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val bookmarks = listOf(
                        "Google" to "https://www.google.com",
                        "GitHub" to "https://github.com",
                        "MediaFire" to "https://www.mediafire.com",
                        "Archive.org" to "https://archive.org",
                        "APKMirror" to "https://www.apkmirror.com",
                        "Fast.com" to "https://fast.com"
                    )

                    bookmarks.forEach { (name, url) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(VitalityGlassCardLight)
                                .border(1.dp, VitalityGlassBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            TextButton(
                                onClick = { navigateTo(url) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(20.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SageTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Webpage Loading Progress Bar
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = VitalityMint,
                trackColor = VitalityDarkNav
            )
        }

        // Active Interception Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VitalityDarkNav)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = VitalityOrangeGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Auto-Download Sniffer Active • Multi-Link Bond Ready",
                    fontSize = 11.sp,
                    color = SageTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(
                onClick = {
                    if (currentUrl.isNotBlank() && currentUrl != "https://www.google.com") {
                        viewModel.onUrlReceived(currentUrl, source = "Browser Active URL")
                    } else {
                        Toast.makeText(context, "No active file URL to download", Toast.LENGTH_SHORT).show()
                    }
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Capture URL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalityMint
                )
            }
        }

        // Embedded Android WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = settings.userAgentString + " PlexoAndroid/1.0"
                    }

                    // Automatic Download Sniffer Listener
                    setDownloadListener { url, _, _, _, _ ->
                        Toast.makeText(
                            ctx,
                            "⚡ Plexo Intercepted Download!",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.onUrlReceived(url, source = "Plexo Built-In Browser")
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            url?.let {
                                currentUrl = it
                                urlInput = it
                            }
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            url?.let {
                                currentUrl = it
                                urlInput = it
                            }
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val target = request?.url?.toString() ?: return false
                            if (DownloadViewModel.isDownloadableUrl(target)) {
                                Toast.makeText(
                                    ctx,
                                    "⚡ Plexo Intercepted Direct File!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.onUrlReceived(target, source = "Plexo Link Sniffer")
                                return true
                            }
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progress = newProgress / 100f
                            isLoading = newProgress < 100
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            title?.let { pageTitle = it }
                        }
                    }

                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
