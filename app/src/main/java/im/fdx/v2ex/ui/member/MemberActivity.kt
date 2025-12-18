package im.fdx.v2ex.ui.member

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.toast

class MemberActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val username = when {
             intent.data != null -> intent.data!!.pathSegments.getOrNull(1)
             intent.extras != null -> intent.extras!!.getString(Keys.KEY_USERNAME)
             else -> null
        }

        if (username.isNullOrEmpty()) {
            toast("未知问题，无法访问用户信息")
            finish()
            return
        }

        setContent {
            V2ExTheme {
                MemberScreen(
                    username = username,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
