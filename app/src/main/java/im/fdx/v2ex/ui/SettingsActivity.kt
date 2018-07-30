package im.fdx.v2ex.ui

import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.*
import android.util.Log
import androidx.core.net.toUri
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.JOB_ID_GET_NOTIFICATION
import im.fdx.v2ex.utils.Keys.PREF_VERSION
import im.fdx.v2ex.utils.Keys.notifyID
import im.fdx.v2ex.utils.extensions.setUpToolbar
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setUpToolbar("设置")
        supportFragmentManager.beginTransaction()
                .add(R.id.container, SettingsFragment())
                .commit()

    }


    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var listPreference: ListPreference
        private lateinit var jobSchedule: JobScheduler

        private var count: Int = 0

        override fun onCreatePreferences(savedInstanceState: Bundle?, what: String?) {
            addPreferencesFromResource(R.xml.preference)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            jobSchedule = activity?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            when {
                MyApp.get().isLogin -> {

                    addPreferencesFromResource(R.xml.preference_login)
                    findPreference("group_user").title = sharedPreferences.getString("username", getString(R.string.user))
                    findPreference(Keys.PREF_LOGOUT).onPreferenceClickListener = Preference.OnPreferenceClickListener {

                        AlertDialog.Builder(activity!!)
                                .setTitle("提示")
                                .setMessage("确定要退出吗")
                                .setPositiveButton(R.string.ok) { d, _ ->
                                    HttpHelper.myCookieJar.clear()
                                    MyApp.get().setLogin(false)
                                    LocalBroadcastManager.getInstance(activity!!).sendBroadcast(Intent(Keys.ACTION_LOGOUT))
                                    findPreference(Keys.PREF_LOGOUT).isEnabled = false
                                    d.dismiss()
                                    activity!!.finish()
                                    activity!!.toast("已退出登录")
                                }
                                .setNegativeButton(R.string.cancel) { _, _ ->

                                }
                                .show()
                        true
                    }

                    listPreference = findPreference("pref_msg_period") as ListPreference
                    listPreference.entry?.let {
                        listPreference.summary = it//初始化时设置summary
                    }


                    if (!sharedPreferences.getBoolean("pref_msg", false)) {
                        findPreference("pref_msg_period").isEnabled = false
                        findPreference("pref_background_msg").isEnabled = false
                    }
                }
            }

            prefRate()
            prefVersion()

        }


        private fun prefVersion() {

            try {
                val manager = activity!!.packageManager
                val info: PackageInfo = manager.getPackageInfo(activity!!.packageName, 0)
                findPreference(PREF_VERSION).summary = info.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val ha = resources.getStringArray(R.array.j)
            count = 7
            findPreference(PREF_VERSION).onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (count < 0) {
                    count = 5
                    activity?.longToast(ha[(System.currentTimeMillis() / 100 % ha.size).toInt()])
                }
                count--
                true
            }
        }

        private fun prefRate() {
            findPreference(Keys.PREF_RATES).onPreferenceClickListener = Preference.OnPreferenceClickListener {
                try {
                    val uri = "market://details?id=im.fdx.v2ex".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    activity?.toast("没有可用的应用商店，请安装后重试")
                }
                true
            }
        }

        @Suppress("unused，仅测试手动加preference")
        private fun addSettings() {
            val screen = this.preferenceScreen // "null". See onViewCreated.

            // Create the Preferences Manually - so that the key can be set programatically.
            val category = PreferenceCategory(screen.context)
            category.title = "Channel Configuration"
            category.order = 0
            screen.addPreference(category)

            val checkBoxPref = CheckBoxPreference(screen.context)
            checkBoxPref.key = "_ENABLED"
            checkBoxPref.title = "Enabled"
            checkBoxPref.summary = "CCCC"
            checkBoxPref.isChecked = true

            category.addPreference(checkBoxPref)
        }

        override fun onResume() {
            super.onResume()
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        }

        override fun onPause() {
            super.onPause()
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            Log.w("PREF", key)
            when (key) {
                "pref_msg" ->

                    if (sharedPreferences.getBoolean(key, false)) {
                        findPreference("pref_msg_period").isEnabled = true
                        findPreference("pref_background_msg").isEnabled = true
                    } else {
                        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(notifyID)
                        jobSchedule.cancel(JOB_ID_GET_NOTIFICATION)
                        findPreference("pref_msg_period").isEnabled = false
                        findPreference("pref_background_msg").isEnabled = false

                    }
                "pref_background_msg" -> {
                }
                "pref_msg_period" -> {
                    listPreference.summary = listPreference.entry
                    XLog.d("pref_msg_period changed")
                }

                "pref_add_row" -> {
                }
            }
        }

    }
}
