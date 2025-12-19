package im.fdx.v2ex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.settings.TabSettingScreen
import im.fdx.v2ex.ui.theme.V2ExTheme

class TabSettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            V2ExTheme {
                TabSettingScreen(onBackClick = { finish() })
            }
        }
    }
}