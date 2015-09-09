package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.utils.TimeHelper;

/**
 * Created by a708 on 15-9-7.
 */
public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    TopicModel header;
    private ArrayList<ReplyModel> replyList= new ArrayList<>();
    private LayoutInflater mInflater;

    public DetailsAdapter(Context context,TopicModel header,ArrayList<ReplyModel> replyList) {
        mInflater = LayoutInflater.from(context);
        this.header = header;
        this.replyList = replyList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == TYPE_HEADER){
            View view = mInflater.inflate(R.layout.topic_row_view, parent, false);
            return new MainAdapter.MainViewHolder(view);
        }
        else if(viewType ==TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.reply_row_view, parent, false);
            return new ViewHolderItem(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

//        if(holder instanceof MainAdapter.MainViewHolder) {
            //采用更直观的选择语句
        if(getItemViewType(position)==TYPE_HEADER){

            MainAdapter.MainViewHolder MVHolder = (MainAdapter.MainViewHolder) holder;
            TopicModel currentTopic = header;
            MVHolder.tvTitle.setText(currentTopic.title);
            MVHolder.tvContent.setText(currentTopic.content);

            MVHolder.tvReplyNumber.setText(String.valueOf(currentTopic.replies)+"个回复");
            MVHolder.tvAuthor.setText(currentTopic.author);
            MVHolder.tvNode.setText(currentTopic.nodeTitle);
            MVHolder.tvPushTime.setText(TimeHelper.RelativeTime(currentTopic.created));

        }
//        if(holder instanceof ViewHolderItem) {
            //采用更直观的选择语句
        else if (getItemViewType(position) == TYPE_ITEM) {
            ViewHolderItem VHItem = (ViewHolderItem) holder;

           // if(!replyList.isEmpty()) {
            //    因为上一个if语句默认了replylist不可能为空
             ReplyModel replyModel = getItem(position - 1);
//                L.m(replyModel +" No. "+String.valueOf(position-1)+ "in DetailsAdapter");
                VHItem.replyTime.setText(TimeHelper.RelativeTime(replyModel.created));
                VHItem.replier.setText(replyModel.author);
                VHItem.content.setText(replyModel.content);
                VHItem.row.setText(String.valueOf(position));


        }

    }

    //不需要判空。
    private ReplyModel getItem(int position) {
        return replyList.get(position);
    }


    @Override
    public int getItemCount() {
        return replyList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    //我重用了MainAdapter中的MainViewHolder
//    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
//        public ViewHolderHeader(View itemView) {
//            super(itemView);
//        }
//    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder{

        TextView replier;
        TextView replyTime;
        TextView content;
        TextView row;
        ImageView avatar;


        public ViewHolderItem(View itemView) {
            super(itemView);

            replier = (TextView) itemView.findViewById(R.id.tvReplier);
            replyTime = (TextView) itemView.findViewById(R.id.tvReplyTime);
            content = (TextView) itemView.findViewById(R.id.tvReplyContent);
            row = (TextView) itemView.findViewById(R.id.tvRow);
            avatar = (ImageView) itemView.findViewById(R.id.reply_avatar);
        }
    }
}
