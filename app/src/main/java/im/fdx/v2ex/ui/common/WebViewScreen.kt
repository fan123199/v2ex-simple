package im.fdx.v2ex.ui.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import im.fdx.v2ex.R
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.ui.login.hasWebLoginSessionCookie
import im.fdx.v2ex.ui.login.isTrustedWebLoginUrl
import im.fdx.v2ex.ui.login.verifyWebLogin
import im.fdx.v2ex.utils.UserStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONTokener

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    googleSignIn: Boolean = false,
) {
    var title by remember { mutableStateOf<String?>(null) }
    var currentUrl by remember { mutableStateOf(url) }
    var progress by remember { mutableFloatStateOf(0f) }
    var webView: WebView? by remember { mutableStateOf(null) }
    var pageGeneration by remember { mutableIntStateOf(0) }
    var pageFailed by remember { mutableStateOf(false) }
    var checkingLogin by remember { mutableStateOf(false) }
    var loginCheckFailed by remember { mutableStateOf(false) }
    var loginCompleted by remember { mutableStateOf(false) }
    var googleSignInStarted by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    var loginCheckJob: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val latestOnBackClick by rememberUpdatedState(onBackClick)
    val latestOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    fun openGoogleSignIn(view: WebView, pageUrl: String) {
        if (!googleSignIn || googleSignInStarted || !isTrustedWebLoginUrl(pageUrl) ||
            pageUrl.toHttpUrlOrNull()?.encodedPath != "/signin"
        ) return
        val generation = pageGeneration
        view.evaluateJavascript(GOOGLE_SIGN_IN_LINK_SCRIPT) { value ->
            if (closing || loginCompleted || googleSignInStarted || generation != pageGeneration || view.url != pageUrl) {
                return@evaluateJavascript
            }
            val candidate = runCatching { JSONTokener(value).nextValue() as? String }.getOrNull()
            val source = pageUrl.toHttpUrlOrNull()
            val target = candidate?.toHttpUrlOrNull()
            if (source != null && target != null && isTrustedWebLoginUrl(target.toString()) &&
                target.host == source.host && target.port == source.port && target.encodedPath == "/auth/google"
            ) {
                googleSignInStarted = true
                view.loadUrl(target.toString())
            }
        }
    }

    fun checkLogin(view: WebView, pageUrl: String) {
        if (closing || loginCompleted || checkingLogin || !isTrustedWebLoginUrl(pageUrl)) return
        val cookies = CookieManager.getInstance().getCookie("$HTTPS_V2EX_BASE/").orEmpty()
        if (!hasWebLoginSessionCookie(cookies)) return

        val generation = pageGeneration
        checkingLogin = true
        loginCheckFailed = false
        loginCheckJob = scope.launch {
            try {
                val result = verifyWebLogin(pageUrl, cookies)
                if (result != null && !closing && !loginCompleted && generation == pageGeneration &&
                    webView === view && view.url == pageUrl
                ) {
                    HttpHelper.myCookieJar.clear()
                    HttpHelper.cookiePersistor.persistAll(result.cookies)
                    UserStore.updateUser(result.member)
                    loginCompleted = true
                    latestOnLoginSuccess()
                } else if (result == null && !closing && generation == pageGeneration && view.url == pageUrl) {
                    when (pageUrl.toHttpUrlOrNull()?.encodedPath?.trimEnd('/')) {
                        "/signin" -> openGoogleSignIn(view, pageUrl)
                        "/2fa" -> Unit // Let the user finish verification in the website.
                        else -> loginCheckFailed = true
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (!closing && generation == pageGeneration) loginCheckFailed = true
            } finally {
                if (generation == pageGeneration) checkingLogin = false
            }
        }
    }

    fun closePage() {
        if (closing) return
        closing = true
        loginCheckJob?.cancel()
        webView?.stopLoading()
        latestOnBackClick()
    }

    BackHandler {
        val view = webView
        if (!closing && view?.canGoBack() == true) view.goBack() else closePage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title ?: stringResource(if (googleSignIn) R.string.google_login else R.string.web_loading),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = currentUrl.toHttpUrlOrNull()?.host.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = ::closePage) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.login_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (checkingLogin) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.web_checking_login),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else if (progress < 1f && !pageFailed) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
            if (pageFailed || loginCheckFailed) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(if (pageFailed) R.string.web_load_failed else R.string.web_login_check_failed),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            webView?.let { view ->
                                if (pageFailed) {
                                    pageFailed = false
                                    view.reload()
                                } else {
                                    view.url?.let { checkLogin(view, it) }
                                }
                            }
                        }) {
                            Text(stringResource(R.string.web_retry))
                        }
                    }
                }
            }
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        settings.apply {
                            setSupportZoom(true)
                            // Preserve the existing browser configuration during the Compose migration.
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                            userAgentString = userAgentString.replace(Regex("(?<=\\W)wv"), "")
                            javaScriptEnabled = true
                            domStorageEnabled = true
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, pageUrl: String, favicon: Bitmap?) {
                                pageGeneration++
                                loginCheckJob?.cancel()
                                checkingLogin = false
                                loginCheckFailed = false
                                pageFailed = false
                                currentUrl = pageUrl
                                title = null
                                progress = 0f
                            }

                            override fun onPageFinished(view: WebView, pageUrl: String) {
                                if (closing || pageFailed || view.url != pageUrl) return
                                progress = 1f
                                checkLogin(view, pageUrl)
                                if (!checkingLogin) openGoogleSignIn(view, pageUrl)
                            }

                            override fun onPageCommitVisible(view: WebView, pageUrl: String) {
                                if (!closing && !pageFailed && view.url == pageUrl) {
                                    checkLogin(view, pageUrl)
                                }
                            }

                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame && !closing) pageFailed = true
                            }

                            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                                if (request.isForMainFrame && !closing) pageFailed = true
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView, newTitle: String?) {
                                if (!closing && !newTitle.isNullOrBlank()) title = newTitle
                            }

                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                if (!closing) progress = newProgress / 100f
                            }
                        }
                        webView = this
                        loadUrl(url)
                    }
                },
                onRelease = { view ->
                    closing = true
                    loginCheckJob?.cancel()
                    view.stopLoading()
                    view.webViewClient = WebViewClient()
                    view.webChromeClient = null
                    view.removeAllViews()
                    view.destroy()
                    if (webView === view) webView = null
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}

// Read the official page's link so the nonce belongs to this WebView session.
private val GOOGLE_SIGN_IN_LINK_SCRIPT = """
    (function() {
        var elements = document.querySelectorAll('.sign_in_with, a[href]');
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var target = element.getAttribute('href');
            if (!target) {
                var match = (element.getAttribute('onclick') || '').match(/["']([^"']*\/auth\/google(?:\?[^"']*)?)["']/);
                target = match ? match[1] : null;
            }
            if (target) {
                try {
                    var link = new URL(target, window.location.href);
                    if (link.origin === window.location.origin && link.pathname === '/auth/google') return link.href;
                } catch (ignored) {}
            }
        }
        return null;
    })();
""".trimIndent()
