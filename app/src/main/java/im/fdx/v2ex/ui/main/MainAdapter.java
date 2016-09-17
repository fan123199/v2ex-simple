package im.fdx.v2ex.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.ui.node.NodeActivity;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.CircleVImage;
import im.fdx.v2ex.utils.TimeHelper;
import im.fdx.v2ex.widget.GoodTextView;

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private List<TopicModel> mTopicList;
    private ImageLoader mImageLoader;
    private Context context;

    //这是构造器
    public MainAdapter(Context activity, List<TopicModel> topicList) {
        context = activity;
        mInflater = LayoutInflater.from(activity);
        mImageLoader = VolleyHelper.getInstance().getImageLoader();
        mTopicList = topicList;
    }


    //Done onCreateViewHolder一般就这样.除了layoutInflater,没有什么变动
    // 20150916,可以对View进行Layout的设置。
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /**
         * 找到需要显示的xml文件,是通过inflate
         */
        View view = mInflater.inflate(R.layout.item_topic_view, parent, false);

        return new MainViewHolder(view);

    }

    //Done 对TextView进行赋值, 也就是操作
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder2, int position) {
        final TopicModel currentTopic = mTopicList.get(position);
        MainViewHolder holder = (MainViewHolder) holder2;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("model", currentTopic);
                context.startActivity(intent);
            }
        });
        holder.tvTitle.setText(currentTopic.getTitle());
        holder.tvContent.setMaxLines(6);
        holder.tvContent.setText(currentTopic.getContent());

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            holder.tvContent.setTransitionName("header");
//        }
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
        holder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormalUrl(), mImageLoader);


        holder.ivAvatar.setOnClickListener(new MyOnClickListener(currentTopic, context));
        holder.tvAuthor.setOnClickListener(new MyOnClickListener(currentTopic, context));

    }

//    public void setTopic(List<TopicModel> top10){
//        this.mTopicList = top10;
//    }

    @Override
    public int getItemCount() {
        return mTopicList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    // 这是构建一个引用 到每个数据item的视图.用findViewById将视图的元素与变量对应起来,。
    // 用static就是为了复用
    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public GoodTextView tvContent;
        public TextView tvReplyNumber;
        public TextView tvPushTime;
        public TextView tvAuthor;
        public CircleVImage ivAvatar;
        public TextView tvNode;

        public MainViewHolder(View root) {
            super(root);

            tvTitle = (TextView) root.findViewById(R.id.tv_title);
            tvContent = (GoodTextView) root.findViewById(R.id.tv_content);
            tvReplyNumber = (TextView) root.findViewById(R.id.tv_reply_number);
            tvPushTime = (TextView) root.findViewById(R.id.tv_pushtime);
            tvAuthor = (TextView) root.findViewById(R.id.tv_author);
            ivAvatar = (CircleVImage) root.findViewById(R.id.iv_avatar_profile);
            tvNode = (TextView) root.findViewById(R.id.tv_node);

//            tvNode.setOnClickListener(listener);
        }


    }

    public static class MyOnClickListener implements View.OnClickListener {
        private TopicModel current;
        private Context context;

        public MyOnClickListener(TopicModel current, Context context) {
            this.current = current;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_avatar_profile:
                case R.id.tv_author:
                    Intent intent = new Intent();
                    intent.setAction("im.fdx.v2ex.intent.profile");
                    intent.putExtra("profile_id", current.getMember().getId());
                    context.startActivity(intent);
                    break;

                case R.id.tv_node:

            }

        }
    }
}
