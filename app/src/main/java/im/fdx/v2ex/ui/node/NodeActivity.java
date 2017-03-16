package im.fdx.v2ex.ui.node;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.MyGsonRequest;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;

import static im.fdx.v2ex.network.HttpHelper.OK_CLIENT;
import static im.fdx.v2ex.MyApp.USE_WEB;
import static im.fdx.v2ex.MyApp.USE_API;
import static im.fdx.v2ex.network.HttpHelper.baseRequestBuilder;
import static im.fdx.v2ex.network.JsonManager.API_TOPIC;


public class NodeActivity extends AppCompatActivity {
    private static final String TAG = NodeActivity.class.getSimpleName();
    private static final int MSG_GET_TOPICS = 1;
    private static final int MSG_GET_NODE_INFO = 0;
    RelativeLayout rlNode;
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
                mSwipeRefreshLayout.setRefreshing(false);
                ivNodeIcon.setImageUrl(mNodeModel.getAvatarLargeUrl(), imageloader);
                tvNodeName.setText(mNodeModel.getTitle());
                tvNodeHeader.setText(mNodeModel.getHeader());
                tvNodeNum.setText((String.valueOf(mNodeModel.getTopics())));
            } else if (msg.what == MSG_GET_TOPICS) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };
    private String nodeName;
    private NodeModel mNodeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rlNode = (RelativeLayout) findViewById(R.id.rl_node);
        ivNodeIcon = (NetworkImageView) findViewById(R.id.iv_node_image);
        tvNodeName = (TextView) findViewById(R.id.tv_node_name);
        tvNodeHeader = (TextView) findViewById(R.id.tv_node_header);
        tvNodeNum = (TextView) findViewById(R.id.tv_topic_num);


        RecyclerView rvTopicsOfNode = (RecyclerView) findViewById(R.id.rv_topics_of_node);
//        mAdapter.setData(mTopicModels);


        mAdapter = new TopicsRVAdapter(this, mTopicModels);
        rvTopicsOfNode.setAdapter(mAdapter);
        rvTopicsOfNode.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_of_node);

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        parseIntent(getIntent());

    }

    private void parseIntent(Intent intent) {

        if (intent.getData() != null) {
            List<String> params = intent.getData().getPathSegments();
            nodeName = params.get(1);
            getNodeInfoAndTopicByOK(nodeName);

        } else {
            nodeName = intent.getStringExtra(Keys.KEY_NODE_NAME);

            if (MyApp.getInstance().getHttpMode() == USE_API) {
                String requestURL = JsonManager.API_NODE + "?name=" + nodeName;
                Log.i(TAG, requestURL);
                getNodeInfoJson(requestURL);
                String url = API_TOPIC + "?node_name=" + nodeName;
                getTopicsJsonByVolley(url);
            } else if (MyApp.getInstance().getHttpMode() == USE_WEB) {

                getNodeInfoAndTopicByOK(nodeName);
            }
        }

    }

    private void getNodeInfoAndTopicByOK(final String nodeName) {
        String requestURL = JsonManager.HTTPS_V2EX_BASE + "/go/" + nodeName;
        XLog.d("url:" + requestURL);
        OK_CLIENT.newCall(baseRequestBuilder.url(requestURL).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String body = response.body().string();

                mNodeModel = JsonManager.parseToNode(body);

                Message.obtain(handler, MSG_GET_NODE_INFO).sendToTarget();


                mTopicModels.clear();
                mTopicModels.addAll(JsonManager.parseTopicLists(body, 1));
//                Message.obtain(handler, MSG_GET_TOPICS, mTopicModels).sendToTarget();
                handler.sendEmptyMessage(MSG_GET_TOPICS);
            }
        });
    }

    private void getTopicsJsonByVolley(String requestURL) {
        Log.w(TAG, requestURL);

        Type typeOfT = new TypeToken<ArrayList<TopicModel>>() {
        }.getType();
        MyGsonRequest<ArrayList<TopicModel>> topicGson = new MyGsonRequest<>(requestURL, typeOfT, new Response.Listener<ArrayList<TopicModel>>() {
            @Override
            public void onResponse(ArrayList<TopicModel> response) {
                if (mTopicModels.equals(response)) {
                    mAdapter.notifyDataSetChanged();
//                    mSwipeLayout.setRefreshing(false);
                    return;
                }
                mTopicModels.clear();
                mTopicModels.addAll(0, response);
//                mAdapter.setData(mTopicModels);
                mAdapter.notifyDataSetChanged();
//                mSwipeLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JsonManager.handleVolleyError(NodeActivity.this, error);
//                mSwipeLayout.setRefreshing(false);
            }
        });

        VolleyHelper.getInstance().addToRequestQueue(topicGson);
    }

    private void getNodeInfoJson(String url) {
        StringRequest stringRequest = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        NodeModel nodeModel = JsonManager.myGson.fromJson(response, NodeModel.class);
                        ivNodeIcon.setImageUrl(nodeModel.getAvatarLargeUrl(), imageloader);
                        tvNodeName.setText(nodeModel.getTitle());
                        tvNodeHeader.setText(nodeModel.getHeader());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HintUI.S(rlNode, "getNothing");
                    }
                }
        );
        VolleyHelper.getInstance().addToRequestQueue(stringRequest);
    }

}
