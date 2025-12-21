package im.fdx.v2ex.data.network.cookie

import okhttp3.Cookie

/**
 * Created by fdx on 2017/3/16.
 */

interface CookiePersistor {

    fun removeAll(cookies: List<Cookie>)

    fun clear()

    fun persist(cookie: Cookie)

    fun persistAll(cookies: Collection<Cookie>)

    fun loadAll(): List<Cookie>


}

