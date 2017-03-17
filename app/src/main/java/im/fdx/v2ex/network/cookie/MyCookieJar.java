package im.fdx.v2ex.network.cookie;

import android.util.DebugUtils;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by fdx on 2017/3/16.
 * <p>
 * 请用自己的Cookie策略，而不是开源库。
 */
public class MyCookieJar implements CookieJar {
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private List<Cookie> mCookies = new ArrayList<>();
    private CookiePersistor cookiePersistor;

    public MyCookieJar(CookiePersistor persistor) {
        this.cookiePersistor = persistor;

        mCookies.addAll(cookiePersistor.loadAll());

    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        mCookies.clear();
        mCookies.addAll(cookies);

        cookiePersistor.persistAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {


        List<Cookie> cookies;
        if (cookiePersistor.loadAll() != null) {
            cookies = cookiePersistor.loadAll();
        } else
            cookies = cookieStore.get(url.host());


        XLog.e(cookies);

        return cookies == null ? new ArrayList<Cookie>() : cookies;
    }


    public void clear() {

        mCookies.clear();
        cookiePersistor.clear();

    }
}
