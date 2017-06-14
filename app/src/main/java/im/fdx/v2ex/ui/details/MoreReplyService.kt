package im.fdx.v2ex.ui.details

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import okhttp3.Request
import org.jsoup.Jsoup
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

        XLog.tag("DetailsActivity").d(totalPage.toString() + " | " + topicId)
        if (totalPage == -1 || topicId == null) {
            return
        }

        try {
            for (i in 2..totalPage) {
                val response = HttpHelper.OK_CLIENT.newCall(Request.Builder()
                        .headers(HttpHelper.baseHeaders)
                        .url(NetManager.HTTPS_V2EX_BASE + "/t/" + topicId + "?p=" + i)
                        .build()).execute()
                val body = Jsoup.parse(response.body()!!.string())
                val replies = NetManager.parseResponseToReplay(body)
                val token = NetManager.parseToVerifyCode(body)

                XLog.tag("DetailsActivity").d(replies[0].content)
                val it = Intent()
                it.action = "im.fdx.v2ex.reply"
                it.putExtra("token", token)
                it.putParcelableArrayListExtra("replies", replies)

                if (i == totalPage && isToBottom) {
                    it.putExtra("bottom", true)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(it)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun getOneMore(intent: Intent) {

        val totalPage = intent.getIntExtra("page", -1)
        val topicId = intent.getLongExtra("topic_id", -1)
        val currentPage = intent.getIntExtra("currentPage", -1)

        XLog.tag("DetailsActivity").d(totalPage.toString() + "<----totalPage | topicId :" + topicId + " |current :  " + currentPage)
        if (totalPage == -1 || topicId == -1L || currentPage == -1) {
            return
        }

        try {
            val response = HttpHelper.OK_CLIENT.newCall(Request.Builder()
                    .headers(HttpHelper.baseHeaders)
                    .url(NetManager.HTTPS_V2EX_BASE + "/t/" + topicId + "?p=" + currentPage)
                    .build()).execute()
            val body = Jsoup.parse(response.body()!!.string())
            val replies = NetManager.parseResponseToReplay(body)
            val token = NetManager.parseToVerifyCode(body)

            XLog.tag("DetailsActivity").d(replies[0].content)
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
