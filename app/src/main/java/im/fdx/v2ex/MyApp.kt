package im.fdx.v2ex

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatDelegate.*
import androidx.core.content.edit
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.logd
import org.jetbrains.anko.defaultSharedPreferences

val pref: SharedPreferences by lazy {
  myApp.defaultSharedPreferences
}

@Deprecated("技术困难，下一期实现")
val userPref: SharedPreferences by lazy {
  val fileName = pref.getString(Keys.KEY_USERNAME, "user")
  myApp.getSharedPreferences(fileName, Context.MODE_PRIVATE)
}

val myApp: MyApp by lazy {
  MyApp.get()
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
  internal var isLogin = false

  fun setLogin(login: Boolean) {
    isLogin = login
    pref.edit {
      putBoolean(Keys.PREF_KEY_IS_LOGIN, login)
    }
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
    XLog.init(when {
      BuildConfig.DEBUG -> LogLevel.ALL
      else -> LogLevel.NONE
    })
    isLogin = pref.getBoolean(Keys.PREF_KEY_IS_LOGIN, false)
    logd("onCreate\nisLogin:$isLogin")
    setDefaultNightMode(if (pref.getBoolean("NIGHT_MODE", false)) MODE_NIGHT_YES else MODE_NIGHT_NO)
  }
}
