package im.fdx.v2ex.network.cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import im.fdx.v2ex.network.JsonManager;
import okhttp3.Cookie;

/**
 * Created by fdx on 2017/3/16.
 */

public class SharedPrefsPersistor implements CookiePersistor {

    private SharedPreferences sharedPreferences;


    public SharedPrefsPersistor(Context context) {
        this.sharedPreferences = context.getSharedPreferences("v2ex_cookie", Context.MODE_PRIVATE);
    }

    public SharedPrefsPersistor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void removeAll(List<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.remove(createCookieKey(cookie));
        }
        editor.apply();

    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void persist(Cookie cookie) {


    }

    @Override
    public void persistAll(Collection<Cookie> cookies) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
//            String encode = new CookiesSerializable().encode(cookie);
            String encode = JsonManager.myGson.toJson(cookie);
            XLog.d("fdx:cookiePersist: " + encode);
            editor.putString(createCookieKey(cookie), encode);
        }
        editor.apply();

    }


    private static String createCookieKey(Cookie cookie) {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    @Override
    public List<Cookie> loadAll() {

        List<Cookie> cookies = new ArrayList<>();

        Set<? extends Map.Entry<String, ?>> mapSet = sharedPreferences.getAll().entrySet();
        for (Map.Entry<String, ?> stringEntry : mapSet) {
            String strCookie = (String) stringEntry.getValue();
//            Cookie cookie = new CookiesSerializable().decode(strCookie);

            Cookie cookie = JsonManager.myGson.fromJson(strCookie, Cookie.class);
            cookies.add(cookie);
        }

        return cookies;
    }

    private Cookie getDecode(String strCookie) {
        return new CookiesSerializable().decode(strCookie);
    }
}
