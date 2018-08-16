package im.fdx.v2ex.ui.details

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.utils.extensions.logd
import okhttp3.Request
import java.io.IOException

/**
 * 为什么不用Thread？感觉Thread不高级
 */
class MoreReplyService @JvmOverloads constructor(name: String = "what") : IntentService(name) {

    override fun onHandleIntent(intent: Intent?) {

        when (intent) {
            null -> return
            else -> getAllMore(intent)
        }
    }

    private fun getAllMore(intent: Intent) {
        val totalPage = intent.getIntExtra("page", -1)
        val topicId = intent.getStringExtra("topic_id")
        val isToBottom = intent.getBooleanExtra("bottom", false)

        logd(totalPage.toString() + " | " + topicId)
        if (totalPage <= 1 || topicId == null) {
            return
        }

        try {
            for (i in 2..totalPage) {
                val response = vCall("${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=$i").execute()
                val parser = Parser(response.body()!!.string())
                val replies = parser.getReplies()
                val token = parser.getVerifyCode()

                if (replies.isEmpty()) return
                val replyIntent = Intent()
                replyIntent.action = "im.fdx.v2ex.reply"
                token?.let {
                    replyIntent.putExtra("token", token)
                }
                replyIntent.putParcelableArrayListExtra("replies", replies)

                if (i == totalPage && isToBottom) {
                    replyIntent.putExtra("bottom", true)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(replyIntent)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun getOneMore(intent: Intent) {

        val totalPage = intent.getIntExtra("page", -1)
        val topicId = intent.getLongExtra("topic_id", -1)
        val currentPage = intent.getIntExtra("currentPage", -1)

      logd(totalPage.toString() + "<----totalPage | topicId :" + topicId + " |current :  " + currentPage)
        if (totalPage == -1 || topicId == -1L || currentPage == -1) {
            return
        }

        try {
            val response = HttpHelper.OK_CLIENT.newCall(Request.Builder()
                    .url("${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=$currentPage")
                    .build()).execute()
            val parser = Parser(response.body()!!.string())

            val replies = parser.getReplies()
            val token = parser.getVerifyCode()

          logd(replies[0].content)
            val it = Intent()
            it.action = "im.fdx.v2ex.reply"
            it.putExtra("token", token)
            it.putParcelableArrayListExtra("replies", replies)

            if (currentPage == totalPage) {
                it.putExtra("bottom", true)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(it)

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
