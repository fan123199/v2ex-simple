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
class MyCookieJar(private val cookiePersistor: CookiePersistor) : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()
    private val mCookies = ArrayList<Cookie>()

    init {
        mCookies.addAll(cookiePersistor.loadAll())
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        mCookies.clear()
        mCookies.addAll(cookies)
        cookiePersistor.persistAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {

        val cookies: List<Cookie>?
        if (cookiePersistor.loadAll().isNotEmpty()) {
            cookies = cookiePersistor.loadAll()
        } else
            cookies = cookieStore[url.host()]
        return cookies ?: ArrayList<Cookie>()
    }


    fun clear() {
        mCookies.clear()
        cookiePersistor.clear()
    }
}
