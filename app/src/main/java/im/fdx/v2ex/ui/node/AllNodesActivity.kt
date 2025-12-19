package im.fdx.v2ex.ui.node

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.startActivity

class AllNodesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isChoose = intent.getBooleanExtra(Keys.KEY_TO_CHOOSE_NODE, false)
        
        setContent {
            V2ExTheme {
                 AllNodesScreen(
                     onBackClick = { finish() },
                     onNodeClick = { node ->
                         if (isChoose) {
                             setResult(Activity.RESULT_OK, Intent().apply { putExtra(Keys.KEY_NODE, node) })
                             finish()
                         } else {
                             startActivity<NodeActivity>(Keys.KEY_NODE_NAME to node.name)
                         }
                     }
                 )
            }
        }
    }
}