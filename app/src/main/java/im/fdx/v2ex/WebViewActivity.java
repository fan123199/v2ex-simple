package im.fdx.v2ex;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import static android.R.attr.key;
import static im.fdx.v2ex.view.GoodTextView.REQUEST_CODE;

public class WebViewActivity extends AppCompatActivity {

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = "http://www.jd.com";
        if (getIntent().getExtras() != null) {
            url = getIntent().getStringExtra("url");
        }


//        CustomChrome.getInstance(this).load(url);

//        myWebView = (WebView) findViewById(R.id.webview);
//        WebChromeClient chromeClient = new myChromeClient();
//        myWebView.setWebChromeClient(chromeClient);
//        WebViewClient webViewClient = new myWebViewClient();
//        myWebView.setWebViewClient(webViewClient);
//        WebSettings webSettings =  myWebView.getSettings();
//
//        if (url != null) {
////            myWebView.loadUrl(url);
//        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();
        }

        return super.onKeyUp(keyCode, event);
    }

    private class myWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
            return false;

        }

    }


    private class myChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            ((TextView) findViewById(R.id.title)).setText(title);
            super.onReceivedTitle(view, title);
        }
    }
}

