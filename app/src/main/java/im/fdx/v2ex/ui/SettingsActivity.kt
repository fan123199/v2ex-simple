package im.fdx.v2ex.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
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
import im.fdx.v2ex.utils.Keys.PREF_NIGHT_MODE
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
    setUpToolbar(getString(R.string.settings))
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, SettingsFragment())
        .commit()
  }

  class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var count: Int = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, what: String?) {
      addPreferencesFromResource(R.xml.preference)
      prefTab()
      prefNightMode()
      prefRate()

      prefVersion()

      if (myApp.isLogin) {
        addPreferencesFromResource(R.xml.preference_login)
        findPreference<Preference>("group_user")?.title = pref.getString(Keys.PREF_USERNAME, "user")
        prefUser()
        prefMessage()
      }

    }

    private fun prefNightMode() {
    }

    private fun prefMessage() {
      val listPreference = findPreference<ListPreference>("pref_msg_period")
      if (!pref.getBoolean("pref_msg", false)) {
        findPreference<Preference>("pref_background_msg")?.isEnabled = false
        findPreference<Preference>("pref_msg_period")?.isEnabled = false
      }
    }

    private fun prefTab() {
      findPreference<Preference>("pref_tab_bar")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
        startActivity(Intent(requireActivity(), TabSettingActivity::class.java))
        true
      }
    }

    private fun prefUser() {
      findPreference<Preference>(Keys.PREF_LOGOUT)?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

        AlertDialog.Builder(requireActivity())
            .setTitle("提示")
            .setMessage("确定要退出吗")
            .setPositiveButton(R.string.ok) { _, _ ->
              HttpHelper.myCookieJar.clear()
              setLogin(false)
              findPreference<Preference>(Keys.PREF_LOGOUT)?.isEnabled = false
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

      findPreference<Preference>(PREF_VERSION)?.summary = BuildConfig.VERSION_NAME

      val ha = resources.getStringArray(R.array.j)
      count = 7
      findPreference<Preference>(PREF_VERSION)?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
        if (count < 0) {
          count = 3
          activity?.longToast(ha[(System.currentTimeMillis() / 100 % ha.size).toInt()])
        }
        count--
        true
      }
    }

    private fun prefRate() {
      findPreference<Preference>(Keys.PREF_RATES)?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
        try {
          val uri = "market://details?id=im.fdx.v2ex".toUri()
          val intent = Intent(Intent.ACTION_VIEW, uri)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          startActivity(intent)
        } catch (e: Exception) {
          activity?.toast(getString(R.string.there_is_no_app_store))
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
            findPreference<Preference>("pref_msg_period")?.isEnabled = true
            findPreference<Preference>("pref_background_msg")?.isEnabled = true
          } else {
            val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notifyID)
            WorkManager.getInstance().cancelAllWorkByTag(TAG_WORKER)
            findPreference<Preference>("pref_msg_period")?.isEnabled = false
            findPreference<Preference>("pref_background_msg")?.isEnabled = false

          }
        "pref_background_msg" -> {}
        "pref_msg_period" -> {}
        "pref_add_row" -> {}
        PREF_NIGHT_MODE -> {
          val mode = pref.getString(PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO.toString())!!
          AppCompatDelegate.setDefaultNightMode(mode.toInt())
        }
        PREF_TEXT_SIZE -> {
          LocalBroadcastManager.getInstance(myApp).sendBroadcast(Intent(Keys.ACTION_TEXT_SIZE_CHANGE))
          activity?.finish()
        }
      }
    }
  }
}
