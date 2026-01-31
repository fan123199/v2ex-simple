package im.fdx.v2ex.data.network

import android.util.Log
import com.google.gson.Gson
import im.fdx.v2ex.data.model.Data
import im.fdx.v2ex.data.model.Res
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import okhttp3.RequestBody.Companion.asRequestBody


object Api {

    val client = OkHttpClient().newBuilder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("Authorization", "14ac5499cfdd2bb2859e4476d2e5b1d2bad079bf")
                .build()
            chain.proceed(request)
        }
        .build()

    fun uploadImage(path: String, fileName: String, callback: (Data?, Int) -> Unit) {

        val requestBody = MultipartBody.Builder()
            .addFormDataPart("smfile", fileName, (File(path)).asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        client.newCall(
            Request.Builder()
                .url("https://sm.ms/api/v2/upload")
                .post(requestBody)
                .build()
        )
            .start(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(null, 2)
                }

                override fun onResponse(call: Call, response: Response) {
                    val str = response.body.string()
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
            override fun onFailure(call: Call, e: IOException) {
                callback(null, 2)
            }

            override fun onResponse(call: Call, response: Response) {
                val str = response.body.string()
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



