package im.fdx.v2ex.network


import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso

import java.io.IOException

import im.fdx.v2ex.MyApp


@Deprecated(" Created by fdx on 2015/8/14.\n  Volley网络库的请求队列。使用单例，增加资源利用")
class VolleyHelper//带参数context, 与ytb教程不同
private constructor() {
    private var mRequestQueue: RequestQueue
    val imageLoader: ImageLoader
    private val mCtx: Context? = null

    init {
        //        mCtx = context;
        //        mRequestQueue = Volley.newRequestQueue(MyApp.getInstance());
        //        以上参数是没有传入context的构造函数的
        mRequestQueue = requestQueue


        imageLoader = ImageLoader(mRequestQueue, object : ImageLoader.ImageCache {

            internal var cacheSize = 4 * 1024 //4K
            private val cache = LruCache<String, Bitmap>(cacheSize)

            override fun getBitmap(url: String): Bitmap {
                return cache.get(url)
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        })
    }

    private // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            //防止mCtx只是activity的context
            //            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(MyApp.getInstance())
            }

            return mRequestQueue
        }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    /**
     * Created by fdx on 2017/3/15.
     * fdx will maintain it
     */

    object MyImageLoader {

        fun load(context: Context, url: String, imageView: ImageView) {
            Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView)
        }

        fun load(context: Context, url: String): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = Picasso.with(context).load(url).resize(300, 300).get()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return bitmap

        }

    }

//    companion object {
//
//        private var mInstance: VolleyHelper? = null
//
//        //带参数context, 与ytb教程不同
//        val instance: VolleyHelper
//            @Synchronized get() {
//                if (mInstance == null) {
//                    mInstance = VolleyHelper()
//                }
//
//                return mInstance
//            }
//    }
}
