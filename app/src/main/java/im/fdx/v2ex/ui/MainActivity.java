package im.fdx.v2ex.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.V2exJsonManager;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.MainRecyclerViewAdapter;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.L;

public class MainActivity extends Activity {


    RecyclerView mRecyclerView;
    MainRecyclerViewAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;

    MySingleton mSingleton;
    private ArrayList<TopicModel> Top10 = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSingleton = MySingleton.getInstance(this);

        SendJsonArray();

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview

        //将要显示的数据mydataset传入MainRecyclerViewAdapter,生成一个我们能用的mAdapter
        mAdapter = new MainRecyclerViewAdapter(this);
        mAdapter.setTopic(Top10);

        //然后显示.大工告成
        mRecyclerView.setAdapter(mAdapter);
        L.m("成功显示");
    }

    public void SendJsonArray() {
        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET, V2exJsonManager.V2EX_LATEST, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                parseJsonArray(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mSingleton.addToRequestQueue(jsonArrayRequest);
    }

    public void parseJsonArray(JSONArray response) {
        if(response == null || response.length() == 0) {
            return;
        }
        try {
            String test = "";
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
