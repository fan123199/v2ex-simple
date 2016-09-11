package im.fdx.v2ex.ui.details;

import android.content.Context;
import android.content.Intent;
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
import im.fdx.v2ex.utils.CircleVImage;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeHelper;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

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
            final TopicModel thisTopic = mHeader;
//            MVHolder.itemView.setTop(position);
            MVHolder.tvTitle.setText(thisTopic.getTitle());

//            MVHolder.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            // TODO: 2016/8/8 能够识别链接和图片
//            MVHolder.tvContent.setTextIsSelectable(true);
            MVHolder.tvContent.setGoodText(ContentUtils.formatContent(thisTopic.getContent_rendered()));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                MVHolder.tvContent.setTransitionName("header");
//            }
            String replyNumberString = String.valueOf(thisTopic.getReplies()) +
                    " " + mContext.getString(R.string.reply);

            MVHolder.tvReplyNumber.setText(replyNumberString);
            MVHolder.tvAuthor.setText(thisTopic.getMember().getUserName());
            MVHolder.tvNode.setText(thisTopic.getNode().getTitle());
            MVHolder.tvNode.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                            openNode(thisTopic);
                }


            });
            MVHolder.tvPushTime.setText(TimeHelper.getRelativeTime(mContext, thisTopic.getCreated()));

            MVHolder.ivAvatar.setImageUrl(thisTopic.getMember().getAvatarNormalUrl(), mImageLoader);
            MVHolder.ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openProfile(thisTopic);

                }


            });

        } else if (getItemViewType(position) == TYPE_ITEM) {
            ViewHolderItem vhItem = (ViewHolderItem) holder;

            // if(!mReplyList.isEmpty()) {
            //    因为上一个if语句默认了replylist不可能为空
            final ReplyModel replyItem = mReplyList.get(position - 1);
            vhItem.tvReplyTime.setText(TimeHelper.getRelativeTime(mContext, replyItem.getCreated()));
            vhItem.tvReplier.setText(replyItem.getMember().getUserName());
            vhItem.tvThanks.setText(String.format(mContext.getResources().
                    getString(R.string.show_thanks), replyItem.getThanks()));
            // TODO: 2016/8/8   vhItem.tvContent.setAutoLinkMask(Linkify.WEB_URLS);
            vhItem.tvContent.setText(ContentUtils.formatContent(replyItem.getContent()));
            vhItem.tvRow.setText(String.valueOf(position));


            vhItem.ivUserAvatar.setImageUrl(replyItem.getMember().getAvatarNormalUrl(), mImageLoader);
//            vhItem.ivUserAvatar.setImageURI(Uri.parse(replyItem.getMember().getAvatarNormalUrl()));

            vhItem.ivUserAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.iv_reply_avatar:
                           openProfile(replyItem);
                            break;
                    }

                }
            });
            if (position == getItemCount() - 1) {
                vhItem.divider.setVisibility(View.GONE);
            }

        }

    }
    private void openNode(TopicModel topicModel) {
        Intent itNode = new Intent();
        itNode.setAction("im.fdx.v2ex.intent.node");
        itNode.putExtra(Keys.KEY_NODE_NAME, topicModel.getNode().getName());
        mContext.startActivity(itNode);
    }

    private void openProfile(Object model) {
        Intent itProfile = new Intent("im.fdx.v2ex.intent.profile");
        if (model instanceof TopicModel) {
            itProfile.putExtra("profile_id", ((TopicModel) model).getMember().getId());
        } else if (model instanceof ReplyModel) {
            itProfile.putExtra("profile_id", ((ReplyModel) model).getMember().getId());
        }
        mContext.startActivity(itProfile);
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
        CircleVImage ivUserAvatar;
        View divider;


        public ViewHolderItem(View itemView) {
            super(itemView);

            tvReplier = (TextView) itemView.findViewById(R.id.tv_replier);
            tvReplyTime = (TextView) itemView.findViewById(R.id.tv_reply_time);
            tvContent = (TextView) itemView.findViewById(R.id.tv_reply_content);
            tvRow = (TextView) itemView.findViewById(R.id.tv_reply_row);
            ivUserAvatar = (CircleVImage) itemView.findViewById(R.id.iv_reply_avatar);
            tvThanks = (TextView) itemView.findViewById(R.id.tv_thanks);
            divider = itemView.findViewById(R.id.divider);
        }
    }

}
