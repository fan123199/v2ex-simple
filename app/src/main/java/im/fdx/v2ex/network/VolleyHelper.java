package im.fdx.v2ex.network;



import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import im.fdx.v2ex.MyApplication;

/**
 * Created by fdx on 2015/8/14.
 * Volley网络库的请求队列。使用单例，增加资源利用
 */
public class VolleyHelper {

    private static VolleyHelper mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Context mCtx;

    //带参数context, 与ytb教程不同
    private VolleyHelper() {
//        mCtx = context;
//        mRequestQueue = Volley.newRequestQueue(MyApplication.getInstance());
//        以上参数是没有传入context的构造函数的
        mRequestQueue = getRequestQueue();


        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {

            int cacheSize = 4 * 1024; //4K
            private final LruCache<String, Bitmap>
                    cache = new LruCache<>(cacheSize);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    //带参数context, 与ytb教程不同
    public static synchronized VolleyHelper getInstance() {
        if(mInstance ==  null){
            mInstance = new VolleyHelper();
        }

        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            //防止mCtx只是activity的context
//            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            mRequestQueue = Volley.newRequestQueue(MyApplication.getInstance());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
