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
import im.fdx.v2ex.utils.Keys
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class GetMsgWorker(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {

        getUnread(context)

        return Result.success()

    }

    private fun getUnread(context: Context) {

        vCall(NetManager.URL_FOLLOWING).start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(context, -1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                if (code != 200) {
                    NetManager.dealError(context, code)
                    return
                }

                val html = response.body!!.string()
                //                <a href="/notifications" class="fade">0 条未读提醒</a>
                val p = Regex("(?<=<a href=\"/notifications\".{0,20}>)\\d+")
                val matcher = p.find(html)
                if (matcher!= null) {
                    val num = matcher.value.toIntOrNull()
                    XLog.d("num:$num")
                    if (num != null && num != 0) {
                        val intent = Intent(Keys.ACTION_GET_NOTIFICATION)
                        intent.putExtra(Keys.KEY_UNREAD_COUNT, num)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                        putNotification(context, num)
                    }
                } else {
                    XLog.e("not find num of unread message")
                }
            }
        })
    }

    private fun putNotification(context: Context, number: Int) {
        val resultIntent = Intent(context, im.fdx.v2ex.ui.main.MainActivity::class.java)
        resultIntent.putExtra(Keys.KEY_UNREAD_COUNT, number)
        val stackBuilder = TaskStackBuilder.create(context)

        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        val color = ContextCompat.getColor(context, R.color.primary)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "未读消息", NotificationManager.IMPORTANCE_DEFAULT)

            val notificationManager = context.getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(context, "channel_id")
        mBuilder
                //                .setSubText("subtext")
                .setContentTitle(context.getString(R.string.you_have_notifications, number))
                //                .setContentText("")
                .setTicker(context.getString(R.string.you_have_notifications))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notification_icon)
                //                        .setLargeIcon(R.drawable.logo2x)
                .setLights(color, 2000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(color)
                .setContentIntent(resultPendingIntent)
        val mNotificationCompat = mBuilder.build()
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(Keys.notifyID, mNotificationCompat)
    }
}