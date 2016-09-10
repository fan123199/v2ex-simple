package im.fdx.v2ex.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import im.fdx.v2ex.R;
import im.fdx.v2ex.utils.HintUI;

/**
 * Created by a708 on 15-8-13.
 * 用于对Json处理的类
 */

//BasicNetwork.logSlowRequests: HTTP response for request=<[ ]
//        https://www.v2ex.com/api/topics/latest.json 0xe61fa5b9 NORMAL 1>
//        // [lifetime=5462], [size=60681], [rc=200], [retryCount=0]

public class JsonManager {

    public static final String TAG = JsonManager.class.getSimpleName();


    public static final String V2EX_API = "https://www.v2ex.com/api";
    public static final String HTTPS_V2EX_BASE = "https://www.v2ex.com";
    public static final String HOT_JSON = "https://www.v2ex.com/api/topics/hot.json";
    public static final String LATEST_JSON = "https://www.v2ex.com/api/topics/latest.json";
    //以下,接受参数： name: 节点名
    public static final String NODE_JSON = "https://www.v2ex.com/api/nodes/show.json";

    public static final String SIGN_UP_URL = HTTPS_V2EX_BASE + "/signup";

    public static final String SIGN_IN_URL = HTTPS_V2EX_BASE + "/signin";
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    public static final String USER_JSON = "https://www.v2ex.com/api/members/show.json";
    //以下接收参数：
    //     topic_id: 主题ID
    // fdx_comment: 坑爹，官网没找到。怪不得没法子
    public static final String API_REPLIES = "https://www.v2ex.com/api/replies/show.json";

    public static final int MY_TIMEOUT_MS = 4000;

    public static final int MY_MAX_RETRIES = 1;

    public static void handleVolleyError(Context context,VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Log.e(TAG,context.getString(R.string.error_timeout));
            HintUI.t(context,context.getString(R.string.app_name) + ": " + context.getString(R.string.error_timeout));
        } else if (error instanceof AuthFailureError) {
            Log.e(TAG,context.getString(R.string.error_auth_failure));
            HintUI.t(context,context.getPackageName() + ": " + context.getString(R.string.error_auth_failure));

        } else if (error instanceof ServerError) {
            Log.e(TAG,context.getString(R.string.error_auth_failure));
            HintUI.t(context,context.getPackageName() + ": " + context.getString(R.string.error_auth_failure));

        } else if (error instanceof NetworkError) {
            Log.e(TAG,context.getString(R.string.error_network));
            HintUI.t(context, context.getPackageName() + ": " + context.getString(R.string.error_network));
        } else if (error instanceof ParseError) {
            Log.e(TAG,context.getString(R.string.error_parser));
            HintUI.t(context, context.getPackageName() + ": " + context.getString(R.string.error_parser));
        }
    }

    public static Gson myGson = new Gson();

//    /**
//     *  @param response
//     *        foo
//     * @param articleModel
//     * @param context
//     */
//    public static void handleJson(JSONArray response, ArrayList<TopicModel> articleModel, Context context) {
//        if (response == null || response.length() == 0) {
//            return;
//        }
//
//        try {
//            for (int i = 0; i < response.length(); i++) {
//                TopicModel tm = myGson.fromJson(response.getJSONObject(i).toString(), TopicModel.class);
//                articleModel.add(tm);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * @param context  登录的Context
     * @param username 用户名
     * @param password 密码
     */
    public static void Login(final Context context,final String username,final String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO: 15-9-17


                HintUI.t(context,response);
                Log.i(TAG,response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyError(context,error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("u",username);
                params.put("p", password);
                params.put("once", "1154");  // TODO: 15-9-17
                params.put("next","/");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
               Map<String,String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");


                return params;
            }
        };
        VolleyHelper.getInstance().addToRequestQueue(stringRequest);
    }
}