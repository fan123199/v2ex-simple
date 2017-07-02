package im.fdx.v2ex.ui


import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import im.fdx.v2ex.R


@Deprecated("暂时不用，等以后实现")
class WebViewActivity : AppCompatActivity() {

    private var myWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        var url: String? = "http://www.jd.com"
        if (intent.extras != null) {
            url = intent.getStringExtra("url")
        }



        myWebView = findViewById(R.id.webview)
        val chromeClient = myChromeClient()
        myWebView!!.setWebChromeClient(chromeClient)
        val webViewClient = myWebViewClient()
        myWebView!!.setWebViewClient(webViewClient)
        val webSettings = myWebView!!.settings

        if (url != null) {
            myWebView!!.loadUrl(url)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView!!.canGoBack()) {
            myWebView!!.goBack()
        }

        return super.onKeyUp(keyCode, event)
    }

    private inner class myWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.url.toString())
                return true
            }
            return false

        }

    }


    private inner class myChromeClient : WebChromeClient() {

        override fun onReceivedTitle(view: WebView, title: String) {
            (findViewById<TextView>(R.id.title) as TextView).text = title
            super.onReceivedTitle(view, title)
        }
    }
}

