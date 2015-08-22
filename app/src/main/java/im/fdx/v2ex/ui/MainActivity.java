package im.fdx.v2ex.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

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
        GetJson();

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        mAdapter = new MainAdapter(this);
        mAdapter.setTopic(Top10);
        mRecyclerView.setAdapter(mAdapter); //大工告成
        L.m("显示成功");

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        GetJson();
                        L.m("Thread done");
                    }
                });

            }

        });
    }

    public void GetJson() {

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
        long id;
        String title;
        String author;
        String content;
        int replies;
        String node_title;
        try {
            for(int i = 0; i< response.length();i++) {
                JSONObject responseJSONObject = response.getJSONObject(i);


                id = responseJSONObject.optInt("id");

                title = responseJSONObject.optString("title");

                author = responseJSONObject.optJSONObject("member").optString("username");

                content = responseJSONObject.optString("content");

                replies = responseJSONObject.optInt("replies");

                node_title = responseJSONObject.optJSONObject("node").optString("title");

                if(id == Top10.get(0).id) break;

                Top10.add(new TopicModel(id, title, author, content, replies, node_title));
            }

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
        L.m(String.valueOf(Top10));
        mAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mSwipeLayout.setRefreshing(true);
                GetJson();
                return true;
            case R.id.menu_settings:
                L.t(this.getApplicationContext(), "choose settings");
        }
        return super.onOptionsItemSelected(item);
    }
}
