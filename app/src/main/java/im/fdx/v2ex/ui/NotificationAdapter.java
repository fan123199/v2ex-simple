package im.fdx.v2ex.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NotificationModel;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.ui.main.MainActivity;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.Keys;

/**
 * Created by fdx on 2017/3/24.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;
    public List<NotificationModel> mModels;


    private int number = -1;

    public void setNumber(int num) {
        this.number = num;

    }


    public NotificationAdapter(Context context, List<NotificationModel> models) {
        mContext = context;
        mModels = models;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NotificationViewHolder nholder = (NotificationViewHolder) holder;

        if (position >= number) {
            nholder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.list_background));
        }

        final NotificationModel model = mModels.get(position);

        nholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDetail = new Intent(mContext, DetailsActivity.class);
                intentDetail.putExtra(Keys.KEY_TOPIC_ID, model.getTopic().getId());
                mContext.startActivity(intentDetail);
            }
        });


        nholder.tvAction.setText(model.getType());
        nholder.tvContent.setText(model.getContent());
        Picasso.with(mContext).load(model.getMember().getAvatarNormalUrl()).into(nholder.ivAvatar);
        nholder.tvUsername.setText(model.getMember().getUsername());
        nholder.tvTime.setText(model.getTime());
        nholder.tvTopicTitle.setText(model.getTopic().getTitle());

        nholder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MemberActivity.class);
                intent.putExtra(Keys.KEY_USERNAME, model.getMember().getUsername());
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mModels.size();
    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTopicTitle;
        public TextView tvUsername;
        public TextView tvTime;
        public TextView tvContent;
        public TextView tvAction;
        public CircleImageView ivAvatar;

        public NotificationViewHolder(View itemView) {
            super(itemView);

            tvTopicTitle = (TextView) itemView.findViewById(R.id.tv_topic_title);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvContent = (TextView) itemView.findViewById(R.id.content_notification);
            tvAction = (TextView) itemView.findViewById(R.id.tv_action_notification);
            ivAvatar = (CircleImageView) itemView.findViewById(R.id.iv_avatar_notification);
        }
    }
}

