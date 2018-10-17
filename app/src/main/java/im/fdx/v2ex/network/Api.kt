package im.fdx.v2ex.network

import android.util.Log
import com.google.gson.Gson
import im.fdx.v2ex.model.Data
import im.fdx.v2ex.model.Res
import okhttp3.*
import java.io.File
import java.io.IOException

object Api {

    val client = OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                        .newBuilder()
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
                        .header("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
                        .header("Cache-Control", "max-age=0")
                        .header("Origin", "https://sm.ms")
                        .header("Refer", "https://sm.ms/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.3 Safari/537.36")
                        .build()
                chain.proceed(request)
            }
            .build()

    fun uploadImage(path: String, fileName: String, callback: (Data?, Int) -> Unit) {

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("smfile", fileName, MultipartBody.create(MediaType.parse("image/*"), File(path)))
                .build()

        client.newCall(Request.Builder().url("https://sm.ms/api/upload?ssl=true").post(requestBody).build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                      callback(null, 2)
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val str = response?.body()?.string() ?: "-"
                        Log.e("fdx", str)

                        val res: Res = Gson().fromJson(str, Res::class.java)
                        if (res.code == "success") {
                          res.data?.let { callback(it, 0) }
                        } else {
                          callback(res.data, 1)
                        }
                    }
                })

    }

    /**
     * 还需想下逻辑，现在技术实现又遇到难题了
     */
    fun deleteImage(path: String, callback: (Data?, Int) -> Unit) {
        client.newCall(Request.Builder().url(path).build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
              callback(null, 2)
            }

            override fun onResponse(call: Call?, response: Response?) {
                val str = response?.body()?.string() ?: "-"
                val res: Res = Gson().fromJson(str, Res::class.java)
                if (res.code == "success") {
                  callback(null, 0)
                } else {
                  callback(null, 1)
                }
            }
        })
    }
}

