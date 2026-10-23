package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.ads.AdaptiveBannerAdView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import com.example.model.InAppBrowserState
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusUrgent
import java.net.URLEncoder

/**
 * High-performance In-App WebView Browser for Sarkari Job Alert candidates.
 * Replaces direct external browser redirection to keep candidates inside the app
 * while reviewing official recruitment portals, notifications, admit cards, and results.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowserScreen(
    state: InAppBrowserState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Prepare normalized URL & Google Docs viewer for PDF links
    val initialRawUrl = remember(state.url) {
        val trimmed = state.url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    val isPdfDocument = remember(initialRawUrl) {
        initialRawUrl.endsWith(".pdf", ignoreCase = true) ||
            initialRawUrl.contains(".pdf?", ignoreCase = true) ||
            initialRawUrl.contains("/pdf/", ignoreCase = true)
    }

    val finalLoadUrl = remember(initialRawUrl, isPdfDocument) {
        if (isPdfDocument && !initialRawUrl.contains("docs.google.com/viewer")) {
            try {
                "https://docs.google.com/viewer?embedded=true&url=" + URLEncoder.encode(initialRawUrl, "UTF-8")
            } catch (_: Exception) {
                initialRawUrl
            }
        } else {
            initialRawUrl
        }
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(finalLoadUrl) }
    var pageTitle by remember { mutableStateOf(state.title ?: "Government Portal") }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorDescription by remember { mutableStateOf<String?>(null) }
    var showSlowServerOption by remember { mutableStateOf(false) }

    // If government server is slow or sluggish, show quick external browser option after 5 seconds
    LaunchedEffect(isLoading, finalLoadUrl) {
        if (isLoading) {
            showSlowServerOption = false
            delay(5000L)
            if (isLoading) {
                showSlowServerOption = true
            }
        } else {
            showSlowServerOption = false
        }
    }

    // Domain display and SSL status
    val parsedUri = runCatching { Uri.parse(currentUrl) }.getOrNull()
    val hostDomain = parsedUri?.host ?: "Official Portal"
    val isHttps = currentUrl.startsWith("https://")

    // Handle physical back button: walk through web history first, or close browser
    BackHandler {
        val wv = webViewInstance
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onClose()
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(top = statusBarPadding, bottom = navBarPadding)
    ) {
        // Top Browser Navigation & Control Header
        Surface(
            color = PureWhite,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Close Action Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("browser_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Browser",
                            tint = Slate800,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Page Title and Secure Domain Indicator
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = pageTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Icon(
                                imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isHttps) "Secure connection" else "Not secure",
                                tint = if (isHttps) EmeraldGreen else StatusUrgent,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = hostDomain,
                                fontSize = 11.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isPdfDocument) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFEDD5),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PDF Notice",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFC2410C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Web Navigation Actions: Back / Forward / Refresh / Share / External
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // History Back
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = canGoBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browser_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) Slate800 else Slate200,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // History Forward
                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = canGoForward,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browser_forward_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) Slate800 else Slate200,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reload or Stop
                        IconButton(
                            onClick = {
                                if (isLoading) {
                                    webViewInstance?.stopLoading()
                                    isLoading = false
                                } else {
                                    hasError = false
                                    webViewInstance?.reload()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browser_reload_btn")
                        ) {
                            Icon(
                                imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = if (isLoading) "Stop Loading" else "Reload Page",
                                tint = Slate800,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Share Link
                        IconButton(
                            onClick = {
                                shareUrl(context, currentUrl, pageTitle)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browser_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Link",
                                tint = Slate800,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Open in Chrome / External Browser Fallback
                        IconButton(
                            onClick = {
                                openInExternalBrowser(context, initialRawUrl)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browser_external_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open in Chrome",
                                tint = RoyalBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Smooth Linear Loading Progress Bar
                AnimatedVisibility(
                    visible = isLoading && progress in 0.01f..0.99f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = RoyalBlue,
                        trackColor = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                    )
                }
            }
        }

        // PDF Document Quick Helper Strip
        if (isPdfDocument) {
            Surface(
                color = Color(0xFFFEF3C7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = StatusUrgent,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Official PDF Document Preview",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                    }
                    Text(
                        text = "Open in PDF App / Chrome",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { openInExternalBrowser(context, initialRawUrl) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Web Content Viewport & Error Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(PureWhite)
        ) {
            key(finalLoadUrl) {
                AndroidView<WebView>(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Prevent black screen flash / compositing void
                            setBackgroundColor(android.graphics.Color.WHITE)
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)

                            // Enable modern cookie policies for session-based government application forms
                            try {
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)
                            } catch (_: Exception) {}

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                allowFileAccess = false
                                allowContentAccess = true
                                loadsImagesAutomatically = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                // Prevent dark mode inversion from turning government pages into unreadable black surfaces
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    @Suppress("DEPRECATION")
                                    forceDark = WebSettings.FORCE_DARK_OFF
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    isAlgorithmicDarkeningAllowed = false
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val reqUrl = request?.url?.toString() ?: return false
                                    if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                        return false // Let WebView load government URLs
                                    }

                                    // External protocols (mailto:, tel:, whatsapp:)
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        ctx.startActivity(intent)
                                        true
                                    } catch (_: Exception) {
                                        true
                                    }
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    hasError = false
                                    url?.let { currentUrl = it }
                                }

                                // Instant visible commit dismisses loading overlay early without black screen waiting
                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    isLoading = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                    url?.let { currentUrl = it }
                                }

                                @SuppressLint("WebViewClientOnReceivedSslError")
                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    // Government portals (*.gov.in, *.nic.in) often use CCA India certificates
                                    // that trigger standard SSL warnings. Proceed to avoid a blank black screen.
                                    handler?.proceed()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        hasError = true
                                        errorDescription = error?.description?.toString()
                                    }
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    // Prevent whole app crash if Android WebView renderer memory limit is reached
                                    return true
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress / 100f
                                    if (newProgress >= 70) {
                                        isLoading = false
                                    }
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                    if (!title.isNullOrBlank() && !title.startsWith("http")) {
                                        pageTitle = title
                                    }
                                }
                            }

                            loadUrl(finalLoadUrl)
                            webViewInstance = this
                        }
                    },
                    update = { wv ->
                        webViewInstance = wv
                    },
                    onRelease = { wv ->
                        try {
                            wv.stopLoading()
                            wv.destroy()
                        } catch (_: Throwable) {}
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("in_app_webview")
                )
            }

            // Smooth Pure White Loading Screen Overlay with Slow Server Warning
            if (isLoading && !hasError) {
                Surface(
                    color = PureWhite,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = RoyalBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Loading Official Portal...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Connecting to government recruitment server",
                            fontSize = 12.sp,
                            color = Slate500
                        )

                        if (showSlowServerOption) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Government server taking longer than usual?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate700,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { openInExternalBrowser(context, initialRawUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Open Directly in Chrome",
                                            fontSize = 12.sp,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // High-touch Error / Server Down Fallback
            if (hasError) {
                Surface(
                    color = PureWhite,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = StatusUrgent,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Portal Currently Unavailable",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = errorDescription ?: "Government recruitment servers frequently experience high traffic during application deadlines.",
                            fontSize = 13.sp,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewInstance?.reload()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Connection")
                            }

                            Button(
                                onClick = {
                                    openInExternalBrowser(context, initialRawUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in Chrome", color = PureWhite)
                            }
                        }
                    }
                }
            }
        }

        // Sticky AdMob Adaptive Banner at the bottom of the In-App Browser
        AdaptiveBannerAdView()
    }
}

private fun shareUrl(context: Context, url: String, title: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "$title\n$url\n\nShared via Sarkari Job Alert")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Official Link")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
    }
}

private fun openInExternalBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not launch external browser", Toast.LENGTH_SHORT).show()
    }
}
