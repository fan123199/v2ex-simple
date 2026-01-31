package im.fdx.v2ex.ui.common

import im.fdx.v2ex.data.model.Data
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import im.fdx.v2ex.data.network.HttpHelper
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("正在加载中") }
    var progress by remember { mutableFloatStateOf(0f) }
    var webView: WebView? by remember { mutableStateOf(null) }

    // Handle back button
    BackHandler(enabled = webView != null && webView!!.canGoBack()) {
        webView?.goBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // TODO: Replace with native Compose WebView when available or use a third-party library
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            setSupportZoom(true)
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                            userAgentString = userAgentString.replace(Regex("(?<=\\W)wv"), "")
                            javaScriptEnabled = true
                            domStorageEnabled = true 
                        }
                        
                        // Setup Cookie persistence login logic
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                val cookie = CookieManager.getInstance().getCookie(url)
                                if (cookie?.contains("A2=") == true && url != null) {
                                    persistCookies(url, cookie)
                                    onLoginSuccess()
                                }
                                super.onPageStarted(view, url, favicon)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView, newTitle: String?) {
                                super.onReceivedTitle(view, newTitle)
                                if (!newTitle.isNullOrEmpty()) {
                                    title = newTitle
                                }
                            }

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                if (newProgress < 100) {
                                    if (view?.url == "https://www.v2ex.com/#") {
                                        title = "正在登录中..."
                                    }
                                }
                            }
                        }

                        if (url.isNotEmpty()) {
                            loadUrl(url)
                        }
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (progress < 1f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                )
            }
        }
    }
}

private fun persistCookies(url: String, cookieStr: String) {
    val url2 = url.toHttpUrl()
    val strs = cookieStr.split(";")
    val cookies = mutableListOf<Cookie>()
    strs.forEach {
        Cookie.parse(url2, it.trim())?.let { it1 -> cookies.add(it1) }
    }
    HttpHelper.cookiePersistor.persistAll(cookies)
}




