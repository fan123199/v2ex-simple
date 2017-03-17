package im.fdx.v2ex.ui.details;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.MyGsonRequest;
import im.fdx.v2ex.utils.SmoothManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import static im.fdx.v2ex.MyApp.USE_WEB;
import static im.fdx.v2ex.MyApp.USE_API;
import static im.fdx.v2ex.network.HttpHelper.OK_CLIENT;
import static im.fdx.v2ex.network.HttpHelper.baseHeaders;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class DetailsActivity extends AppCompatActivity {


    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final int POSITION_START = 0;
    private static final int MSG_OK_GET_TOPIC = 0;
    private static final int MSG_ERROR_AUTH = 1;
    private static final int MSG_ERROR_IO = 2;
    private SwipeRefreshLayout mSwipe;
    private ImageView ivSend;
    private EditText etReply;
    private DetailsAdapter mDetailsAdapter;
    private TopicModel mTopicHeader;
    private List<ReplyModel> replyLists = new ArrayList<>();
    RecyclerView mRCView;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("im.fdx.v2ex.event.login")) {
                invalidateOptionsMenu();
                addFootView();
            }
        }

    };

    private void addFootView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.foot_container);
        linearLayout.setVisibility(View.GONE);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_OK_GET_TOPIC:
                    mSwipe.setRefreshing(false);
                    mDetailsAdapter = new DetailsAdapter(DetailsActivity.this, mTopicHeader, replyLists);
                    mRCView.setAdapter(mDetailsAdapter);

                    break;
                case MSG_ERROR_IO:
                    mSwipe.setRefreshing(false);
                    break;
                case MSG_ERROR_AUTH:
                    HintUI.t(DetailsActivity.this, "该主题需要登录查看");
                    DetailsActivity.this.finish();
                    break;

            }
            return false;
        }
    });
    private long mTopicId;
    private String once;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar mToolbar = getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //I add parentActivity in Manifest, so I do not need below code ? NEED
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
        mRCView.smoothScrollToPosition(POSITION_START);
        //// 这个Scroll 到顶部的bug，卡了我一个星期，用了SO上的方法，自定义了一个LinearLayoutManager

        mSwipe = (SwipeRefreshLayout) findViewById(R.id.swipe_details);
        mSwipe.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReplyData();
            }

        });


        etReply = (EditText) findViewById(R.id.et_post_reply);

        etReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                XLog.tag(TAG).d(s.toString());
                if (TextUtils.isEmpty(s)) {
                    ivSend.setClickable(false);
                    ivSend.setImageResource(R.drawable.ic_send_unable);

                } else {
                    ivSend.setClickable(true);
                    ivSend.setImageResource(R.drawable.ic_send_enable);

                }
            }
        });
        ivSend = (ImageView) findViewById(R.id.iv_send);


        parseIntent(getIntent());

        //以下是设置刷新按钮的位置，暂时不用
