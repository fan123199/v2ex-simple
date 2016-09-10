package im.fdx.v2ex.ui.details;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.GsonSimple;
import im.fdx.v2ex.utils.SmoothManager;

public class DetailsActivity extends AppCompatActivity {

    private String TAG = DetailsActivity.class.getSimpleName();
    private SwipeRefreshLayout mSwipe;

    private DetailsAdapter mDetailsAdapter;
    private TopicModel detailsHeader;

    private List<ReplyModel> replyLists = new ArrayList<>();
    RecyclerView mRCView;
//    private String topicId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar mToolbar = getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //I add parentActivity in Manifest, so I do not need below code ? NEED
        if (toolbar != null) {
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


        LinearLayoutManager mLayoutManager = new SmoothManager(this);
        mRCView.setLayoutManager(mLayoutManager);
        mRCView.smoothScrollToPosition(0);
        //// TODO: 2016/8/14  这个Scroll 到顶部的bug，卡了我一个星期，用了So上的方法，自定义了一个LinearLayoutManager


        Intent mGetIntent = getIntent();
        detailsHeader = mGetIntent.getParcelableExtra("model");
        mSwipe = (SwipeRefreshLayout) findViewById(R.id.swipe_details);

        mSwipe.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        GetReplyData();
        mSwipe.setRefreshing(true);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int start = (int) (40 * metrics.density);
        mSwipe.setProgressViewOffset(false, -start, (int) (start*1.5));
        mDetailsAdapter = new DetailsAdapter(this, detailsHeader, replyLists);
        mRCView.setAdapter(mDetailsAdapter);


    }


    public void GetReplyData() {
        Type typeofR = new TypeToken<ArrayList<ReplyModel>>() {
        }.getType();
        GsonSimple<ArrayList<ReplyModel>> d = new GsonSimple<>(JsonManager.API_REPLIES + "?topic_id="
                + detailsHeader.getId(),typeofR, new ArrayListListener(), new MyErrorListener());
        VolleyHelper.getInstance().addToRequestQueue(d);

        //GsonRequest改造成功，弃用这个
//        VolleyHelper.getInstance().addToRequestQueue(jsonArrayRequest);

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
                mSwipe.setRefreshing(true);
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

    private class ArrayListListener implements Response.Listener<ArrayList<ReplyModel>> {
        @Override
        public void onResponse(ArrayList<ReplyModel> response) {
            Log.i(TAG, "GSON DONE: "+ ((response ==null||response.isEmpty())?0:response.get(0).toString()));
            if (response == null || response.size() == 0) {
                mSwipe.setRefreshing(false);
                return;
            }
            replyLists.clear();

            replyLists.addAll(response);

            mDetailsAdapter.notifyDataSetChanged();
            Log.d(TAG,"done with details");
            mSwipe.setRefreshing(false);

        }
    }

    private class MyErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            JsonManager.handleVolleyError(getApplicationContext(),error);
            mSwipe.setRefreshing(false);
        }
    }
}
