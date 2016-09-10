package im.fdx.v2ex.ui.details;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.main.MainAdapter;
import im.fdx.v2ex.utils.ContentUtils;
import im.fdx.v2ex.utils.MyNetworkCircleImageView;
import im.fdx.v2ex.utils.MyOnClickListener;
import im.fdx.v2ex.utils.TimeHelper;

/**
 * Created by fdx on 15-9-7.
 * 详情页的Adapter。
 */
public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DetailsAdapter.class.getSimpleName();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private TopicModel mHeader;
    private List<ReplyModel> mReplyList = new ArrayList<>();
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private Context mContext;

    public DetailsAdapter(Context context, TopicModel header, List<ReplyModel> replyList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mHeader = header;
        this.mReplyList = replyList;
        mImageLoader = VolleyHelper.getInstance().getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = mInflater.inflate(R.layout.item_topic_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 30);
            view.setLayoutParams(lp);
//            HintUI.m("align : " + String.valueOf(lp.alignWithParent));
//            HintUI.m(String.valueOf(view.isScrollContainer()));
//            HintUI.m(String.valueOf(parent.isScrollContainer()));
//            view.stopNestedScroll();

            return new MainAdapter.MainViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.item_reply_view, parent, false);
            return new ViewHolderItem(view);
        }
        throw new RuntimeException(" No type that matches " + viewType + " + Make sure using types correctly");
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

//        if(holder instanceof MainAdapter.MainViewHolder) {
        //采用更直观的选择语句
        if (getItemViewType(position) == TYPE_HEADER) {

            MainAdapter.MainViewHolder MVHolder = (MainAdapter.MainViewHolder) holder;
            final TopicModel currentTopic = mHeader;
//            MVHolder.itemView.setTop(position);
            MVHolder.tvTitle.setText(currentTopic.getTitle());

//            MVHolder.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            // TODO: 2016/8/8 能够识别链接和图片
            MVHolder.tvContent.setTextIsSelectable(true);
            MVHolder.tvContent.setText(ContentUtils.formatContentSimple(currentTopic.getContent()));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                MVHolder.tvContent.setTransitionName("header");
//            }
            String replyNumberString = String.valueOf(currentTopic.getReplies()) +
                    " " + mContext.getString(R.string.reply);

            MVHolder.tvReplyNumber.setText(replyNumberString);
            MVHolder.tvAuthor.setText(currentTopic.getMember().getUsername());
            MVHolder.tvNode.setText(currentTopic.getNode().getTitle());
            MVHolder.tvNode.setOnClickListener(new MyOnClickListener(mContext, currentTopic));
            MVHolder.tvPushTime.setText(TimeHelper.getRelativeTime(mContext, currentTopic.getCreated()));

            MVHolder.ivAvatar.setImageUrl(currentTopic.getMember().getAvatarNormal(), mImageLoader);

            MVHolder.ivAvatar.setOnClickListener(new MyOnClickListener(mContext, currentTopic));

        }

        else if (getItemViewType(position) == TYPE_ITEM) {
            ViewHolderItem VHItem = (ViewHolderItem) holder;

            // if(!mReplyList.isEmpty()) {
            //    因为上一个if语句默认了replylist不可能为空
            ReplyModel replyItem = mReplyList.get(position - 1);
            VHItem.tvReplyTime.setText(TimeHelper.getRelativeTime(mContext, replyItem.getCreated()));
            VHItem.tvReplier.setText(replyItem.getMember().getUsername());
            VHItem.tvThanks.setText(String.format(mContext.getResources().
                    getString(R.string.show_thanks), replyItem.getThanks()));
            // TODO: 2016/8/8   VHItem.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            VHItem.tvContent.setText(ContentUtils.formatContent(replyItem.getContent()));
            VHItem.tvRow.setText(String.valueOf(position));
            VHItem.ivUserAvatar.setImageUrl(replyItem.getMember().getAvatarNormal(), mImageLoader);
            if (position == getItemCount() - 1) {
                VHItem.divider.setVisibility(View.GONE);
            }

        }

    }


    @Override
    public int getItemCount() {
        return mReplyList.size() + 1;
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
        TextView tvThanks;
        MyNetworkCircleImageView ivUserAvatar;
        View divider;


        public ViewHolderItem(View itemView) {
            super(itemView);

            tvReplier = (TextView) itemView.findViewById(R.id.tv_replier);
            tvReplyTime = (TextView) itemView.findViewById(R.id.tv_reply_time);
            tvContent = (TextView) itemView.findViewById(R.id.tv_reply_content);
            tvRow = (TextView) itemView.findViewById(R.id.tv_reply_row);
            ivUserAvatar = (MyNetworkCircleImageView) itemView.findViewById(R.id.reply_avatar);
            tvThanks = (TextView) itemView.findViewById(R.id.tv_thanks);
            divider = itemView.findViewById(R.id.divider);
        }
    }

}
