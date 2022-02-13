package im.fdx.v2ex.network

import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.firebase.crashlytics.FirebaseCrashlytics
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.network.cookie.MyCookieJar
import im.fdx.v2ex.network.cookie.SharedPrefsPersistor
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.logi
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

object HttpHelper {

    val cookiePersistor = SharedPrefsPersistor(MyApp.get())
    val myCookieJar: MyCookieJar = MyCookieJar(cookiePersistor)

    val OK_CLIENT: OkHttpClient = OkHttpClient().newBuilder()
//                        .addInterceptor(HttpLoggingInterceptor())
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)  //禁止重定向
            .addNetworkInterceptor(
                        HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                            override fun log(message: String) {
                                logd("okhttp: $message")
                            }
                        }).apply { level = HttpLoggingInterceptor.Level.HEADERS }
                    )
            .addInterceptor(ChuckerInterceptor(MyApp.get()))//好东西，查看Okhttp数据
            .addInterceptor { chain ->
                val request = chain.request()
                        .newBuilder()
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                        .header("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
//                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7")
                        .header("Host", "www.v2ex.com")
                        .header("Authority", "v2ex.com")
                        .header("Cache-Control", "max-age=0")
//                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Mobile Safari/537.36")
                        //换回PC端的User-Agent, 因为一些奇怪的问题
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36")
                        .build()
                chain.proceed(request)
            }
            .cookieJar(myCookieJar)
            .build()

}

fun vCall(url: String): Call {
    Log.d("fdx", "vCall: $url ")
    return HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url).build())
}


fun Call.start(callback: Callback) {
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            try {
                callback.onFailure(call, e)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                callback.onResponse(call, response)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    })
}


@Deprecated("暂时还用不上，简化代码之用")
fun Call.start(onResp: (Call, Response) -> Unit, onFail: (Call, IOException)-> Unit){
    val callback = object: Callback {
        override fun onResponse(call: Call, response: Response) {
            onResp(call, response)
        }

        override fun onFailure(call: Call, e: IOException) {
            onFail(call , e)
        }
    }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            try {
                callback.onFailure(call, e)
            } catch (e2: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e2)
                e2.printStackTrace()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                callback.onResponse(call, response)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    })
}



