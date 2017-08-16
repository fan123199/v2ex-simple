package im.fdx.v2ex

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.NotificationActivity
import im.fdx.v2ex.utils.Keys
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern

class AlarmReceiver : BroadcastReceiver() {

    val url = "https://www.v2ex.com/my/following"
    override fun onReceive(context: Context, intent: Intent) {
        XLog.d("YES, It is alarm")
        notificationUnRead(context)
    }

    private fun notificationUnRead(context: Context) {

        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(context, -1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {


                val code = response.code()
                if (code != 200) {
                    NetManager.dealError(context, code)
                    return
                }

                val html = response.body()!!.string()
                //                <a href="/notifications" class="fade">0 条未读提醒</a>
                val p = Pattern.compile("(?<=<a href=\"/notifications\".{0,20}>)\\d+")
                val matcher = p.matcher(html)
                if (matcher.find()) {
                    val num = Integer.parseInt(matcher.group())
                    XLog.d("num" + num)
                    if (num != 0) {
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
        val resultIntent = Intent(context, NotificationActivity::class.java)
        resultIntent.putExtra(Keys.KEY_UNREAD_COUNT, number)
        val stackBuilder = TaskStackBuilder.create(context)
        //        stackBuilder.addParentStack(NotificationActivity.class);
        //        stackBuilder.addNextIntent(resultIntent);

        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val c = ContextCompat.getColor(context, R.color.primary)
        val mBuilder = NotificationCompat.Builder(context, notifyID.toString())
        mBuilder
                //                .setSubText("subtext")
                .setContentTitle(context.getString(R.string.you_have_notifications, number))
                //                .setContentText("")
                .setTicker(context.getString(R.string.you_have_notifications))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notification_icon)
                //                        .setLargeIcon(R.drawable.logo2x)
                .setLights(c, 2000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(resultPendingIntent)//                .setContentIntent(easyPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBuilder.color = context.resources.getColor(R.color.primary, context.theme)
        }
        val mNotificationCompat = mBuilder.build()
        val mNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notifyID, mNotificationCompat)
    }

    companion object {

        val notifyID = 1223
    }

}
