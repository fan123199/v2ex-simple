package im.fdx.v2ex.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.*
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.PREF_TAB
import im.fdx.v2ex.utils.Keys.PREF_TEXT_SIZE
import im.fdx.v2ex.utils.Keys.PREF_VERSION
import im.fdx.v2ex.utils.Keys.TAG_WORKER
import im.fdx.v2ex.utils.Keys.notifyID
import im.fdx.v2ex.utils.extensions.setUpToolbar
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class SettingsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    setUpToolbar("设置")
    supportFragmentManager.beginTransaction()
        .add(R.id.container, SettingsFragment())
        .commit()
  }

  class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var count: Int = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, what: String?) {
      addPreferencesFromResource(R.xml.preference)


      when {
        MyApp.get().isLogin -> {
          addPreferencesFromResource(R.xml.preference_login)
          findPreference("group_user").title = pref.getString(Keys.PREF_USERNAME, "user")
          prefUser()
          prefMessage()
          prefTab()
        }
      }

      prefRate()
      prefVersion()

    }

    private fun prefMessage() {
      val listPreference: ListPreference = findPreference("pref_msg_period") as ListPreference
      listPreference.entry?.let {
        listPreference.summary = it//初始化时设置summary
      }

      if (!pref.getBoolean("pref_msg", false)) {
        findPreference("pref_background_msg").isEnabled = false
        findPreference("pref_msg_period").isEnabled = false
      }
    }

    private fun prefTab() {
      findPreference("pref_tab_bar").onPreferenceClickListener = Preference.OnPreferenceClickListener {
        val intent = Intent(activity!!, TabSettingActivity::class.java)
        startActivityForResult(intent, 110)
        true
      }
    }

    private fun prefUser() {
      findPreference(Keys.PREF_LOGOUT).onPreferenceClickListener = Preference.OnPreferenceClickListener {

        AlertDialog.Builder(activity!!)
            .setTitle("提示")
            .setMessage("确定要退出吗")
            .setPositiveButton(R.string.ok) { _, _ ->
              HttpHelper.myCookieJar.clear()
              myApp.setLogin(false)
              findPreference(Keys.PREF_LOGOUT).isEnabled = false
              pref.edit {
                remove(PREF_TEXT_SIZE)
                remove(PREF_TAB)
              }
              activity?.finish()
              activity?.toast("已退出登录")
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .show()
        true
      }
    }

    private fun prefVersion() {

      findPreference(PREF_VERSION).summary = BuildConfig.VERSION_NAME

      val ha = resources.getStringArray(R.array.j)
      count = 7
      findPreference(PREF_VERSION).onPreferenceClickListener = Preference.OnPreferenceClickListener {
        if (count < 0) {
          count = 3
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
          activity?.toast("没有可用的应用商店，请检查后重试")
        }
        true
      }
    }

    override fun onResume() {
      super.onResume()
      pref.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onPause() {
      super.onPause()
      pref.unregisterOnSharedPreferenceChangeListener(this)
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
            WorkManager.getInstance().cancelAllWorkByTag(TAG_WORKER)
            findPreference("pref_msg_period").isEnabled = false
            findPreference("pref_background_msg").isEnabled = false

          }
        "pref_background_msg" -> {
        }
        "pref_msg_period" -> {
          val listPreference: ListPreference = findPreference("pref_msg_period") as ListPreference
          listPreference.summary = listPreference.entry
          XLog.d("pref_msg_period changed")
        }

        "pref_add_row" -> {
        }

        Keys.PREF_TEXT_SIZE -> {
          LocalBroadcastManager.getInstance(activity!!).sendBroadcast(Intent(Keys.ACTION_TEXT_SIZE_CHANGE))
          activity?.finish()
        }
      }
    }
  }
}
