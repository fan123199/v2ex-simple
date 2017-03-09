package im.fdx.v2ex.network;

import com.readystatesoftware.chuck.ChuckInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.fdx.v2ex.MyApplication;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

public class HttpHelper {


    public static final int REQUEST_SIGNUP = 0;
    public static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
//            .addInterceptor(interceptor)
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new ChuckInterceptor(MyApplication.getInstance().getApplicationContext()))
            .cookieJar(new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);

                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {

                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies == null ? new ArrayList<Cookie>() : cookies;
                }
            })
            .build();
    public static Request.Builder baseRequestBuilder = new Request.Builder()
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            .addHeader("Host", "www.v2ex.com")
            .addHeader("Cache-Control", "max-age=0")
//            .addHeader("X-Requested-With", "com.android.browser")
//            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Mobile Safari/537.36");
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36");
}
