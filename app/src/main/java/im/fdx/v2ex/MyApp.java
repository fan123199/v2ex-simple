package im.fdx.v2ex;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

import im.fdx.v2ex.utils.Keys;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApp extends Application {

    private static MyApp instance;
    private SharedPreferences mPrefs;

    public void setLogin(boolean login) {
        isLogin = login;

        if (login) {
            mPrefs.edit().putBoolean(Keys.PREF_KEY_IS_LOGIN, true).apply();
        } else {
            mPrefs.edit().remove(Keys.PREF_KEY_IS_LOGIN).apply();
        }
    }

    public boolean isLogin() {
        return isLogin;
    }

    private boolean isLogin;

    public static MyApp getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        XLog.init(BuildConfig.DEBUG ? LogLevel.ALL   // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                : LogLevel.NONE);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLogin = mPrefs.getBoolean(Keys.PREF_KEY_IS_LOGIN, false);

        Log.d("MyApp", "onCreate\n"
                + "\nisLogin:" + isLogin);
    }

}
