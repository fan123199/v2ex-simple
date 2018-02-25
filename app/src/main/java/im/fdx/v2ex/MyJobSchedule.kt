package im.fdx.v2ex

import android.app.job.JobParameters
import android.app.job.JobService

class MyJobSchedule : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        return true
    }
}