//        final DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int start = (int) (40 * metrics.density);
//        mSwipe.setProgressViewOffset(false, -start, (int) (start*1.5));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    private void parseIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            List<String> params = data.getPathSegments();
            if (scheme.equals("https") || scheme.equals("http")) {
                if (host.contains("v2ex.com")) {//不需要判断，在manifest中已指定
                    mTopicId = Long.parseLong(params.get(1));
                    getTopicAndReplyByOk(mTopicId);
                }
            }
        } else {
            TopicModel topicModel = intent.getParcelableExtra("model");
            mTopicId = topicModel.getId();
            mSwipe.setRefreshing(true);
            if (MyApp.getInstance().getHttpMode() == USE_API) {
                mTopicHeader = topicModel;

                mDetailsAdapter = new DetailsAdapter(DetailsActivity.this, mTopicHeader, replyLists);
                mRCView.setAdapter(mDetailsAdapter);

                getReplyByVolley();
            } else if (MyApp.getInstance().getHttpMode() == USE_WEB) {
                getTopicAndReplyByOk(mTopicId);

            }
        }


    }

    private void getTopicAndReplyByOk(final long topicId) {
        OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(JsonManager.HTTPS_V2EX_BASE + "/t/" + topicId)
                .build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(MSG_ERROR_IO);
                XLog.tag("DetailsActivity").d("failed " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                //权限问题，需要登录
                if (response.code() == 302) {
                    handler.sendEmptyMessage(MSG_ERROR_AUTH);
                    return;
                }

                String bodyStr = response.body().string();

                Element body = Jsoup.parse(bodyStr);
                mTopicHeader = JsonManager.parseResponseToTopic(body, topicId);

                List<ReplyModel> replies = JsonManager.parseResponseToReplay(body);

                once = parseOnce(body);
                replyLists.clear();
                replyLists.addAll(replies);
                handler.sendEmptyMessage(MSG_OK_GET_TOPIC);
            }

        });
    }

    private String parseOnce(Element body) {

        Element onceElement = body.getElementsByAttributeValue("name", "once").first();
        if (onceElement != null) {
            return onceElement.attr("value");
        }

        return null;
    }

    private void getReplyData() {

        if (MyApp.getInstance().getHttpMode() == USE_API) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getReplyByVolley();
                }
            }).start();

        } else if (MyApp.getInstance().getHttpMode() == USE_WEB) {
            getTopicAndReplyByOk(mTopicId);
        }

    }

    private void getReplyByVolley() {
        Type typeofR = new TypeToken<ArrayList<ReplyModel>>() {
        }.getType();
        MyGsonRequest<ArrayList<ReplyModel>> replies = new MyGsonRequest<>(JsonManager.API_REPLIES + "?topic_id="
                + mTopicHeader.getId(), typeofR, new Response.Listener<ArrayList<ReplyModel>>() {
            @Override
            public void onResponse(ArrayList<ReplyModel> response) {
                Log.i(TAG, "GSON DONE: " + ((response == null || response.isEmpty()) ? 0 : response.get(0).toString()));
                if (response == null || response.size() == 0) {
                    mSwipe.setRefreshing(false);
                    Log.d(TAG, "no response got");
                    HintUI.t(DetailsActivity.this, "无法获取回复");
                    return;
                }
                replyLists.clear();
                replyLists.addAll(response);

                mSwipe.setRefreshing(false);
                mDetailsAdapter.notifyDataSetChanged();
                Log.d(TAG, "done with details");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JsonManager.handleVolleyError(getApplicationContext(), error);
                mSwipe.setRefreshing(false);
            }
        });
        VolleyHelper.getInstance().addToRequestQueue(replies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply, menu);


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reply:
//                postReply();


                break;
            case R.id.menu_refresh:
                mSwipe.setRefreshing(true);
                getReplyData();
                break;
            case R.id.menu_item_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "来自V2EX的帖子： " + mTopicHeader.getTitle() + "   "
                        + JsonManager.HTTPS_V2EX_BASE + "/t/" + mTopicHeader.getId());
                sendIntent.setType("text/plain");
                // createChooser 中有三大好处，自定义title
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                break;
            case R.id.menu_item_open_in_browser:
                Uri uri = Uri.parse(mTopicHeader.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.android.chrome");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    intent.setPackage(null);
                    startActivity(intent);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void postReply(View view) {
        XLog.tag(TAG).d("I clicked");
        final String content = etReply.getText().toString();
        RequestBody requestBody = new FormBody.Builder()
                .add("content", content)
                .add("once", once)
                .build();


        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(baseHeaders)
                .header("Origin", JsonManager.HTTPS_V2EX_BASE)
                .header("Referer", JsonManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(JsonManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .post(requestBody)
                .build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() == 302) {
                    XLog.tag(TAG).d("成功发布");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getReplyData();
                            etReply.setText("");
                            etReply.clearFocus();
                        }
                    });
                }
            }
        });
    }
}
