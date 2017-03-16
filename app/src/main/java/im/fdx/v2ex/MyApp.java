package im.fdx.v2ex;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import com.elvishew.xlog.XLog;

import static java.lang.reflect.Array.getInt;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApp extends Application {
    /**
     * @deprecated in future
     */
    public static final int USE_API = 1;
    private static MyApp instance;
    public static final int USE_WEB = 2;

    private int httpMode;

    public static MyApp getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        initHttpMode();
        XLog.init();
        Log.d("APP", "onCreate");
    }

    public int getHttpMode() {
        return httpMode;
    }

    public void setHttpMode(int mode) {
        httpMode = mode;
    }

    private void initHttpMode() {
        httpMode = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean("pref_http_mode", false) ? USE_API : USE_WEB;
    }

    public void switchHttpMode() {
        httpMode = httpMode == USE_API ? USE_WEB : USE_API;
    }
}
