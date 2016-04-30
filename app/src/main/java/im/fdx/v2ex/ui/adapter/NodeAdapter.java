package im.fdx.v2ex.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.MySingleton;

/**
 * Created by fdx on 16/4/28.
 * 暂时弃用，将来用于显示节点的所有主题。
 */
public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    NodeModel mNodeMode = new NodeModel();
    ImageLoader mImageLoader;

    public NodeAdapter(NodeModel nodeModel) {
        mNodeMode = nodeModel;
        mImageLoader = MySingleton.getInstance().getImageLoader();

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

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder{

        public TextView tvNodeName;
        public NetworkImageView ivNode;

        public NodeViewHolder(View itemView) {
            super(itemView);
            tvNodeName = (TextView) itemView.findViewById(R.id.tvNodeName);
            ivNode = (NetworkImageView) itemView.findViewById(R.id.ivNodeImage);


        }
    }
}
