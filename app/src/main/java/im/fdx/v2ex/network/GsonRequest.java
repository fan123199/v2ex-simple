package im.fdx.v2ex.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by fdx on 16-1-19.
 * 由于Java的类型擦除，我不能用这个去获得List的Gson转换
 * @deprecated 不用Volley了
 */
public class GsonRequest<T> extends Request<T> {

    private final Gson gson = new Gson();
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    private Type mTTypeToken;

    public GsonRequest(int method, String url,Type type, Map<String, String> headers,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.headers = headers;
        this.listener = listener;
        this.mTTypeToken = type;
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Log.w("GsonRequest", "I get response");
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            T listFoo = gson.fromJson(json, mTTypeToken);
            return  Response.success(listFoo,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }


}

