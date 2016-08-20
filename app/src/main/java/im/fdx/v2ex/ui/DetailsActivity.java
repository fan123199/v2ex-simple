package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.DetailsAdapter;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.GsonSimple;
import im.fdx.v2ex.utils.L;

public class DetailsActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DetailsAdapter mDetailsAdapter;

    private TopicModel detailsHeader;
    private List<ReplyModel> replyLists = new ArrayList<>();

    RecyclerView mRCView;
//    private String topicId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        ActionBar mToolbar = getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //I add parentActivity in Manifest, so I do not need below code ? NEED
        if(toolbar !=null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                finish();
                    onBackPressed();
                }
            });
        }

        mRCView = (RecyclerView) findViewById(R.id.detail_recycler_view);
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        mLayoutManager.scrollToPosition(0);
//        mRCView.setLayoutManager(mLayoutManager);


        LinearLayoutManager mLayoutManager = new LinearLayoutManagerWithSmoothScroller(this);
        mRCView.setLayoutManager(mLayoutManager);
        mRCView.smoothScrollToPosition(0);
        //// TODO: 2016/8/14  这个Scroll 到顶部的bug，卡了我一个星期，用了So上的方法，自定义了一个LinearLayoutManager



        Intent mGetIntent = getIntent();
        detailsHeader = mGetIntent.getParcelableExtra("model");

        GetReplyData();
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
                        GetReplyData();
                    }
                });

            }

        });
    }


    public void GetReplyData() {
        L.m("hehe");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                JsonManager.API_REPLIES + "?topic_id=" + detailsHeader.getId(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null || response.length() == 0) {
                            return;
                        }

                        //时间会变化, so we do not need this.
//        if(replyLists.size() == response.length()){
////            L.m("no new reply");
//            return;
//        }
                        replyLists.clear();

                        Type typeofR = new TypeToken<ArrayList<ReplyModel>>() {
                        }.getType();
                        List<ReplyModel> jsonReply = JsonManager.myGson.fromJson(response.toString(), typeofR);
                        replyLists.addAll(jsonReply);

// 老方法，不够简洁。简洁当然目前也不够，todo: 改成Gson最好了

                        mDetailsAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JsonManager.handleVolleyError(DetailsActivity.this, error); // DONE: 15-9-8 重构volleyerror.
                        mSwipeRefreshLayout.setRefreshing(false);

                    }
                }
        );

        MySingleton.getInstance().addToRequestQueue(jsonArrayRequest);

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
                GetReplyData();
                break;
            case R.id.menu_item_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "来自V2EX的帖子： " + detailsHeader.getTitle() + "   "
                        + JsonManager.HTTPS_V2EX_BASE + "/t/" + detailsHeader.getId());
                sendIntent.setType("text/plain");
                // createChooser 中有三大好处，自定义title
                startActivity(Intent.createChooser(sendIntent, "分享到"));
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
