package im.fdx.v2ex;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApp extends Application {
    /**
     * @deprecated in future
     */
    public static final int USE_API = 1;
    public static final int USE_WEB = 2;

    private static MyApp instance;
    private SharedPreferences mDefaultSharedPrefs;

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public boolean isLogin() {
        return isLogin;
    }

    private boolean isLogin;

    private int httpMode;

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
        mDefaultSharedPrefs = getSharedPreferences("DEFAULT", MODE_PRIVATE);
        isLogin = mDefaultSharedPrefs.getBoolean("is_login", false);

        httpMode = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean("pref_http_mode", false) ? USE_API : USE_WEB;
        Log.d("MyApp", "onCreate\n"
                + "\nisLogin:" + isLogin
                + "\nhttp mode :(1api,2web)" + httpMode);
    }

    public int getHttpMode() {
        return httpMode;
    }

    public void setHttpMode(int mode) {
        httpMode = mode;
    }

}
