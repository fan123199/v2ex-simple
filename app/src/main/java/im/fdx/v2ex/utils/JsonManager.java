package im.fdx.v2ex.utils;

import android.content.Context;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;

/**
 * Created by a708 on 15-8-13.
 * 用于对Json处理的类
 */

public class JsonManager {


    public static final String V2EX_API = "https://www.v2ex.com/api";
    public static final String HTTP_V2EX_BASE = "https://www.v2ex.com";
    public static final String HOT_JSON = "https://www.v2ex.com/api/topics/hot.json";
    public static final String LATEST_JSON = "https://www.v2ex.com/api/topics/latest.json";
    //以下,接受参数： name: 节点名
    public static final String NODE_JSON = "https://www.v2ex.com/api/nodes/show.json";

    public static final String SIGN_UP_URL = HTTP_V2EX_BASE + "/signup";

    public static final String SIGN_IN_URL = HTTP_V2EX_BASE + "/signin";
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    public static final String USER_JSON = "https://www.v2ex.com/api/members/show.json";
    //以下接收参数：
    //     topic_id: 主题ID
    // fdx_comment: 坑爹，官网没找到。怪不得没法子
    public static final String API_REPLIES = "https://www.v2ex.com/api/replies/show.json";

    public static void handleVolleyError(Context context,VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            L.m(context.getString(R.string.error_timeout));
            L.t(context.getApplicationContext(),"连接超时，请重试");
        } else if (error instanceof AuthFailureError) {
            L.m(context.getString(R.string.error_auth_failure));

        } else if (error instanceof ServerError) {
            L.m(context.getString(R.string.error_auth_failure));

        } else if (error instanceof NetworkError) {
            L.m(context.getString(R.string.error_network));
            L.t(context.getApplicationContext(), "网络错误，请检查网络连接后重试");
        } else if (error instanceof ParseError) {
            L.m(context.getString(R.string.error_parser));
        }
    }


    /**
     *
     * @param response
     *        foo
     * @param articleModel
     *        bar
     */
    public static void handleJson(JSONArray response, ArrayList<TopicModel> articleModel) {
        if(response == null || response.length() == 0) {
            return;
        }
        long id;
        String title;
        String author;
        String content;
        int replies;
        String node_title;
        long created;
        String avatarString;

        boolean flag = true;

        if(articleModel.isEmpty()) {
            flag = false;
        }

//        L.m(String.valueOf(flag));

        try {
            for(int i = 0; i< response.length();i++) {

                JSONObject responseJSONObject = response.getJSONObject(i);

                id = responseJSONObject.optInt("id");
                title = responseJSONObject.optString("title");
                author = responseJSONObject.optJSONObject("member").optString("username");
                content = responseJSONObject.optString("content");
                replies = responseJSONObject.optInt("replies");
                node_title = responseJSONObject.optJSONObject("node").optString("title");
                created = responseJSONObject.optLong("created");
                avatarString = "http:"+ responseJSONObject.optJSONObject("member").optString("avatar_normal");

                if(flag) {
                    if (id == articleModel.get(i).id) {
                        break;
                    }
                }

                articleModel.add(i, new TopicModel(id, title, author, content, replies, node_title, created, avatarString));
            }

        } catch (JSONException e) {
//            L.m("parse false");
            e.printStackTrace();
        }
    }



    public static void Login(final Context context,final String username,final String password){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO: 15-9-17
                L.t(context,response);
                L.m(response);
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
//                params.put("once",)  // TODO: 15-9-17
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
        MySingleton.getInstance().addToRequestQueue(stringRequest);
    }
}