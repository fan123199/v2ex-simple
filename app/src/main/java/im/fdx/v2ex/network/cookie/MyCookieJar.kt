package im.fdx.v2ex.network.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.*

/**
 * Created by fdx on 2017/3/16.
 *
 *
 * 请用自己的Cookie策略，而不是开源库。
 */
class MyCookieJar(private val cookiePersistor: SharedPrefsPersistor) : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()
    private val mCookies = ArrayList<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        mCookies.clear()
        mCookies.addAll(cookies)
        cookieStore[url.host] = mCookies
        cookiePersistor.persistAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {

        val cookies: List<Cookie>? = if (cookiePersistor.loadByHost(url.host).isNotEmpty()) {
            cookiePersistor.loadByHost(url.host.removePrefix("www."))
        } else
            cookieStore[url.host]
        return cookies ?: ArrayList()
    }

    fun clear() {
        mCookies.clear()
        cookieStore.clear()
        cookiePersistor.clear()
    }
}
