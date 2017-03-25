package im.fdx.v2ex.ui.node;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.main.CreateTopicActivity;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static im.fdx.v2ex.network.HttpHelper.OK_CLIENT;


public class NodeActivity extends AppCompatActivity {
    private static final String TAG = NodeActivity.class.getSimpleName();
    private static final int MSG_GET_TOPICS = 1;
    private static final int MSG_GET_NODE_INFO = 0;
    private static final int MSG_ERROR_AUTH = 2;
    RelativeLayout rlNodeList;
    RelativeLayout rlNodeHeader;
    NetworkImageView ivNodeIcon;
    TextView tvNodeName;
    TextView tvNodeHeader;
    TextView tvNodeNum;
    SwipeRefreshLayout mSwipeRefreshLayout;

    List<TopicModel> mTopicModels = new ArrayList<>();
    private TopicsRVAdapter mAdapter;

    private static ImageLoader imageloader = VolleyHelper.getInstance().getImageLoader();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            XLog.i("get handler msg " + msg.what);
            if (msg.what == MSG_GET_NODE_INFO) {

                ivNodeIcon.setImageUrl(mNodeModel.getAvatarLargeUrl(), imageloader);
                XLog.d(mNodeModel.getTitle());
//                tvNodeName.setText(mNodeModel.getTitle());
                collapsingToolbarLayout.setTitle(mNodeModel.getTitle());
                collapsingToolbarLayout.setTitleEnabled(true);
                tvNodeHeader.setText(mNodeModel.getHeader());
                tvNodeNum.setText(getString(R.string.topic_number, mNodeModel.getTopics()));
            } else if (msg.what == MSG_GET_TOPICS) {
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (msg.what == MSG_ERROR_AUTH) {
                HintUI.t(NodeActivity.this, "需要登录");
                finish();
            }
        }
    };
    private String nodeName;
    private NodeModel mNodeModel;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String nodeTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //很关键，不会一闪而过一个东西
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_node);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.ctl_node);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                int maxScroll = appBarLayout.getTotalScrollRange();
                double percentage = (double) Math.abs(verticalOffset) / (double) maxScroll;
                handleAlphaOnTitle(percentage);
            }
        });


        FloatingActionButton fabNode = (FloatingActionButton) findViewById(R.id.fab_node);
        fabNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NodeActivity.this, CreateTopicActivity.class);
                intent.putExtra(Keys.KEY_NODE_NAME, nodeName);
                startActivity(intent);
            }
        });
        if (!MyApp.getInstance().isLogin()) {
            fabNode.hide();
        }

        rlNodeHeader = (RelativeLayout) findViewById(R.id.rl_node_header);


        rlNodeList = (RelativeLayout) findViewById(R.id.rl_node_list);
        ivNodeIcon = (NetworkImageView) findViewById(R.id.iv_node_image);
        tvNodeName = (TextView) findViewById(R.id.tv_node_name);
        tvNodeHeader = (TextView) findViewById(R.id.tv_node_header);
        tvNodeNum = (TextView) findViewById(R.id.tv_topic_num);


        RecyclerView rvTopicsOfNode = (RecyclerView) findViewById(R.id.rv_topics_of_node);

        mAdapter = new TopicsRVAdapter(this, mTopicModels);
        rvTopicsOfNode.setAdapter(mAdapter);
        rvTopicsOfNode.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_of_node);

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNodeInfoAndTopicByOK(nodeName);

            }
        });
        parseIntent(getIntent());

    }

    private void handleAlphaOnTitle(double percentage) {
        XLog.tag("collapse").d(percentage);
        if (percentage > 0.8 && percentage <= 1) {
            rlNodeHeader.setVisibility(View.INVISIBLE);  //View隐藏
            collapsingToolbarLayout.setTitle(mNodeModel != null ? mNodeModel.getTitle() : "");
        } else if (percentage <= 0.8 && percentage >= 0) {
            rlNodeHeader.setVisibility(View.VISIBLE); //view 显示
//            collapsingToolbarLayout.setTitle("");//设置title不显示
        }
    }

    private void parseIntent(Intent intent) {

        if (intent.getData() != null) {
            List<String> params = intent.getData().getPathSegments();
            nodeName = params.get(1);
        } else if (intent.getStringExtra(Keys.KEY_NODE_NAME) != null) {
            nodeName = intent.getStringExtra(Keys.KEY_NODE_NAME);
        }
        getNodeInfoAndTopicByOK(nodeName);

    }

    private void getNodeInfoAndTopicByOK(final String nodeName) {
        String requestURL = NetManager.HTTPS_V2EX_BASE + "/go/" + nodeName;
        XLog.d("url:" + requestURL);
        OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders).url(requestURL).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                if (response.code() == 302) {
                    handler.sendEmptyMessage(MSG_ERROR_AUTH);
                    return;
                } else if (response.code() != 200) {
                    NetManager.dealError();
                    return;
                }
                String body = response.body().string();
                Document html = Jsoup.parse(body);
                mNodeModel = NetManager.parseToNode(html);
                Message.obtain(handler, MSG_GET_NODE_INFO).sendToTarget();

                mTopicModels.clear();
                mTopicModels.addAll(NetManager.parseTopicLists(html, 1));
//                Message.obtain(handler, MSG_GET_TOPICS, mTopicModels).sendToTarget();
                handler.sendEmptyMessage(MSG_GET_TOPICS);
            }
        });
    }

}
