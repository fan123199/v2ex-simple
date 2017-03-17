package im.fdx.v2ex.network;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.readystatesoftware.chuck.ChuckInterceptor;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.network.cookie.CookiePersistor;
import im.fdx.v2ex.network.cookie.MyCookieJar;
import im.fdx.v2ex.network.cookie.SharedPrefsPersistor;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

public class HttpHelper {

    public static final int REQUEST_SIGNUP = 0;
    private static PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApp.getInstance().getApplicationContext()));
    public static final MyCookieJar myCookieJar = new MyCookieJar(new SharedPrefsPersistor(MyApp.getInstance().getApplicationContext()));
    public static final OkHttpClient OK_CLIENT = new OkHttpClient().newBuilder()
//            .addInterceptor(new HttpLoggingInterceptor())
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)  //禁止重定向
            .addInterceptor(new ChuckInterceptor(MyApp.getInstance().getApplicationContext()))//好东西，查看Okhttp数据
            .cookieJar(myCookieJar)
//            .cookieJar(cookieJar)
            .build();
    public static Headers baseHeaders = new Headers.Builder()
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .add("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
            .add("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            .add("Host", "www.v2ex.com")
            .add("Cache-Control", "max-age=0")
//            .add("X-Requested-With", "com.android.browser")
//            .add("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Mobile Safari/537.36");
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36")
            .build();


}
