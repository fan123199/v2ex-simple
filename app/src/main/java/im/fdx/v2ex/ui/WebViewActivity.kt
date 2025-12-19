package im.fdx.v2ex.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.theme.V2ExTheme

class WebViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra("url") ?: ""

        setContent {
            V2ExTheme {
                WebViewScreen(
                    url = url,
                    onBackClick = { finish() },
                    onLoginSuccess = {
                         setResult(Activity.RESULT_OK)
                         finish()
                    }
                )
            }
        }
    }
}

