package im.fdx.v2ex.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.ui.node.NodeActivity;

/**
 * Created by fdx on 2016/9/10.
 * fdx will maintain it
 */
public class MyOnClickListener implements View.OnClickListener {
    private final TopicModel mCurrentTopic;
    private Context mContext;

    public MyOnClickListener(Context context, TopicModel currentTopic) {
        mCurrentTopic = currentTopic;
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_node:
                Intent itNode = new Intent();
                itNode.setAction("im.fdx.v2ex.intent.node");
                itNode.putExtra(Keys.KEY_NODE_NAME, mCurrentTopic.getNode().getName());
                mContext.startActivity(itNode);
                break;
            case R.id.iv_avatar:
                Uri uri = Uri.parse(mCurrentTopic.getMember().getAvatarLarge());
                Intent intent = new Intent();
                intent.setAction("im.fdx.v2ex.intent.profile");
                mContext.startActivity(intent);
                break;
        }
    }
}
