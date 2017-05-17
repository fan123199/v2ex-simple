package im.fdx.v2ex.ui;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import org.greenrobot.greendao.annotation.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.model.NotificationModel;
import im.fdx.v2ex.ui.main.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.utils.ViewUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationActivity extends AppCompatActivity {

    List<NotificationModel> notifications = new ArrayList<>();
    private NotificationAdapter adapter;
    private SwipeRefreshLayout mSwipe;
    private RecyclerView rvNotification;
    private FrameLayout flContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.notification);

        flContainer = (FrameLayout) findViewById(R.id.fl_container);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSwipe = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNotification();

            }
        });

        rvNotification = (RecyclerView) findViewById(R.id.rv_container);
        rvNotification.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notifications);
        rvNotification.setAdapter(adapter);
        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        adapter.setNumber(intent.getIntExtra("number", -1));
        fetchNotification();
    }

    private void fetchNotification() {
        String url = "https://www.v2ex.com/notifications";
        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(url)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                if (response.code() == 200) {
                    Document html = Jsoup.parse(response.body().string());
                    List<NotificationModel> c = parseToNotifications(html);
                    if (c.isEmpty()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipe.setRefreshing(false);
                                ViewUtil.showNoContent(NotificationActivity.this, flContainer);
                            }
                        });
                        return;
                    }
                    notifications.addAll(c);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            mSwipe.setRefreshing(false);
                        }
                    });
                }
            }
        });

    }

    private static
    @NotNull
    List<NotificationModel> parseToNotifications(Document html) {
        Element body = html.body();
        Elements items = body.getElementsByAttributeValueStarting("id", "n_");
        if (items == null) {
            return Collections.emptyList();
        }

        List<NotificationModel> notificationModels = new ArrayList<>();
        for (Element item : items) {
            NotificationModel notification = new NotificationModel();

            String notificationId = item.attr("id").substring(2);
            notification.setId(notificationId);

            Element contentElement = item.getElementsByClass("payload").first();
            String content = "";
            if (contentElement != null) {
                content = contentElement.text();
            }

            String time = item.getElementsByClass("snow").first().text();
            notification.setTime(time);// 2/6


            Element memberElement = item.getElementsByTag("a").first();
            String username = memberElement.attr("href").replace("/member/", "");
            String avatarUrl = memberElement.getElementsByClass("avatar").first().attr("src");
            MemberModel memberModel = new MemberModel();
            memberModel.setUsername(username);
            memberModel.setAvatar_normal(avatarUrl);
            notification.setMember(memberModel); // 3/6

            TopicModel topicModel = new TopicModel();

            Element topicElement = item.getElementsByClass("fade").first();
//            <a href="/t/348757#reply1">交互式《线性代数》学习资料</a>
            String href = topicElement.getElementsByAttributeValueStarting("href", "/t/").first().attr("href");

            topicModel.setTitle(topicElement.getElementsByAttributeValueStarting("href", "/t/").first().text());

            Pattern p = Pattern.compile("(?<=/t/)\\d+(?=#)");
            Matcher matcher = p.matcher(href);
            if (matcher.find()) {
                String topicId = matcher.group();
                topicModel.setId(topicId);
            }

            Pattern p2 = Pattern.compile("(?<=reply)d+\\b");
            Matcher matcher2 = p2.matcher(href);
            if (matcher2.find()) {
                String replies = matcher2.group();
                notification.setReplyPosition(replies);
            }
            notification.setTopic(topicModel); //4/6
            String type = topicElement.ownText();
            notification.setType(type);
            notification.setContent(content); //1/6
            notificationModels.add(notification);
        }
        return notificationModels;
    }
}
