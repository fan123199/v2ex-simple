package im.fdx.v2ex;

import android.app.Application;

import im.fdx.v2ex.network.MySingleton;

/**
 * Created by fdx on 2015/8/16.
 * 用于启动时获取app状态
 */
public class MyApplication extends Application {
    private static MyApplication instance;
    public static MyApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        //  Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
