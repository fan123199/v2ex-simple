package im.fdx.v2ex.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.fdx.v2ex.R;

/**
 * Created by a708 on 15-8-14.
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainTextViewholder> {


    private String[] mDataset;

    //这是构建一个引用 到每个数据item的视图.
    public static class MainTextViewholder extends RecyclerView.ViewHolder{

        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvReplyNumber;
        public TextView tvPushTime;
        public TextView tvAuthor;
        public ImageView ivAvatar;

        public MainTextViewholder(View root) {
            super(root);

        }

    }

        //这是构造器
    public MainRecyclerViewAdapter(String[] myDataset) {
        mDataset = myDataset;
    }



    @Override
    public MainRecyclerViewAdapter.MainTextViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        //这叫布局解释器,用来解释
        LayoutInflater lyInflater = LayoutInflater.from(parent.getContext());
        //找到需要显示的xml文件,主要靠inflate
         View v = lyInflater.inflate(R.layout.my_text_view, parent, false);

        MainTextViewholder vh = new MainTextViewholder((TextView) v);
        return vh;

    }

    //对TextView进行赋值, 也就是操作
    @Override
    public void onBindViewHolder(MainRecyclerViewAdapter.MainTextViewholder holder, int position) {


        String item = mDataset[position];

    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }



}
