package im.fdx.v2ex.ui


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.utils.extensions.logi
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.synthetic.main.activity_web_view.*
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl


@Suppress("UNUSED_VARIABLE")
class WebViewActivity : BaseActivity() {

    private lateinit var myWebView: WebView

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private var noFirst: Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        toolbar = setUpToolbar("")
        var url = ""
        if (intent.extras != null) {
            url = intent.getStringExtra("url")?:""
        }

        logi("loadUrl: $url")
        WebView.setWebContentsDebuggingEnabled(true)

        myWebView = findViewById(R.id.webview)
        val webSettings = myWebView.settings
        webSettings.setSupportZoom(true)
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012)" +
                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Mobile Safari/537.36"
        webSettings.javaScriptEnabled  = true
        val chromeClient = MyChromeClient()
        myWebView.webChromeClient = chromeClient
        val webViewClient = MyWebViewClient()
        myWebView.webViewClient = webViewClient


        if (url.isNotEmpty()) {
            myWebView.loadUrl(url)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
        }

        return super.onKeyUp(keyCode, event)
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return false
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            val cookie = CookieManager.getInstance().getCookie(url)
            Log.i("fdxcookie",url + "----"+  cookie)
            if(cookie.contains("A2=") && url !=null) {
                // "abc:efg;"
                HttpHelper.cookiePersistor.persistAll(strtocookie(url, cookie))
                setResult(Activity.RESULT_OK)
                setLogin(true)
                finish()
            }
            noFirst = true
            super.onPageFinished(view, url)
        }

    }


    private inner class MyChromeClient : WebChromeClient() {

        override fun onReceivedTitle(view: WebView, title: String) {
            toolbar.title = title
            super.onReceivedTitle(view, title)
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if(newProgress < 100) {
                if(view?.url.equals("https://www.v2ex.com/#")) {
                    webview.visibility = View.INVISIBLE
                    val progressDialog = ProgressDialog(this@WebViewActivity)
                    progressDialog.setMessage("正在登录中，请稍后")
                    progressDialog.show()
                }
            } else{

            }
        }

    }


    fun strtocookie(url: String , cookieStr: String) : MutableList<Cookie> {
        val url2 = url.toHttpUrl()
        val strs = cookieStr.split(";")
        val cookies = mutableListOf<Cookie>()
        strs.forEach {
            Cookie.parse(url2, it)?.let { it1 -> cookies.add(it1) }
        }

        return cookies
    }

}

