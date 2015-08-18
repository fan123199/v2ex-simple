package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.V2exJsonManager;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;

/**
 * Created by a708 on 15-8-14.
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainTextViewholder> {


//    private String[] mDataset;
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TopicModel> Top10 = new ArrayList<>();

    //将获取json的volley实现放在Adapter中.
    private MySingleton mSingleton;
    private ImageLoader mImageLoader;



    //JSON 解析
    JSONArray mJsonArray;

        //这是构造器
    public MainRecyclerViewAdapter(Context context) {
        this.mContext = context;
        mSingleton = MySingleton.getInstance(context);
        mInflater = LayoutInflater.from(context);
        mImageLoader = mSingleton.getImageLoader();

    }



    //onCreateViewHolder这一般就是这样了.除了layoutInflater,没有什么变动
    @Override
    public MainRecyclerViewAdapter.MainTextViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        //这叫布局解释器,用来解释
//        LayoutInflater lyInflater = LayoutInflater.from(parent.getContext());
        //找到需要显示的xml文件,主要靠inflate
         View view = mInflater.inflate(R.layout.my_text_view, parent, false);

        return new MainTextViewholder(view);

    }

    //对TextView进行赋值, 也就是操作
    @Override
    public void onBindViewHolder(MainRecyclerViewAdapter.MainTextViewholder holder, int position) {
        TopicModel currentTopic = Top10.get(position);
        holder.tvTitle.setText(currentTopic.title);


    }

    @Override
    public int getItemCount() {
        return mJsonArray.length();
    }


    //这是构建一个引用 到每个数据item的视图.用findViewById将视图的元素与变量对应起来
    public static class MainTextViewholder extends RecyclerView.ViewHolder{

        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvReplyNumber;
        public TextView tvPushTime;
        public TextView tvAuthor;
        public ImageView ivAvatar;

        public MainTextViewholder(View root) {
            super(root);

            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvContent = (TextView) root.findViewById(R.id.tvContent);
            tvReplyNumber = (TextView) root.findViewById(R.id.tvReplyNumber);
            tvPushTime = (TextView) root.findViewById(R.id.tvPushTime);
            tvAuthor = (TextView) root.findViewById(R.id.tvAuthor);
            ivAvatar = (ImageView) root.findViewById(R.id.ivAvatar);
        }

    }

}
