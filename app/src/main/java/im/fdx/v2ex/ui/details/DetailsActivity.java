package im.fdx.v2ex.ui.details;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.inputmethod.InputMethodManager;
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
import im.fdx.v2ex.model.BaseModel;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
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
    private static final int MSG_GO_TO_BOTTOM = 3;
    private SwipeRefreshLayout mSwipe;
    private ImageView ivSend;
    private EditText etReply;
    private DetailsAdapter mDetailsAdapter;
    private List<BaseModel> mAllContent = new ArrayList<>();
    RecyclerView mRCView;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            XLog.tag(TAG).d("get in lbc:" + intent.getAction());
            if (intent.getAction().equals("im.fdx.v2ex.event.login")) {
                invalidateOptionsMenu();
                addFootView();
            } else if (intent.getAction().equals("im.fdx.v2ex.event.logout")) {
                invalidateOptionsMenu();
                removeFootView();
            }
        }

    };

    private void removeFootView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.foot_container);
        linearLayout.setVisibility(View.GONE);
    }

    private void addFootView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.foot_container);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_OK_GET_TOPIC:
                    mSwipe.setRefreshing(false);
                    break;
                case MSG_ERROR_IO:
                    mSwipe.setRefreshing(false);
                    break;
                case MSG_ERROR_AUTH:
                    HintUI.t(DetailsActivity.this, "该主题需要登录查看");
                    DetailsActivity.this.finish();
                    break;
                case MSG_GO_TO_BOTTOM:
                    mRCView.scrollToPosition(mAllContent.size() - 1);

            }
            return false;
        }
    });
    private long mTopicId = -1L;
    private String once;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        IntentFilter filter = new IntentFilter("im.fdx.v2ex.event.login");

        filter.addAction("im.fdx.v2ex.event.logout");

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        if (MyApp.getInstance().isLogin()) {
            addFootView();
        } else {
            removeFootView();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar mToolbar = getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
        }

        //I add parentActivity in Manifest, so I do not need below code ? NONONONONO---NEEDED
//        if (toolbar != null) {
//            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onBackPressed();
//                }
//            });
//        }

        mRCView = (RecyclerView) findViewById(R.id.detail_recycler_view);
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        mLayoutManager.scrollToPosition(0);
//        mRCView.setLayoutManager(mLayoutManager);

        LinearLayoutManager mLayoutManager = new SmoothManager(this);
        mRCView.setLayoutManager(mLayoutManager);
        mRCView.smoothScrollToPosition(POSITION_START);
        //// 这个Scroll 到顶部的bug，卡了我一个星期，用了SO上的方法，自定义了一个LinearLayoutManager
        mRCView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && etReply.hasFocus()) {
//                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                    inputMethodManager.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
                    etReply.clearFocus();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
        mDetailsAdapter = new DetailsAdapter(DetailsActivity.this, mAllContent);
        mRCView.setAdapter(mDetailsAdapter);

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
        etReply.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                XLog.tag(TAG).d("hasFocus" + hasFocus);
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        etReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

//                XLog.tag(TAG).d(s.toString());
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

        mSwipe.setRefreshing(true);

        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            List<String> params = data.getPathSegments();
            if (scheme.equals("https") || scheme.equals("http")) {
                if (host.contains("v2ex.com")) {//不需要判断，在manifest中已指定
                    mTopicId = Long.parseLong(params.get(1));

                    getTopicAndReplyByOk(mTopicId, false);
                }
            }
        } else if (intent.getParcelableExtra("model") != null) {
            TopicModel topicModel = intent.getParcelableExtra("model");
            mTopicId = topicModel.getId();

            if (MyApp.getInstance().getHttpMode() == USE_API) {
                mAllContent.add(0, topicModel);
                getReplyByVolley();
            } else if (MyApp.getInstance().getHttpMode() == USE_WEB) {
                getTopicAndReplyByOk(mTopicId, false);

            }
        } else if (intent.getLongExtra(Keys.KEY_TOPIC_ID, -1L) != -1L) {
            mTopicId = intent.getLongExtra(Keys.KEY_TOPIC_ID, -1L);
            getTopicAndReplyByOk(mTopicId, false);
        }
        XLog.tag(TAG).d(mTopicId);


    }

    private void getTopicAndReplyByOk(final long topicId, final boolean scrolltoBottom) {
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
                BaseModel topicHeader = JsonManager.parseResponseToTopic(body, topicId);

                List<ReplyModel> replies = JsonManager.parseResponseToReplay(body);

                once = parseOnce(body);
                mAllContent.clear();
                mAllContent.add(topicHeader);
                mAllContent.addAll(replies);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDetailsAdapter.notifyDataSetChanged();
                        mSwipe.setRefreshing(false);
                        if (scrolltoBottom) {
                            handler.sendEmptyMessage(MSG_GO_TO_BOTTOM);
                        }

                    }
                });
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
            getTopicAndReplyByOk(mTopicId, false);
        }

    }

    @Deprecated
    private void getReplyByVolley() {
        Type typeofR = new TypeToken<ArrayList<ReplyModel>>() {
        }.getType();
        MyGsonRequest<ArrayList<ReplyModel>> replies = new MyGsonRequest<>(JsonManager.API_REPLIES + "?topic_id="
                + mTopicId, typeofR, new Response.Listener<ArrayList<ReplyModel>>() {
            @Override
            public void onResponse(ArrayList<ReplyModel> response) {
                XLog.tag(TAG).i(TAG, "GSON DONE: " + ((response == null || response.isEmpty()) ? 0 : response.get(0).toString()));
                if (response == null || response.size() == 0) {
                    mSwipe.setRefreshing(false);
                    XLog.tag(TAG).d(TAG, "no response got");
                    HintUI.t(DetailsActivity.this, "无法获取回复");
                    return;
                }

                mAllContent.clear();
                mAllContent.addAll(1, response);
//                replyLists.clear();
//                replyLists.addAll(response);

                mDetailsAdapter.notifyDataSetChanged();
                mSwipe.setRefreshing(false);
                XLog.tag(TAG).d(TAG, "done with details");
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, "来自V2EX的帖子： " + ((TopicModel) mAllContent.get(0)).getTitle() + "   "
                        + JsonManager.HTTPS_V2EX_BASE + "/t/" + ((TopicModel) mAllContent.get(0)).getId());
                sendIntent.setType("text/plain");
                // createChooser 中有三大好处，自定义title
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                break;
            case R.id.menu_item_open_in_browser:

                Long topicId = ((TopicModel) mAllContent.get(0)).getId();
                String url = JsonManager.HTTPS_V2EX_BASE + "/t/" + topicId;
                Uri uri = Uri.parse(url);
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
        etReply.clearFocus();
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintUI.t(DetailsActivity.this, "未知原因，回复失败");
                    }
                });

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() == 302) {
                    XLog.tag(TAG).d("成功发布");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            etReply.setText("");


                        }
                    });
                    getTopicAndReplyByOk(mTopicId, true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
