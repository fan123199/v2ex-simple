package im.fdx.v2ex;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;

public class UpdateService extends Service {

    private AlarmManager alarmManager;


    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //// TODO: 2017/3/24 JobScheduler or period settings.
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarmManager.cancel(getOperationIntent());
        long interval = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_msg_period", "60")) * 1000L;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5 * 1000, interval,
                getOperationIntent());
        XLog.d("updateService alarmManager: time" + interval);
        return START_STICKY;
    }

    private PendingIntent getOperationIntent() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        return PendingIntent.getBroadcast(this, 199, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XLog.d("service onDestroy");
        alarmManager.cancel(getOperationIntent());
    }
}

