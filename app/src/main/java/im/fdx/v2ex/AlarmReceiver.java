package im.fdx.v2ex;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.NotificationActivity;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int notifyID = 1223;

    public static void notificationUnRead(final Context context) {
        String url = "https://www.v2ex.com/my/following";
        HttpHelper.OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError(context, -1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                int code = response.code();
                if (code != 200) {
                    NetManager.dealError(context, code);
                    return;
                }

                String html = response.body().string();
//                <a href="/notifications" class="fade">0 条未读提醒</a>
                Pattern p = Pattern.compile("(?<=<a href=\"/notifications\".{0,20}>)\\d+");
                Matcher matcher = p.matcher(html);
                if (matcher.find()) {
                    int num = Integer.parseInt(matcher.group());
                    XLog.d("num" + num);
                    if (num != 0) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Keys.ACTION_GET_NOTIFICATION));
                        putNotification(context, num);
                    }
                } else {
                    XLog.e("not find num of unread message");
                }
            }
        });
    }

    public static void putNotification(Context context, int number) {
        Intent resultIntent = new Intent(context, NotificationActivity.class);
        resultIntent.putExtra("number", number);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(NotificationActivity.class);
//        stackBuilder.addNextIntent(resultIntent);

        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        int c;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = context.getResources().getColor(R.color.primary, context.getTheme());
        } else {
            c = context.getResources().getColor(R.color.primary);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
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
                .setContentIntent(resultPendingIntent)
//                .setContentIntent(easyPendingIntent)
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBuilder.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));
        }
        Notification mNotificationCompat = mBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifyID, mNotificationCompat);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        XLog.d("YES, It is alarm");
        notificationUnRead(context);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("im.fdx.v2ex.action_alarm"));
    }

}
