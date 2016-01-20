package im.fdx.v2ex.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.utils.MyNetworkCircleImageView;
import im.fdx.v2ex.utils.TimeHelper;

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<TopicModel> topicList;
    private ImageLoader mImageLoader;
    private Context mContext;


    //这是构造器
    public MainAdapter(Context context, ArrayList<TopicModel> topList) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mImageLoader = MySingleton.getInstance().getImageLoader();
        topicList = topList;
    }



    //Done onCreateViewHolder一般就这样.除了layoutInflater,没有什么变动
    // 20150916,可以对View进行Layout的设置。
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //这叫布局解释器,用来解释
//        另一种方式，LayoutInflater lyInflater = LayoutInflater.from(parent.getContext());
        //找到需要显示的xml文件,是通过inflate
        View view = mInflater.inflate(R.layout.topic_row_view, parent, false);

        return new MainViewHolder(view);

    }

    //Done 对TextView进行赋值, 也就是操作
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        TopicModel currentTopic = topicList.get(position);
        holder.tvTitle.setText(currentTopic.getTitle());
        holder.tvContent.setMaxLines(6);
        holder.tvContent.setText(currentTopic.getContent());
        holder.tvContent.setTransitionName("header" + position);

        String sequence = Integer.toString(currentTopic.getReplies()) + " " + mContext.getString(R.string.reply);
        holder.tvReplyNumber.setText(sequence);
        holder.tvAuthor.setText(currentTopic.getMember().getUsername()); // 各个模型建立完毕
        holder.tvNode.setText(currentTopic.getNode().getTitle());
        holder.tvPushTime.setText(TimeHelper.RelativeTime(mContext, currentTopic.getCreated()));
        holder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormal(), mImageLoader);

    }

    public void setTopic(ArrayList<TopicModel> top10){
        this.topicList = top10;
    }
    //Done
    @Override
    public int getItemCount() {
        return topicList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    //Done
    //这是构建一个引用 到每个数据item的视图.用findViewById将视图的元素与变量对应起来
    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvReplyNumber;
        public TextView tvPushTime;
        public TextView tvAuthor;
        public MyNetworkCircleImageView ivAvatar;
        public TextView tvNode;

        public MainViewHolder(View root) {
            super(root);

            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvContent = (TextView) root.findViewById(R.id.tvContent);
            tvReplyNumber = (TextView) root.findViewById(R.id.tvReplyNumber);
            tvPushTime = (TextView) root.findViewById(R.id.tvPushTime);
            tvAuthor = (TextView) root.findViewById(R.id.tvReplier);
            ivAvatar = (MyNetworkCircleImageView) root.findViewById(R.id.ivAvatar);
            tvNode = (TextView) root.findViewById(R.id.tvNode);
        }


    }

}
