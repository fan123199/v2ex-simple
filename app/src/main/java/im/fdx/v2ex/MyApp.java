package im.fdx.v2ex;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

import im.fdx.v2ex.utils.Keys;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApp extends Application {

    public static final int USE_WEB = 2;

    private static MyApp instance;

    public void setLogin(boolean login) {
        isLogin = login;
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
        XLog.init(BuildConfig.DEBUG ? LogLevel.ALL             // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                : LogLevel.NONE);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        SharedPreferences mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLogin = mDefaultSharedPrefs.getBoolean(Keys.PREF_KEY_IS_LOGIN, false);

        Log.d("MyApp", "onCreate\n"
                + "\nisLogin:" + isLogin);
    }

}
