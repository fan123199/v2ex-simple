package im.fdx.v2ex

import android.app.*
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.URL_FOLLOWING
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.NotificationActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.logi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern


class MyJobSchedule : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        logi("jobStart")
        getNotification(this, params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        logi("jobStop")
        return false
    }

    private fun getNotification(context: Context, params: JobParameters?) {

        vCall(URL_FOLLOWING).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(context, -1)
                jobFinished(null, true)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val code = response.code()
                if (code != 200) {
                    NetManager.dealError(context, code)
                    jobFinished(null, false)
                    return
                }

                val html = response.body()!!.string()
                //                <a href="/notifications" class="fade">0 条未读提醒</a>
                val p = Pattern.compile("(?<=<a href=\"/notifications\".{0,20}>)\\d+")
                val matcher = p.matcher(html)
                if (matcher.find()) {
                    val num = Integer.parseInt(matcher.group())
                    XLog.d("num:$num")
                    if (num != 0) {
                        val intent = Intent(Keys.ACTION_GET_NOTIFICATION)
                        intent.putExtra(Keys.KEY_UNREAD_COUNT, num)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                        putNotification(context, num, params)
                    }
                } else {
                    XLog.e("not find num of unread message")
                }
            }
        })
    }

    private fun putNotification(context: Context, number: Int, params: JobParameters?) {
        val resultIntent = Intent(context, NotificationActivity::class.java)
        resultIntent.putExtra(Keys.KEY_UNREAD_COUNT, number)
        val stackBuilder = TaskStackBuilder.create(context)

        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val color = ContextCompat.getColor(context, R.color.primary)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "未读消息", IMPORTANCE_DEFAULT)

            val notificationManager = getSystemService(
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
        jobFinished(params, false)
    }

}