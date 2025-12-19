package im.fdx.v2ex.ui.topic

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.ui.main.Topic

import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.utils.extensions.startActivity

class TopicActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val topicModel = intent.getParcelableExtra<Topic>(Keys.KEY_TOPIC_MODEL)
        val topicId = intent.getStringExtra(Keys.KEY_TOPIC_ID) ?: topicModel?.id ?: ""
        
        if (topicId.isEmpty()) {
            finish()
            return
        }

        setContent {
            V2ExTheme {
                TopicDetailScreen(
                    topicId = topicId,
                    initialTopic = topicModel,
                    onBackClick = { finish() },
                    onMemberClick = { username -> startActivity<MemberActivity>(Keys.KEY_USERNAME to username) }
                )
            }
        }
    }
}