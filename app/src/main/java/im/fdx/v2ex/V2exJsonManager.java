package im.fdx.v2ex;

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
    public static final String HOT_JSON = "/topics/hot.json";
    public static final String LATEST_JSON = "/topics/latest.json";
    public static final String V2EX_LATEST = V2EX_API+LATEST_JSON;
}