package im.fdx.v2ex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NotificationModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
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
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                .headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(url)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                if (response.code() == 200) {
                    Document html = Jsoup.parse(response.body().string());
                    List<NotificationModel> c = NetManager.parseToNotifications(html);
                    if (c.isEmpty()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipe.setRefreshing(false);
                                ViewUtil.INSTANCE.showNoContent(NotificationActivity.this, flContainer);
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

}
