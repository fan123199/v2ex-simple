package im.fdx.v2ex

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import com.elvishew.xlog.XLog


class UpdateService : Service() {

    private var alarmManager: AlarmManager? = null

    override fun onCreate() {
        super.onCreate()

        //// TODO: 2017/3/24 JobScheduler or period settings.
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        alarmManager!!.cancel(operationIntent)
        val interval = java.lang.Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_msg_period", "60")) * 1000L
        alarmManager!!.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5 * 1000, interval,
                operationIntent)
        XLog.d("updateService alarmManager: time" + interval)
        return Service.START_STICKY
    }

    private val operationIntent: PendingIntent
        get() {
            val intent = Intent(this, AlarmReceiver::class.java)
            return PendingIntent.getBroadcast(this, 199, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

    override fun onDestroy() {
        super.onDestroy()
        XLog.d("service onDestroy")
        alarmManager!!.cancel(operationIntent)
    }
}

