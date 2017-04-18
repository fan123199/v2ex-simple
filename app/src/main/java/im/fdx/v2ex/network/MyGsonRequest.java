package im.fdx.v2ex.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;

import java.lang.reflect.Type;

/**
 * Created by a708 on 16-1-19.
 */
public class MyGsonRequest<T> extends GsonRequest<T> {

//    public MyGsonRequest(int method, String url,Class<TopicModel> clazz,
//                     Map<String, String> headers,
//                     Response.Listener<TopicModel> listener,
//                     Response.ErrorListener errorListener) {
//        super(method, url,  clazz, headers, listener, errorListener);
//    }

    public MyGsonRequest(String url, Type type,
                         Response.Listener<T> listener,
                         Response.ErrorListener errorListener) {
        super(Method.GET, url, type, null, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(NetManager.MY_TIMEOUT_MS,
                NetManager.MY_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

//    @Override
//    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
//
//        retryPolicy = new DefaultRetryPolicy(JsonManager.MY_TIMEOUT_MS,
//                JsonManager.MY_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        return this;
//    }
}
