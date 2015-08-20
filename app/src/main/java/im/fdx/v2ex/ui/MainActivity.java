package im.fdx.v2ex.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.V2exJsonManager;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.MainAdapter;

public class MainActivity extends Activity {


    public Boolean TAG_REFRESH = false;
    RecyclerView mRecyclerView;
    MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;
    SwipeRefreshLayout mSwipeLayout;

    MySingleton mSingleton;//暂时不用,调试context
    public RequestQueue queue;

    private ArrayList<TopicModel> Top10 = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        mSingleton = MySingleton.getInstance(this);
        GetJson(TAG_REFRESH);

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        mAdapter = new MainAdapter(this);
        mRecyclerView.setAdapter(mAdapter); //大工告成
        L.m("显示成功");

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TAG_REFRESH = true;
                GetJson(TAG_REFRESH);
                L.m("success");
                mSwipeLayout.setRefreshing(false);
                TAG_REFRESH = false;
            }

        });
    }

    public void GetJson(boolean tag) {

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                V2exJsonManager.LATEST_JSON, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                parseJsonArray(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyError();
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }

    private void handleVolleyError() {
        L.m("false get json");
    }

    public void parseJsonArray(JSONArray response) {
        if(response == null || response.length() == 0) {
            return;
        }
        try {
            for(int i = 0; i< response.length();i++) {
            JSONObject responseJSONObject = response.getJSONObject(i);

                int id = responseJSONObject.optInt("id");
                String title = responseJSONObject.optString("title");
                String author = responseJSONObject.optJSONObject("member").optString("username");
                String content = responseJSONObject.optString("content");
                int replies = responseJSONObject.optInt("replies");
                String node_title = responseJSONObject.optJSONObject("node").optString("title");

                Top10.add(new TopicModel(id,title,author,content,replies,node_title));
            }
            mAdapter.setTopic(Top10);
            L.m("parse success");

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
    }
}
