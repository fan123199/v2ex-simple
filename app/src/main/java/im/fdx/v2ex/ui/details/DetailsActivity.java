package im.fdx.v2ex.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.GsonSimple;
import im.fdx.v2ex.utils.SmoothManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static im.fdx.v2ex.network.OkHttpHelper.baseRequestBuilder;
import static im.fdx.v2ex.network.OkHttpHelper.okHttpClient;

public class DetailsActivity extends AppCompatActivity {


    private final int MSG_OK_GET_TOPIC = 0;
    private String TAG = DetailsActivity.class.getSimpleName();
    private SwipeRefreshLayout mSwipe;

    private DetailsAdapter mDetailsAdapter;
    private TopicModel mDetailsMainContent;

    private List<ReplyModel> replyLists = new ArrayList<>();
    RecyclerView mRCView;
//    private String topicId;


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == MSG_OK_GET_TOPIC) {
                mDetailsMainContent = (TopicModel) msg.obj;
                getReplyData();
                mDetailsAdapter = new DetailsAdapter(DetailsActivity.this, mDetailsMainContent, replyLists);
                mRCView.setAdapter(mDetailsAdapter);

            }
            return false;
        }
    });


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
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        getReplyData();
                    }
                });

            }

        });

        Intent mGetIntent = getIntent();
        Uri data = mGetIntent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            List<String> params = data.getPathSegments();
            if (scheme.equals("https") || scheme.equals("http")) {
                if (host.contains("v2ex.com")) {//不需要判断，在manifest中已指定
                    final int topicId;
                    topicId = Integer.parseInt(params.get(1));
                    final Request request = baseRequestBuilder
                            .url(data.toString())
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            XLog.d("failed " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, okhttp3.Response response) throws IOException {


                            TopicModel topicModel = parseResponse(response, topicId);


                            Message.obtain(handler, MSG_OK_GET_TOPIC, topicModel).sendToTarget();

                        }
                    });

                }
            }
        } else {
            mDetailsMainContent = mGetIntent.getParcelableExtra("model");
            getReplyData();
            mSwipe.setRefreshing(true);
            mDetailsAdapter = new DetailsAdapter(this, mDetailsMainContent, replyLists);
            mRCView.setAdapter(mDetailsAdapter);
        }


        //以下是设置刷新按钮的位置，暂时不用
//        final DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int start = (int) (40 * metrics.density);
//        mSwipe.setProgressViewOffset(false, -start, (int) (start*1.5));


    }

    @NonNull
    private TopicModel parseResponse(okhttp3.Response response, int topicId) throws IOException {
        TopicModel topicModel = new TopicModel(topicId);
        Document html = Jsoup.parse(response.body().string());
        Element body = html.body();
        Element head = html.head();
        String title = body.getElementsByTag("h1").text();


        String content = body.getElementsByClass("topic_content").first().text();
        String contentRendered = body.getElementsByClass("topic_content").first().html();

        Elements allMeta = head.getElementsByTag("meta");
        String createdUnformed = head.getElementsByAttributeValue("property", "article:published_time").attr("content");
        long created = formatDate(createdUnformed);


        String replyNum = "";

        Elements grays = body.getElementsByClass("gray");

        int replies = 0;
        for (Element gray :
                grays) {
            if (gray.text().contains("回复")) {
                String wholeText = gray.text();
                int index = wholeText.indexOf("回复");
                replyNum = wholeText.substring(0, index - 1);

                break;
            }
        }
        XLog.d("hehe" + replyNum);
        replies = Integer.parseInt(replyNum);
        topicModel.setReplies(replies); //done

        topicModel.setTitle(title); //done
        topicModel.setContent(content); //done

        topicModel.setContentRendered(contentRendered); //done
        topicModel.setCreated(created); //done
        MemberModel member = new MemberModel();


        String memberLink = body.getElementsByClass("header").first()
                .getElementsByAttributeValueStarting("href", "/member/").attr("href");
        String username = memberLink.replace("/member/", "");
        member.setUsername(username); //done
        String largeAvatar = body.getElementsByClass("header").first()
                .getElementsByClass("avatar").attr("src");

        member.setAvatar_large(largeAvatar);
        member.setAvatar_normal(largeAvatar.replace("large", "normal"));
//                            member.setId(0);

        topicModel.setMember(member); //done

        Element nodeElement = body.getElementsByClass("header").first()
                .getElementsByAttributeValueStarting("href", "/go/").first();
        String nodeName = nodeElement.attr("href").replace("/href/", "");
        String nodeTitle = nodeElement.text();

        NodeModel nodeModel = new NodeModel();
        nodeModel.setName(nodeName);
        nodeModel.setTitle(nodeTitle);
        topicModel.setNode(nodeModel);//done
        return topicModel;
    }

    private long formatDate(String createdUnformed) {

        //2017-03-07T01:45:30Z

        String beautyString = createdUnformed.replace('T', ' ').replace('Z', ' ');

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ", Locale.CHINA);
        Date date;
        try {
            date = simpleDateFormat.parse(beautyString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1L;
    }


    public void getReplyData() {
        Type typeofR = new TypeToken<ArrayList<ReplyModel>>() {
        }.getType();
        GsonSimple<ArrayList<ReplyModel>> replies = new GsonSimple<>(JsonManager.API_REPLIES + "?topic_id="
                + mDetailsMainContent.getId(), typeofR, new ArrayListListener(), new MyErrorListener());
        VolleyHelper.getInstance().addToRequestQueue(replies);

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
                getReplyData();
                break;
            case R.id.menu_item_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "来自V2EX的帖子： " + mDetailsMainContent.getTitle() + "   "
                        + JsonManager.HTTPS_V2EX_BASE + "/t/" + mDetailsMainContent.getId());
                sendIntent.setType("text/plain");
                // createChooser 中有三大好处，自定义title
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                break;
            case R.id.menu_item_open_in_browser:
                Uri uri = Uri.parse(mDetailsMainContent.getUrl());
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
            Log.i(TAG, "GSON DONE: " + ((response == null || response.isEmpty()) ? 0 : response.get(0).toString()));
            if (response == null || response.size() == 0) {
                mSwipe.setRefreshing(false);
                return;
            }
            replyLists.clear();

            replyLists.addAll(response);

            mDetailsAdapter.notifyDataSetChanged();
            Log.d(TAG, "done with details");
            mSwipe.setRefreshing(false);

        }
    }

    private class MyErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            JsonManager.handleVolleyError(getApplicationContext(), error);
            mSwipe.setRefreshing(false);
        }
    }
}
