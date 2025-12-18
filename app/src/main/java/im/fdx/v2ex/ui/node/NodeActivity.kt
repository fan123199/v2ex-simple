package im.fdx.v2ex.ui.node

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.toast

class NodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val nodeName = when {
            intent.data != null -> intent.data!!.pathSegments[1]
            intent.getStringExtra(Keys.KEY_NODE_NAME) != null -> intent.getStringExtra(Keys.KEY_NODE_NAME)!!
            else -> ""
        }

        if (nodeName.isEmpty()) {
            toast("打开节点失败")
            finish()
            return
        }

        setContent {
            V2ExTheme {
                NodeScreen(
                    nodeName = nodeName,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
