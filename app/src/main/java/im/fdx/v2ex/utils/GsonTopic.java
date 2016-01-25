package im.fdx.v2ex.utils;

import com.android.volley.Response;

import im.fdx.v2ex.model.TopicModel;

/**
 * Created by a708 on 16-1-19.
 */
public class GsonTopic extends GsonRequest<TopicModel> {

//    public GsonTopic(int method, String url,Class<TopicModel> clazz,
//                     Map<String, String> headers,
//                     Response.Listener<TopicModel> listener,
//                     Response.ErrorListener errorListener) {
//        super(method, url,  clazz, headers, listener, errorListener);
//    }

    public GsonTopic(String url, Class<TopicModel> clazz,
                     Response.Listener<TopicModel> listener,
                     Response.ErrorListener errorListener) {
        super(Method.GET, url, clazz, null, listener, errorListener);
    }
}
