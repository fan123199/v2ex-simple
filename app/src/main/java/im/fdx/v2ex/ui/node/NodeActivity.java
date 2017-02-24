package im.fdx.v2ex.ui.node;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.w3c.dom.Node;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.ui.main.MainAdapter;
import im.fdx.v2ex.utils.GsonSimple;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeHelper;


public class NodeActivity extends AppCompatActivity {
    private static final String TAG = NodeActivity.class.getSimpleName();
//    NodeModel nodeModel;
RelativeLayout rlNode;
    NetworkImageView ivNodeIcon;
    TextView tvNodeName;
    TextView tvNodeHeader;

    List<TopicModel> topicModels = new ArrayList<>();
    private MyAdapter adapter;
    private ImageLoader imageloader = VolleyHelper.getInstance().getImageLoader();

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
//        adapter.setData(topicModels);

        adapter = new MyAdapter(topicModels,this);
//        adapter.setData(topicModels);
        rvTopicsOfNode.setAdapter(adapter);
        rvTopicsOfNode.setLayoutManager(new LinearLayoutManager(this));

        long nodeId = getIntent().getLongExtra(Keys.KEY_NODE_ID, -1L);

        String requestURL = JsonManager.NODE_JSON + "?id=" + nodeId;
        Log.i(TAG, requestURL);

        getNodeInfoJson(requestURL);
        String reqTopicsUrl = JsonManager.NODE_TOPIC+ "?node_id="+nodeId;
        getTopicsJson(reqTopicsUrl);

    }

    private void getTopicsJson(String requestURL) {
        Log.w(TAG, requestURL);

        Type typeOfT = new TypeToken<ArrayList<TopicModel>>() {
        }.getType();
        GsonSimple<ArrayList<TopicModel>> topicGson = new GsonSimple<>(requestURL, typeOfT, new Response.Listener<ArrayList<TopicModel>>() {
            @Override
            public void onResponse(ArrayList<TopicModel> response) {

                if (topicModels.equals(response)) {
                    adapter.notifyDataSetChanged();
//                    mSwipeLayout.setRefreshing(false);
                    return;
                }

                topicModels.clear();
                topicModels.addAll(0, response);
//                adapter.setData(topicModels);
                adapter.notifyDataSetChanged();
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

                        //                        HintUI.T(getApplication(), nodeModel.getHeader());
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

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<TopicModel> data = new ArrayList<>();
        private Context context ;
//        private ImageLoader mImageLoader = VolleyHelper.getInstance().getImageLoader();


        public MyAdapter(List<TopicModel> data, Context context) {
            this.data = data;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_view, parent, false);

            return new MainAdapter.MainViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder2, int position) {
            final TopicModel currentTopic = data.get(position);

            MainAdapter.MainViewHolder holder = (MainAdapter.MainViewHolder) holder2;

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
            holder.tvPushTime.setText(TimeHelper.getRelativeTime(context, currentTopic.getCreated()));
            holder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormalUrl(), imageloader);


//            holder.ivAvatar.setOnClickListener(new MainAdapter.MyOnClickListener(currentTopic, context));
//            holder.tvAuthor.setOnClickListener(new MainAdapter.MyOnClickListener(currentTopic, context));

        }

        @Override
        public int getItemCount() {
            return topicModels.size();
        }

        public void setData(List<TopicModel> data) {
            this.data = data;
        }
    }

}
