package im.fdx.v2ex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.DetailsAdapter;
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.V2exJsonManager;

public class DetailsActivity extends Activity {

    public RecyclerView mRCView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DetailsAdapter mDetailsAdapter;

    TopicModel Header;
    ArrayList<ReplyModel> replyLists = new ArrayList<>();

    private String TopicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //处理传递过来的Intent，共两个数据
        Intent mGetIntent = getIntent();
        Header = mGetIntent.getParcelableExtra("model");
        TopicId = String.valueOf(mGetIntent.getLongExtra("topic_id", 1L));
        L.m(TopicId);

        GetReplyJson();
        mRCView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRCView.setLayoutManager(mLayoutManager);

        mDetailsAdapter = new DetailsAdapter(this,Header, replyLists);

        mRCView.setAdapter(mDetailsAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_details);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        GetReplyJson();
                    }
                });

            }

        });
    }



    public void GetReplyJson() {

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                V2exJsonManager.API_REPLIES+"?topic_id="+ TopicId, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                parseDetailJsonArray(response);
                L.m(String.valueOf(replyLists));
                mDetailsAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                handleVolleyError(error);//// TODO: 15-9-8 重构volleyerror.

            }
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }

    private void parseDetailJsonArray(JSONArray response) {

        if(response == null || response.length() == 0) {
            return;
        }

        long id;
        String author;
        String content;
        long created;
        int thanks;

        if(replyLists.size() == response.length()){
            L.m("no new reply");
            return;
        }
        replyLists.clear();


        try {
            for(int i = 0; i< response.length();i++) {

                JSONObject responseJSONObject = response.getJSONObject(i);

                id = responseJSONObject.optInt("id");
                author = responseJSONObject.optJSONObject("member").optString("username");
                content = responseJSONObject.optString("content");
                thanks = responseJSONObject.optInt("thanks");
                created = responseJSONObject.optLong("created");
                L.m(content+i);

                replyLists.add(new ReplyModel(id, content, thanks, created, author));
            }

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
