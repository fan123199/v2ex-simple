package im.fdx.v2ex.ui.node;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.VolleyHelper;

/**
 * Created by fdx on 16/4/28.
 * 暂时弃用，将来用于显示节点的所有主题。
 */
public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    private NodeModel mNodeMode = new NodeModel();
    private ImageLoader mImageLoader;

    public NodeAdapter(NodeModel nodeModel) {
        mNodeMode = nodeModel;
        mImageLoader = VolleyHelper.getInstance().getImageLoader();

    }

    @Override
    public NodeAdapter.NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node,parent,false);
        return new NodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        holder.tvNodeName.setText(mNodeMode.getName());
        holder.ivNode.setImageUrl(mNodeMode.getAvatarLargeUrl(),mImageLoader);
        holder.tvNodeHeader.setText(mNodeMode.getHeader());
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder{

        public TextView tvNodeName;
        public NetworkImageView ivNode;
        public TextView tvNodeHeader;

        public NodeViewHolder(View itemView) {
            super(itemView);
            tvNodeName = (TextView) itemView.findViewById(R.id.tv_node_name);
            ivNode = (NetworkImageView) itemView.findViewById(R.id.iv_node_image);
            tvNodeHeader = (TextView) itemView.findViewById(R.id.tv_node_header);


        }
    }
}
