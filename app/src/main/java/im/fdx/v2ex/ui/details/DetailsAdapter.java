package im.fdx.v2ex.ui.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.elvishew.xlog.XLog;

import org.jsoup.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.BaseModel;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.view.CircleVImage;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeHelper;
import im.fdx.v2ex.view.GoodTextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.value;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.media.CamcorderProfile.get;

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
    private ImageLoader mImageLoader = VolleyHelper.getInstance().getImageLoader();
    private Context mContext;
    private List<BaseModel> mAllList;
    private long mTopicId;
    private int maxWith;
    private String verifyCode;

    public DetailsAdapter(Context context, TopicModel header, List<ReplyModel> replyList) {
        mContext = context;
        mHeader = header;
        mReplyList = replyList;
    }

    public DetailsAdapter(Context context, List<BaseModel> allList) {
        mContext = context;
        mAllList = allList;
    }

    public void setTopicId(long topicId) {
        mTopicId = topicId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_topic_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 30);
            view.setLayoutParams(lp);
//            HintUI.m("align : " + String.valueOf(lp.alignWithParent));
//            HintUI.m(String.valueOf(view.isScrollContainer()));
//            HintUI.m(String.valueOf(parent.isScrollContainer()));
//            view.stopNestedScroll();

            return new TopicsRVAdapter.MainViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_reply_view, parent, false);
            return new ItemViewHolder(view);
        }
        throw new RuntimeException(" No type that matches " + viewType + " + Make sure using types correctly");
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder instanceof TopicsRVAdapter.MainViewHolder)
            maxWith = ((TopicsRVAdapter.MainViewHolder) holder).tvContent.getHeight();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

//        if(holder instanceof TopicsRVAdapter.MainViewHolder) {
        //采用更直观的选择语句
        if (getItemViewType(position) == TYPE_HEADER) {

            TopicsRVAdapter.MainViewHolder MVHolder = (TopicsRVAdapter.MainViewHolder) holder;
            final TopicModel topic = ((TopicModel) mAllList.get(position));
//            MVHolder.itemView.setTop(position);
            MVHolder.tvTitle.setText(topic.getTitle());
            MVHolder.tvContent.setSelected(true);
            MVHolder.tvContent.setGoodText(topic.getContent_rendered());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                MVHolder.tvContent.setTransitionName("header");
//            }
            String replyNumberString = String.valueOf(topic.getReplies()) +
                    " " + mContext.getString(R.string.reply);

            MVHolder.tvReplyNumber.setText(replyNumberString);
            MVHolder.tvAuthor.setText(topic.getMember().getUsername());
            MVHolder.tvNode.setText(topic.getNode().getTitle());
            TopicsRVAdapter.MyOnClickListener l = new TopicsRVAdapter.MyOnClickListener(topic, mContext);
            MVHolder.tvNode.setOnClickListener(l);
            MVHolder.tvCreated.setText(TimeHelper.getRelativeTime(topic.getCreated()));

            MVHolder.ivAvatar.setImageUrl(topic.getMember().getAvatarNormalUrl(), mImageLoader);
            MVHolder.ivAvatar.setOnClickListener(l);

        } else if (getItemViewType(position) == TYPE_ITEM) {
            final ItemViewHolder itemVH = (ItemViewHolder) holder;
            // if(!mReplyList.isEmpty()) {
            //    因为上一个if语句默认了replylist不可能为空
            final ReplyModel replyItem = (ReplyModel) mAllList.get(position);


            if (MyApp.getInstance().isLogin()) {
                itemVH.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuInflater menuInflater = ((Activity) mContext).getMenuInflater();
                        menuInflater.inflate(R.menu.menu_reply, menu);

                        MenuItem.OnMenuItemClickListener menuListener = new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_reply:


                                        EditText editText = (EditText) ((Activity) mContext).findViewById(R.id.et_post_reply);


                                        String text = String.format("@%s ", replyItem.getMember().getUsername());

                                        if (!editText.getText().toString().contains(text)) {
                                            SpannableString spanString = new SpannableString(text);

                                            ForegroundColorSpan span = null;
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                                span = new ForegroundColorSpan(mContext.getColor(R.color.primary));
                                            } else {
                                                span = new ForegroundColorSpan(Color.BLACK);
                                            }
                                            spanString.setSpan(span, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                            editText.append(spanString);
                                        }

                                        //span // TODO: 2017/3/22
//                                    int length  =editText.length();
//                                    XLog.tag(TAG).d("length" +length);
//                                    Object name = new Object();
//                                    editText.getText().setSpan(name,length,length + text.length(), (int) replyItem.getId());
                                        editText.setSelection(editText.length());

                                        editText.requestFocus();

                                        return true;
                                    case R.id.menu_thank:

                                        XLog.tag(TAG).d("hehe" + verifyCode);
                                        if (verifyCode == null) {
                                            return true;
                                        }
                                        RequestBody body = new FormBody.Builder().add("t", verifyCode).build();

                                        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                                                .headers(HttpHelper.baseHeaders)
                                                .url("https://www.v2ex.com/thank/reply/" + replyItem.getId())
                                                .post(body)
                                                .build()).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                JsonManager.handleError();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                                if (response.code() == 200) {
                                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            HintUI.t(mContext, "感谢成功");
                                                            itemVH.tvThanks.setText(String.format(mContext.getResources().
                                                                    getString(R.string.show_thanks), replyItem.getThanks() + 1));
                                                        }
                                                    });
                                                } else {
                                                    JsonManager.handleError();
                                                }
                                            }
                                        });

                                        break;
                                }
                                return false;
                            }
                        };

                        menu.findItem(R.id.menu_reply).setOnMenuItemClickListener(menuListener);

                        menu.findItem(R.id.menu_thank).setOnMenuItemClickListener(menuListener);


                    }
                });
            }



            itemVH.tvReplyTime.setText(TimeHelper.getRelativeTime(replyItem.getCreated()));
            itemVH.tvReplier.setText(replyItem.getMember().getUsername());
            itemVH.tvThanks.setText(String.format(mContext.getResources().
                    getString(R.string.show_thanks), replyItem.getThanks()));
