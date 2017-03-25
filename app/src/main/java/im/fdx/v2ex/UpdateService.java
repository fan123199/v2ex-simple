package im.fdx.v2ex;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.NotificationActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateService extends Service {

    private AlarmManager alarmManager;
    private static final long INTERVAL = 1000 * 120;


    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //// TODO: 2017/3/24 JobScheduler or period settings.
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                System.currentTimeMillis() + 2500, INTERVAL,
//                getOperationIntent());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        notificationUnRead();
        return START_STICKY;
    }

    private void notificationUnRead() {
        String url = "https://www.v2ex.com/my/following";
        HttpHelper.OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 200) {
                    NetManager.dealError();
                    return;
                }

                String html = response.body().string();
//                <a href="/notifications" class="fade">0 条未读提醒</a>
                Pattern p = Pattern.compile("(?<=<a href=\"/notifications\".{0,20}>)\\d+");
                Matcher matcher = p.matcher(html);
                if (matcher.find()) {
                    String num = matcher.group();
                    XLog.d("num" + num);

                    if (!num.equals("0")) {
                        putNotification(UpdateService.this, num);
                    }
                } else {
                    XLog.d("not find num of message");
                }
            }
        });
    }

    public static void putNotification(Context context, String number) {
        Intent resultIntent = new Intent(context, NotificationActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(NotificationActivity.class);
//        stackBuilder.addNextIntent(resultIntent);

        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent easyPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        int notifyID = 1223;
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
                .setTicker("this is from others")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notification_icon)
//                        .setLargeIcon(R.drawable.logo2x)
                .setLights(c, 2000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
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
    public void onDestroy() {
        super.onDestroy();
        XLog.d("service onDestroy");
//        alarmManager.cancel();
    }
}

