package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.ui.DetailsActivity;

/**
 * Created by a708 on 15-8-14.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TopicModel> Top10 = new ArrayList<>();


    //这是构造器
    public MainAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }



    //Done onCreateViewHolder一般就这样.除了layoutInflater,没有什么变动
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //这叫布局解释器,用来解释
//        LayoutInflater lyInflater = LayoutInflater.from(parent.getContext());
        //找到需要显示的xml文件,主要靠inflate
         View view = mInflater.inflate(R.layout.my_text_view, parent, false);

        return new MainViewHolder(view);

    }

    //Done 对TextView进行赋值, 也就是操作
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        TopicModel currentTopic = Top10.get(position);
        holder.tvTitle.setText(currentTopic.title);
        holder.tvContent.setText(currentTopic.content);
        holder.tvReplyNumber.setText(String.valueOf(currentTopic.replies));
        holder.tvAuthor.setText(currentTopic.author);
        holder.tvNode.setText(currentTopic.nodeTitle);
    }

    public void setTopic(ArrayList<TopicModel> top10){
        this.Top10 = top10;
    }
    //Done
    @Override
    public int getItemCount() {
        return Top10.size();
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
        public ImageView ivAvatar;
        public TextView tvNode;

        public MainViewHolder(View root) {
            super(root);


            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvContent = (TextView) root.findViewById(R.id.tvContent);
            tvReplyNumber = (TextView) root.findViewById(R.id.tvReplyNumber);
            tvPushTime = (TextView) root.findViewById(R.id.tvPushTime);
            tvAuthor = (TextView) root.findViewById(R.id.tvAuthor);
            ivAvatar = (ImageView) root.findViewById(R.id.ivAvatar);
            tvNode = (TextView) root.findViewById(R.id.tvNode);
        }


    }

}
