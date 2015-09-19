package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.utils.TimeHelper;

/**
 * Created by a708 on 15-8-14.
 * 主页的Adapter，就一个普通的RecyclerView
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<TopicModel> TopicList = new ArrayList<>();
    private ImageLoader mImageLoader;
//    private Context mContext;


    //这是构造器
    public MainAdapter(Context context) {
//        mContext = context;
        mInflater = LayoutInflater.from(context);
        mImageLoader = MySingleton.getInstance(context).getImageLoader();
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
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        TopicModel currentTopic = TopicList.get(position);
        holder.tvTitle.setText(currentTopic.title);
        holder.tvContent.setMaxLines(6);
        holder.tvContent.setText(currentTopic.content);
        holder.tvContent.setTransitionName("header" + position);
        holder.tvReplyNumber.setText(String.valueOf(currentTopic.replies) + "个回复");
        holder.tvAuthor.setText(currentTopic.author);
        holder.tvNode.setText(currentTopic.nodeTitle);
        holder.tvPushTime.setText(TimeHelper.RelativeTime(currentTopic.created));
        holder.ivAvatar.setImageUrl(currentTopic.avatarString, mImageLoader);

    }

    public void setTopic(ArrayList<TopicModel> top10){
        this.TopicList = top10;
    }
    //Done
    @Override
    public int getItemCount() {
        return TopicList.size();
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
        public NetworkImageView ivAvatar;
        public TextView tvNode;

        public MainViewHolder(View root) {
            super(root);

            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvContent = (TextView) root.findViewById(R.id.tvContent);
            tvReplyNumber = (TextView) root.findViewById(R.id.tvReplyNumber);
            tvPushTime = (TextView) root.findViewById(R.id.tvPushTime);
            tvAuthor = (TextView) root.findViewById(R.id.tvReplier);
            ivAvatar = (NetworkImageView) root.findViewById(R.id.ivAvatar);
            tvNode = (TextView) root.findViewById(R.id.tvNode);
        }


    }

}
