package im.fdx.v2ex.ui.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.startActivity

class SearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)
        } else null

        setContent {
            V2ExTheme {
                SearchScreen(
                    initialQuery = query,
                    onBackClick = { finish() },
                    onTopicClick = { topicId -> startActivity<TopicActivity>(Keys.KEY_TOPIC_ID to topicId) },
                    onMemberClick = { username -> startActivity<MemberActivity>(Keys.KEY_USERNAME to username) },
                    onNodeClick = { nodeName -> startActivity<NodeActivity>(Keys.KEY_NODE_NAME to nodeName) }
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
         if (Intent.ACTION_SEARCH == intent.action) {
             val query = intent.getStringExtra(SearchManager.QUERY)
             setContent {
                 V2ExTheme {
                     SearchScreen(
                         initialQuery = query,
                         onBackClick = { finish() },
                         onTopicClick = { topicId -> startActivity<TopicActivity>(Keys.KEY_TOPIC_ID to topicId) },
                         onMemberClick = { username -> startActivity<MemberActivity>(Keys.KEY_USERNAME to username) },
                         onNodeClick = { nodeName -> startActivity<NodeActivity>(Keys.KEY_NODE_NAME to nodeName) }
                     )
                 }
             }
         }
    }
}
