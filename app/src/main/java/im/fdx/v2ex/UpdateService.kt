package im.fdx.v2ex

import android.app.Service
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.elvishew.xlog.XLog


class UpdateService : Service() {

    override fun onCreate() {
        super.onCreate()
        XLog.d("service onCreate")

        val mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.d("service onDestroy")
    }
}

