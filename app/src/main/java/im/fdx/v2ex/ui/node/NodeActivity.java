package im.fdx.v2ex.ui.node;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.MyGsonRequest;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeHelper;
import okhttp3.Call;
import okhttp3.Callback;

import static im.fdx.v2ex.network.HttpHelper.OK_CLIENT;
import static im.fdx.v2ex.network.HttpHelper.USE_VOLLEY;
import static im.fdx.v2ex.network.HttpHelper.baseRequestBuilder;


public class NodeActivity extends AppCompatActivity {
    private static final String TAG = NodeActivity.class.getSimpleName();
    private static final int MSG_GET_REPLY = 1;
    private static final int MSG_GET_NODE = 0;
    RelativeLayout rlNode;
    NetworkImageView ivNodeIcon;
    TextView tvNodeName;
    TextView tvNodeHeader;

    List<TopicModel> mTopicModels = new ArrayList<>();
    private MyAdapter mAdapter;

    private static ImageLoader imageloader = VolleyHelper.getInstance().getImageLoader();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_GET_NODE) {
                NodeModel nodeModel = (NodeModel) msg.obj;
                ivNodeIcon.setImageUrl(nodeModel.getAvatarLargeUrl(), imageloader);
                tvNodeName.setText(nodeModel.getTitle());
                tvNodeHeader.setText(nodeModel.getHeader());
            } else if (msg.what == MSG_GET_REPLY) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

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


        RecyclerView rvTopicsOfNode = (RecyclerView) findViewById(R.id.rv_topics_of_node);
//        mAdapter.setData(mTopicModels);

        mAdapter = new MyAdapter(mTopicModels, this);
        rvTopicsOfNode.setAdapter(mAdapter);
        rvTopicsOfNode.setLayoutManager(new LinearLayoutManager(this));

        long nodeId = getIntent().getLongExtra(Keys.KEY_NODE_ID, -1L);

        String nodeName = getIntent().getStringExtra(Keys.KEY_NODE_NAME);

        if (nodeId != -1L) {
            String requestURL = JsonManager.NODE_JSON + "?id=" + nodeId;
            Log.i(TAG, requestURL);
            getNodeInfoJson(requestURL);
        } else if (nodeName != null) {
            String requestURL = JsonManager.HTTPS_V2EX_BASE + "/go/" + nodeName;

            getNodeInfoByOK(requestURL, nodeName);
        }

        if (MyApp.getInstance().getHttpMode() == USE_VOLLEY) {
            String reqTopicsUrl = JsonManager.NODE_TOPIC + "?node_id=" + nodeId;
            getTopicsJson(reqTopicsUrl);
        }

    }

    private void getNodeInfoByOK(String requestURL, final String nodeName) {
        OK_CLIENT.newCall(baseRequestBuilder.url(requestURL).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String body = response.body().string();
                parseToNode(body, nodeName);
                mTopicModels = JsonManager.parseTopics(body);
//                Message.obtain(handler, MSG_GET_REPLY, mTopicModels).sendToTarget();
                handler.sendEmptyMessage(MSG_GET_REPLY);
            }
        });
    }

    private void parseToNode(String response, String nodeName) {

        NodeModel nodeModel = new NodeModel(nodeName);
        Document html = Jsoup.parse(response);
        Element body = html.body();
        Element header = body.getElementsByClass("header").first();
        String content = header.getElementsByClass("f12 gray").first().text();
        String number = header.getElementsByTag("strong").first().text();
        String nodeTitleOrignal = header.getElementsByClass("fr f12").first().text();
        String nodeTitle = nodeTitleOrignal.split(">")[1].split(" ")[0].trim();
        nodeModel.setName(nodeName);
        nodeModel.setTitle(nodeTitle);
        nodeModel.setTopics(Integer.parseInt(number));
        nodeModel.setHeader(content);
        Message.obtain(handler, MSG_GET_NODE, nodeModel).sendToTarget();
    }

    private void getTopicsJson(String requestURL) {
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

    private static class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<TopicModel> data = new ArrayList<>();
        private Context context;
        private ImageLoader mImageLoader = VolleyHelper.getInstance().getImageLoader();


        public MyAdapter(List<TopicModel> data, Context context) {
            this.data = data;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_view, parent, false);

            return new TopicsRVAdapter.MainViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder2, int position) {
            final TopicModel currentTopic = data.get(position);

            TopicsRVAdapter.MainViewHolder holder = (TopicsRVAdapter.MainViewHolder) holder2;

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), DetailsActivity.class);
                    intent.putExtra("model", currentTopic);
                    context.startActivity(intent);
                }
            });
            holder.tvTitle.setText(currentTopic.getTitle());
            holder.tvContent.setMaxLines(6);
            holder.tvContent.setText(currentTopic.getContent());

            String sequence = Integer.toString(currentTopic.getReplies()) + " " + context.getString(R.string.reply);
            holder.tvReplyNumber.setText(sequence);
            holder.tvAuthor.setText(currentTopic.getMember().getUserName()); // 各个模型建立完毕
            holder.tvNode.setText(currentTopic.getNode().getTitle());
            holder.tvNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent itNode = new Intent(context, NodeActivity.class);
                    itNode.putExtra(Keys.KEY_NODE_ID, currentTopic.getNode().getId());
                    context.startActivity(itNode);
                }
            });
            holder.tvCreated.setText(TimeHelper.getRelativeTime(currentTopic.getCreated()));
            holder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormalUrl(), imageloader);


//            holder.ivAvatar.setOnClickListener(new TopicsRVAdapter.MyOnClickListener(currentTopic, context));
//            holder.tvAuthor.setOnClickListener(new TopicsRVAdapter.MyOnClickListener(currentTopic, context));

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<TopicModel> data) {
            this.data = data;
        }
    }

}