//            itemVH.tvContent.setSelected(true);
            itemVH.tvContent.setGoodText(replyItem.getContent_rendered());
            itemVH.tvRow.setText(String.valueOf(position));

            itemVH.ivUserAvatar.setImageUrl(replyItem.getMember().getAvatarNormalUrl(), mImageLoader);

            itemVH.ivUserAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.iv_reply_avatar:
                            Intent itProfile = new Intent("im.fdx.v2ex.intent.profile");
                            itProfile.putExtra("username", replyItem.getMember().getUsername());
                            mContext.startActivity(itProfile);
                            break;
                    }

                }
            });
            if (position == getItemCount() - 1) {
                itemVH.divider.setVisibility(View.GONE);
            }

        }

    }

    private String getReplyId() {
        return null;
    }

    private String getValue() {
        return null;
    }

    @Override
    public int getItemCount() {
        return mAllList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    //我重用了MainAdapter中的MainViewHolder

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvReplier;
        TextView tvReplyTime;
        GoodTextView tvContent;
        TextView tvRow;
        TextView tvThanks;
        CircleVImage ivUserAvatar;
        View divider;


        public ItemViewHolder(View itemView) {
            super(itemView);

            tvReplier = (TextView) itemView.findViewById(R.id.tv_replier);
            tvReplyTime = (TextView) itemView.findViewById(R.id.tv_reply_time);
            tvContent = (GoodTextView) itemView.findViewById(R.id.tv_reply_content);
            tvRow = (TextView) itemView.findViewById(R.id.tv_reply_row);
            ivUserAvatar = (CircleVImage) itemView.findViewById(R.id.iv_reply_avatar);
            tvThanks = (TextView) itemView.findViewById(R.id.tv_thanks);
            divider = itemView.findViewById(R.id.divider);

        }
    }

}
