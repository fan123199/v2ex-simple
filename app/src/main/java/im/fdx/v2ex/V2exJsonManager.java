package im.fdx.v2ex;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by a708 on 15-8-13.
 */

public class V2exJsonManager extends Activity {

    private TextView mTxtDisplay;
    public static final String V2exJson_URL = "http://www.v2ex.com/api";
    public static final String hotJson = "/topics/latest.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.testjson);

        getJsonVolley();
    }

    public void getJsonVolley() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());// 请求队列
        /**
         * method:请求方式 url:请求地址 listener:正确监听事件 errorListener:错误监听事件
         */
        // JsonArray请求
        String JsonDataUrl = V2exJson_URL+hotJson;
        JsonArrayRequest jsV2exHot = new JsonArrayRequest(
                Request.Method.GET,JsonDataUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        System.out.println("请求成功:" + jsonArray);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求失败:" + error);
            }
        });
        jsV2exHot.setTag("JSON");
        requestQueue.add(jsV2exHot);
    }

}