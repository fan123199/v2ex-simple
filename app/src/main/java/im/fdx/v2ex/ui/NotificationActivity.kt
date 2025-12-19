package im.fdx.v2ex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.notification.NotificationScreen
import im.fdx.v2ex.ui.theme.V2ExTheme

import android.content.Intent
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.startActivity

class NotificationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            V2ExTheme {
                NotificationScreen(
                    onBackClick = { finish() },
                    onTopicClick = { topicId -> startActivity<TopicActivity>(Keys.KEY_TOPIC_ID to topicId) },
                    onMemberClick = { username -> startActivity<MemberActivity>(Keys.KEY_USERNAME to username) }
                )
            }
        }
    }
}

