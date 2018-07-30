package im.fdx.v2ex

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate.*
import androidx.core.content.edit
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import im.fdx.v2ex.utils.Keys

val pref: SharedPreferences by lazy {
    MyApp.get().mPrefs
}

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
class MyApp : Application() {

    companion object {
        private lateinit var INSTANCE: MyApp
        fun get(): MyApp {
            return INSTANCE
        }
    }

    internal lateinit var mPrefs: SharedPreferences
    internal var isLogin = false

    internal var curTextSize = 0


    fun setLogin(login: Boolean) {
        isLogin = login
        if (login) {
            mPrefs.edit {
                putBoolean(Keys.PREF_KEY_IS_LOGIN, true)
            }
        } else {
            mPrefs.edit {
                remove(Keys.PREF_KEY_IS_LOGIN)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        PreferenceManager.setDefaultValues(this, R.xml.preference, false)
        setDefaultNightMode(if (mPrefs.getBoolean("NIGHT_MODE", false)) MODE_NIGHT_YES else MODE_NIGHT_NO)
        XLog.init(when {
            BuildConfig.DEBUG -> LogLevel.ALL
            else -> LogLevel.NONE
        })
        isLogin = mPrefs.getBoolean(Keys.PREF_KEY_IS_LOGIN, false)
        XLog.tag("MyApp").d("onCreate\nisLogin:$isLogin")
    }
}
