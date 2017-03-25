package im.fdx.v2ex.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.MemberActivity;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.ui.node.NodeActivity;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.view.CircleVImageView;
import im.fdx.v2ex.utils.TimeHelper;
import im.fdx.v2ex.view.GoodTextView;

import static im.fdx.v2ex.MyApp.USE_API;

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
public class TopicsRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ;
    private LayoutInflater mInflater;
    private List<TopicModel> mTopicList = new ArrayList<>();
    private ImageLoader mImageLoader;
    private Context mContext;

    //这是构造器
    public TopicsRVAdapter(Context activity, List<TopicModel> topicList) {
        mContext = activity;
        mInflater = LayoutInflater.from(activity);
        mImageLoader = VolleyHelper.getInstance().getImageLoader();
        mTopicList = topicList;
    }


    //Done onCreateViewHolder一般就这样.除了layoutInflater,没有什么变动
    // 20150916,可以对View进行Layout的设置。
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_topic_view, parent, false);
        return new MainViewHolder(view);

    }

    //Done 对TextView进行赋值, 也就是操作
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder2, int position) {
        final TopicModel currentTopic = mTopicList.get(position);
        MyOnClickListener listener = new MyOnClickListener(mContext, currentTopic);
        MainViewHolder holder = (MainViewHolder) holder2;

        holder.container.setOnClickListener(listener);
        holder.tvTitle.setText(currentTopic.getTitle());

        holder.tvContent.setVisibility(View.GONE);
        if (MyApp.getInstance().getHttpMode() == USE_API) {
            holder.tvContent.setMaxLines(6);
            holder.tvContent.setText(currentTopic.getContent());
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            holder.tvContent.setTransitionName("header");
//        }
        String sequence = Integer.toString(currentTopic.getReplies()) + " " + mContext.getString(R.string.reply);
        holder.tvReplyNumber.setText(sequence);
        holder.tvAuthor.setText(currentTopic.getMember().getUsername());
        holder.tvNode.setText(currentTopic.getNode().getTitle());
        holder.tvCreated.setText(TimeHelper.getRelativeTime(currentTopic.getCreated()));
        holder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormalUrl(), mImageLoader);


        holder.tvNode.setOnClickListener(listener);
        holder.ivAvatar.setOnClickListener(listener);
//        holder.tvAuthor.setOnClickListener(listener);

    }

    @Override
    public int getItemCount() {
        return mTopicList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void updateData(List<TopicModel> tps) {
        mTopicList = tps;
    }

    // 这是构建一个引用 到每个数据item的视图.用findViewById将视图的元素与变量对应起来,。
    // 用static就是为了复用
    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public GoodTextView tvContent;
        public TextView tvReplyNumber;
        public TextView tvCreated;
        public TextView tvAuthor;
        public CircleVImageView ivAvatar;
        public TextView tvNode;
        public View container;

        public MainViewHolder(View root) {
            super(root);
            container = root;
            tvTitle = (TextView) root.findViewById(R.id.tv_title);
            tvContent = (GoodTextView) root.findViewById(R.id.tv_content);
            tvReplyNumber = (TextView) root.findViewById(R.id.tv_reply_number);
            tvCreated = (TextView) root.findViewById(R.id.tv_created);
            tvAuthor = (TextView) root.findViewById(R.id.tv_author);
            ivAvatar = (CircleVImageView) root.findViewById(R.id.iv_avatar_profile);
            tvNode = (TextView) root.findViewById(R.id.tv_node);
        }


    }

    public static class MyOnClickListener implements View.OnClickListener {
        private TopicModel topic;
        private Context context;

        public MyOnClickListener(Context context, TopicModel topic) {
            this.context = context;
            this.topic = topic;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_author:
                    break;
                case R.id.iv_avatar_profile:
                    Intent intent = new Intent(context, MemberActivity.class);
                    intent.putExtra(Keys.KEY_USERNAME, topic.getMember().getUsername());
                    context.startActivity(intent);
                    break;
                case R.id.tv_node:
                    Intent itNode = new Intent(context, NodeActivity.class);
                    itNode.putExtra(Keys.KEY_NODE_NAME, topic.getNode().getName());
                    context.startActivity(itNode);
                    break;
                case R.id.main_text_view:
                    Intent intentDetail = new Intent(context, DetailsActivity.class);
                    intentDetail.putExtra("model", topic);
                    context.startActivity(intentDetail);
                    break;
            }

        }
    }
}
