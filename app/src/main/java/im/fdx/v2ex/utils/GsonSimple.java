package im.fdx.v2ex.utils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;

import java.lang.reflect.Type;

import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.JsonManager;

/**
 * Created by a708 on 16-1-19.
 */
public class GsonSimple<T> extends GsonRequest<T> {

//    public GsonSimple(int method, String url,Class<TopicModel> clazz,
//                     Map<String, String> headers,
//                     Response.Listener<TopicModel> listener,
//                     Response.ErrorListener errorListener) {
//        super(method, url,  clazz, headers, listener, errorListener);
//    }

    public GsonSimple(String url, Type t,
                      Response.Listener<T> listener,
                      Response.ErrorListener errorListener) {
        super(Method.GET, url, t, null, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(JsonManager.MY_TIMEOUT_MS,
                JsonManager.MY_MAX_RETRIES,
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
