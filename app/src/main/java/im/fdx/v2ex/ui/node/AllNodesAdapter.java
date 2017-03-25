package im.fdx.v2ex.ui.node;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.Keys;

import static android.media.CamcorderProfile.get;

/**
 * Created by fdx on 2016/9/13.
 * fdx will maintain it
 */

public class AllNodesAdapter extends RecyclerView.Adapter<AllNodesAdapter.AllNodeViewHolder> {

    private List<NodeModel> mNodeModels = new ArrayList<>();

    private List<NodeModel> realAllNodes = new ArrayList<>();
    private final ImageLoader imageLoader = VolleyHelper.getInstance().getImageLoader();

    @Override
    public AllNodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_nodes, parent, false);

        return new AllNodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AllNodeViewHolder holder, int position) {
        final NodeModel node = mNodeModels.get(position);

//        getNodeIcon(node.getId(),holder);

        String a = String.format(Locale.CHINA, "%s (%s)", node.getTitle(), node.getTopics());
        holder.tvNodeName.setText(a);

        holder.tvNodeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), NodeActivity.class);
                intent.putExtra(Keys.KEY_NODE_NAME, node.getName());
                v.getContext().startActivity(intent);

            }
        });
//        holder.tvNodeHeader.setText(node.getHeader());
//        holder.nivNodeIcon.setImageUrl(node.getAvatarLargeUrl(),imageLoader);

    }



    @Override
    public int getItemCount() {
        return mNodeModels.size();
    }


    public void setAllData(List<NodeModel> nodeModels) {
        mNodeModels = nodeModels;
        realAllNodes = nodeModels;
    }

    public void filter(String newText) {

        if (TextUtils.isEmpty(newText)) {
            mNodeModels = realAllNodes;
            notifyDataSetChanged();
            return;
        }
        ArrayList<NodeModel> newNodeModels = new ArrayList<>();
        for (NodeModel nodeModel : realAllNodes) {
            if (nodeModel.getName().contains(newText)
                    || nodeModel.getTitle().contains(newText) ||
                    (nodeModel.getTitle_alternative() != null && nodeModel.getTitle_alternative().contains(newText))) {
                newNodeModels.add(nodeModel);
            }
        }

        mNodeModels = newNodeModels;
        notifyDataSetChanged();


    }

    public static class AllNodeViewHolder extends RecyclerView.ViewHolder {

        public TextView tvNodeName;

        public NetworkImageView nivNodeIcon;

        public AllNodeViewHolder(View itemView) {
            super(itemView);
            tvNodeName = (TextView) itemView.findViewById(R.id.tv_node_name);
//            nivNodeIcon = (NetworkImageView) itemView.findViewById(R.id.iv_node_image);
//            nivNodeIcon.setDefaultImageResId(R.drawable.ic_profile);


        }
    }
}
