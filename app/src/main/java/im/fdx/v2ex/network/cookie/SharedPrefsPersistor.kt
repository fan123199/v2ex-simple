package im.fdx.v2ex.network.cookie

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.utils.extensions.loge
import im.fdx.v2ex.utils.extensions.logv
import okhttp3.Cookie
import java.util.*

/**
 * Created by fdx on 2017/3/16.

 * 保存cookie 到 preference
 */

class SharedPrefsPersistor : CookiePersistor {

    private var sharedPreferences: SharedPreferences

    constructor(context: Context) {
        this.sharedPreferences = context.getSharedPreferences("v2ex_cookie", Context.MODE_PRIVATE)
    }

    constructor(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }

    override fun removeAll(cookies: List<Cookie>) {
        val editor = sharedPreferences.edit()
        for (cookie in cookies) {
            editor.remove(createCookieKey(cookie))
        }
        editor.apply()

    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    override fun persist(cookie: Cookie) {

    }

    override fun persistAll(cookies: Collection<Cookie>) {
        val editor = sharedPreferences.edit()
        for (cookie in cookies) {
            val encode =  Gson().toJson(cookie)
            editor.putString(createCookieKey(cookie), encode)
        }
        editor.apply()

    }


    private fun createCookieKey(cookie: Cookie): String {
        return "${if (cookie.secure) "https" else "http"}://${cookie.domain}${cookie.path}|${cookie.name}"
    }


    @Deprecated("目前没有用到")
    override fun loadAll(): List<Cookie> {

        val cookies = ArrayList<Cookie>()

        val mapSet = sharedPreferences.all.entries
        for ((_, value) in mapSet) {
            val strCookie = value as String
            val cookie = NetManager.myGson.fromJson(strCookie, Cookie::class.java)
            cookies.add(cookie)
        }
        return cookies
    }


    fun loadByHost(url: String): List<Cookie> {
        val cookies = ArrayList<Cookie>()
        val mapSet = sharedPreferences.all.entries
        mapSet.forEach { (key, value) ->
            if (key.contains(url)) {
                val strCookie = value as String
//                logv("fdx2:$strCookie")
                val cookie = NetManager.myGson.fromJson(strCookie, Cookie::class.java)
                cookies.add(cookie)
            }
        }
        return cookies
    }
}
