package im.fdx.v2ex.utils;

import android.app.Activity;
import android.content.Context;
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

import im.fdx.v2ex.network.MySingleton;

/**
 * Created by a708 on 15-8-13.
 */

public class V2exJsonManager{

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

}