package im.fdx.v2ex.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;

/**
 * Created by a708 on 15-8-13.
 * 用于对Json处理的类
 */

public class JsonManager {


    public static final String V2EX_API = "http://www.v2ex.com/api";
    public static final String HTTP_V2EX_BASE = "http://www.v2ex.com";
    public static final String HOT_JSON = "http://www.v2ex.com/api/topics/hot.json";
    public static final String LATEST_JSON = "http://www.v2ex.com/api/topics/latest.json";
    //以下,接受参数： name: 节点名
    public static final String NODE_JSON = "https://www.v2ex.com/api/nodes/show.json";


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
            //TODO
        } else if (error instanceof AuthFailureError) {
            L.m(context.getString(R.string.error_auth_failure));

        } else if (error instanceof ServerError) {
            L.m(context.getString(R.string.error_auth_failure));

        } else if (error instanceof NetworkError) {
            L.m(context.getString(R.string.error_network));

        } else if (error instanceof ParseError) {
            L.m(context.getString(R.string.error_parser));

        }
    }

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
}