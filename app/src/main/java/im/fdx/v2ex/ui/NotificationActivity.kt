package im.fdx.v2ex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.notification.NotificationScreen
import im.fdx.v2ex.ui.theme.V2ExTheme

class NotificationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            V2ExTheme {
                NotificationScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}
