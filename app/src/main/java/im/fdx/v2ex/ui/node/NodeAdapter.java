package im.fdx.v2ex.ui.node;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import im.fdx.v2ex.R;

/**
 * Created by fdx on 16/4/28.
 * 暂时弃用，将来用于显示节点的所有主题。
 */
public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    private NodeModel mNodeMode = new NodeModel();
    private Context context;

    public NodeAdapter(Context context, NodeModel nodeModel) {
        mNodeMode = nodeModel;
        this.context = context;
    }

    @Override
    public NodeAdapter.NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node,parent,false);
        return new NodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        holder.tvNodeName.setText(mNodeMode.getName());
        Picasso.with(context).load(mNodeMode.getAvatarLargeUrl()).into(holder.ivNode);
        holder.tvNodeHeader.setText(mNodeMode.getHeader());
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder{

        public TextView tvNodeName;
        public ImageView ivNode;
        public TextView tvNodeHeader;

        public NodeViewHolder(View itemView) {
            super(itemView);
            tvNodeName = (TextView) itemView.findViewById(R.id.tv_node_name);
            ivNode = (ImageView) itemView.findViewById(R.id.iv_node_image);
            tvNodeHeader = (TextView) itemView.findViewById(R.id.tv_node_header);


        }
    }
}
