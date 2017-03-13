package im.fdx.v2ex;

import android.app.Application;

import com.elvishew.xlog.XLog;

import static im.fdx.v2ex.network.HttpHelper.USE_VOLLEY;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApp extends Application {
    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        //  Auto-generated method stub
        super.onCreate();
        instance = this;
        XLog.init();
    }

    public int getHttpMode() {
        return USE_VOLLEY;
    }
}
