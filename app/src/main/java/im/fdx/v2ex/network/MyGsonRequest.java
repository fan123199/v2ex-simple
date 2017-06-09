package im.fdx.v2ex.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;

import java.lang.reflect.Type;

/**
 * Created by a708 on 16-1-19.
 * @deprecated Volley组件
 */
public class MyGsonRequest<T> extends GsonRequest<T> {

    private static final int MY_MAX_RETRIES = 1;
    private static final int MY_TIMEOUT_MS = 10 * 1000;

    public MyGsonRequest(String url, Type type,
                         Response.Listener<T> listener,
                         Response.ErrorListener errorListener) {
        super(Method.GET, url, type, null, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(MY_TIMEOUT_MS,
                MY_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
