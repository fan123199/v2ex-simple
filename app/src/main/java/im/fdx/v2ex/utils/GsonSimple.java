package im.fdx.v2ex.utils;

import com.android.volley.Response;

import im.fdx.v2ex.model.TopicModel;

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

    public GsonSimple(String url, Class<T> clazz,
                      Response.Listener<T> listener,
                      Response.ErrorListener errorListener) {
        super(Method.GET, url, clazz, null, listener, errorListener);
    }
}
