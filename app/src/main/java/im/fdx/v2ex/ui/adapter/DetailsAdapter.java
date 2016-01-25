package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.utils.ContentUtils;
import im.fdx.v2ex.utils.MyNetworkCircleImageView;
import im.fdx.v2ex.utils.TimeHelper;

/**
 * Created by a708 on 15-9-7.
 * 注意<>中的参数是默认的
 */
public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private TopicModel header;
    private ArrayList<ReplyModel> replyList = new ArrayList<>();
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private Context mContext;

    public DetailsAdapter(Context context, TopicModel header, ArrayList<ReplyModel> replyList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.header = header;
        this.replyList = replyList;
        mImageLoader = MySingleton.getInstance().getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = mInflater.inflate(R.layout.topic_row_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.bottomMargin =30;
            lp.setMargins(0, 0, 0, 30);
            view.setLayoutParams(lp);

            return new MainAdapter.MainViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.reply_row_view, parent, false);
            return new ViewHolderItem(view);
        }
        throw new RuntimeException(" no type that matches " + viewType + " + make sure using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

//        if(holder instanceof MainAdapter.MainViewHolder) {
        //采用更直观的选择语句
        if (getItemViewType(position) == TYPE_HEADER) {

            MainAdapter.MainViewHolder MVHolder = (MainAdapter.MainViewHolder) holder;
            final TopicModel currentTopic = header;
            MVHolder.tvTitle.setText(currentTopic.getTitle());

            MVHolder.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            MVHolder.tvContent.setTextIsSelectable(true);
            MVHolder.tvContent.setText(ContentUtils.formatContentSimple(currentTopic.getContent()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MVHolder.tvContent.setTransitionName("header");
            }
            String string = String.valueOf(currentTopic.getReplies()) + " " + mContext.getString(R.string.reply);

            MVHolder.tvReplyNumber.setText(string);
            MVHolder.tvAuthor.setText(currentTopic.getMember().getUsername());
            MVHolder.tvNode.setText(currentTopic.getNode().getName());
            MVHolder.tvPushTime.setText(TimeHelper.RelativeTime(mContext, currentTopic.getCreated()));

            MVHolder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormal(), mImageLoader);
            MVHolder.ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(currentTopic.getMember().getAvatarLarge());

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                }
            });

        }
//        if(holder instanceof ViewHolderItem) {
        //采用更直观的选择语句
        else if (getItemViewType(position) == TYPE_ITEM) {
            ViewHolderItem VHItem = (ViewHolderItem) holder;

            // if(!replyList.isEmpty()) {
            //    因为上一个if语句默认了replylist不可能为空
            ReplyModel replyItem = getItem(position - 1);
//                L.m(replyModel +" No. "+String.valueOf(position-1)+ "in DetailsAdapter");
            VHItem.tvReplyTime.setText(TimeHelper.RelativeTime(mContext, replyItem.getCreated()));

            VHItem.tvReplier.setText(replyItem.getMember().getUsername());

            VHItem.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            VHItem.tvContent.setText(ContentUtils.formatContent(replyItem.getContent()));

            VHItem.tvRow.setText(String.valueOf(position));
            VHItem.ivUserAvatar.setImageUrl(replyItem.getMember().getAvatarNormal(), mImageLoader);
            if(position == getItemCount()-1) {
                VHItem.divider.setVisibility(View.GONE);
            }

        }

    }

    //不需要判空。
    private ReplyModel getItem(int position) {
        return replyList.get(position);
    }


    @Override
    public int getItemCount() {
        return replyList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
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

    public static class ViewHolderItem extends RecyclerView.ViewHolder {

        TextView tvReplier;
        TextView tvReplyTime;
        TextView tvContent;
        TextView tvRow;
        MyNetworkCircleImageView ivUserAvatar;
        View divider;


        public ViewHolderItem(View itemView) {
            super(itemView);

            tvReplier = (TextView) itemView.findViewById(R.id.tvReplier);
            tvReplyTime = (TextView) itemView.findViewById(R.id.tvReplyTime);
            tvContent = (TextView) itemView.findViewById(R.id.tvReplyContent);
            tvRow = (TextView) itemView.findViewById(R.id.tvRow);
            ivUserAvatar = (MyNetworkCircleImageView) itemView.findViewById(R.id.reply_avatar);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
