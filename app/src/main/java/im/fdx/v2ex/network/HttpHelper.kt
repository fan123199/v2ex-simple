package im.fdx.v2ex.network

import com.readystatesoftware.chuck.ChuckInterceptor
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.network.cookie.MyCookieJar
import im.fdx.v2ex.network.cookie.SharedPrefsPersistor
import okhttp3.*
import java.io.IOException

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

object HttpHelper {

    val myCookieJar: MyCookieJar = MyCookieJar(SharedPrefsPersistor(MyApp.get().applicationContext))
    val baseHeaders: Headers = Headers.Builder()
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .add("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
            .add("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            .add("Host", "www.v2ex.com")
            .add("Cache-Control", "max-age=0")
            //  .add("X-Requested-With", "com.android.browser")
            //  .add("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Mobile Safari/537.36");
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36")
            .build()

    val OK_CLIENT: OkHttpClient = OkHttpClient().newBuilder()
//                        .addInterceptor(HttpLoggingInterceptor())
            //            .connectTimeout(10, TimeUnit.SECONDS)
            //            .writeTimeout(10, TimeUnit.SECONDS)
            //            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)  //禁止重定向
            .addInterceptor(ChuckInterceptor(MyApp.get().applicationContext))//好东西，查看Okhttp数据
            .addInterceptor { chain ->
                val request = chain.request()
                        .newBuilder()
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
                        .header("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
                        .header("Host", "www.v2ex.com")
                        .header("Cache-Control", "max-age=0")
                        //  .header("X-Requested-With", "com.android.browser")
                        //  .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Mobile Safari/537.36");
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36")
                        .build()
                chain.proceed(request)
            }
            .cookieJar(myCookieJar)
            .build()

}

fun vCall(url: String): Call = HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).build())

fun Call.get(
        onResponse: (Call, Response) -> Unit,
        onFailure: (Call, Exception) -> Unit) {
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) = onFailure.invoke(call, e)
        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) = onResponse.invoke(call, response)
    })
}


