package im.fdx.v2ex.network

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.NotificationActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.logd
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class GetMoreRepliesWorker(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {

        getAllMore()

        return Result.success()

    }
    private fun getAllMore() {
        val totalPage = inputData.getInt("page", -1)
        val topicId = inputData.getString("topic_id")
        val isToBottom = inputData.getBoolean("bottom", false)

        logd("$totalPage | $topicId")
        if (totalPage <= 1 || topicId == null) {
            return
        }

        try {
            for (i in 2..totalPage) {
                val response = vCall("${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=$i").execute()
                val parser = Parser(response.body!!.string())
                val replies = parser.getReplies()

                if (replies.isEmpty()) return
                val replyIntent = Intent()
                replyIntent.action = Keys.ACTION_GET_MORE_REPLY
                replyIntent.putExtra(Keys.KEY_TOPIC_ID, topicId)
                replyIntent.putParcelableArrayListExtra("replies", replies)

                if (i == totalPage && isToBottom) {
                    replyIntent.putExtra("bottom", true)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(replyIntent)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}