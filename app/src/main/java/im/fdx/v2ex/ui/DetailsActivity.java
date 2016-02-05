package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.DetailsAdapter;
import im.fdx.v2ex.network.JsonManager;

public class DetailsActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DetailsAdapter mDetailsAdapter;

    private TopicModel detailsHeader;
    private ArrayList<ReplyModel> replyLists = new ArrayList<>();

//    private String topicId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
//        我认为下面这段应该在theme中完成
        ActionBar mToolbar = getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //I add parentAcitity in Manifest, so I do not need below code ?
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                onBackPressed();
            }
        });

        //处理传递过来的Intent，共一个数据
        Intent mGetIntent = getIntent();
        detailsHeader = mGetIntent.getParcelableExtra("model");
//        topicId = String.valueOf(mGetIntent.getLongExtra("topic_id", 1L));
//        L.m(topicId);

        GetReplyJson();
        RecyclerView mRCView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
//        // 2015/9/15  I want space!! fdx： solved in adapter
//
//        RecyclerView.LayoutParams params =
//                new RecyclerView.LayoutParams(
//                        RecyclerView.LayoutParams.WRAP_CONTENT,
//                        RecyclerView.LayoutParams.WRAP_CONTENT);
//        params.setMargins(0,0,0,30);
//        mLayoutManager.findViewByPosition(0).setLayoutParams();
        mRCView.setLayoutManager(mLayoutManager);

        mDetailsAdapter = new DetailsAdapter(this, detailsHeader, replyLists);
        mRCView.setAdapter(mDetailsAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_details);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
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
                JsonManager.API_REPLIES + "?topic_id=" + detailsHeader.getId(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                parseDetailJsonArray(response);
//                L.m(String.valueOf(replyLists));
                mDetailsAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JsonManager.handleVolleyError(DetailsActivity.this,error); // DONE: 15-9-8 重构volleyerror.
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });

        MySingleton.getInstance().addToRequestQueue(jsonArrayRequest);

    }

    private void parseDetailJsonArray(JSONArray response) {

        if(response == null || response.length() == 0) {
            return;
        }

        if(replyLists.size() == response.length()){
//            L.m("no new reply");
            return;
        }
        replyLists.clear();
//        Gson myGson = new Gson();
        try {
            for (int i = 0; i < response.length(); i++) {

                ReplyModel tm = JsonManager.myGson.fromJson(response.getJSONObject(i).toString(), ReplyModel.class);
//                L.t(context, tm.getTitle());
                replyLists.add(tm);
            }
        } catch (JSONException e) {
//            L.m("parse false");
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

        //SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mSwipeRefreshLayout.setRefreshing(true);
                GetReplyJson();
                break;
            case R.id.menu_item_share:
//                L.t(this,"分享到");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "来自V2EX的帖子： " + detailsHeader.getTitle() + "   "
                        + JsonManager.HTTP_V2EX_BASE + "/t/" + detailsHeader.getId());
                sendIntent.setType("text/plain");
                // createChooser 中有三大好处，自定义title
                startActivity(Intent.createChooser(sendIntent,"分享到"));
                break;
            case R.id.menu_item_open_in_browser:
                Uri uri = Uri.parse(detailsHeader.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
