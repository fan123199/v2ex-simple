package im.fdx.v2ex.ui.node;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.MyGsonRequest;
import im.fdx.v2ex.utils.Keys;

import static android.media.CamcorderProfile.get;

/**
 * Created by fdx on 2016/9/13.
 * fdx will maintain it
 */

public class AllNodesAdapter extends RecyclerView.Adapter<AllNodesAdapter.MyViewHolder> {

    private List<NodeModel> nodeModels = new ArrayList<>();
    private final ImageLoader imageLoader = VolleyHelper.getInstance().getImageLoader();

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_nodes, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final NodeModel node = nodeModels.get(position);

//        getNodeIcon(node.getId(),holder);

        String a = String.format(Locale.CHINA,"%s (%s)",node.getTitle(),node.getTopics());
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

    /**
     * get node icon because of api of all node sucks.
     * @param id
     * @param holder
     */
    private void getNodeIcon(Long id, final MyViewHolder holder) {
        String url = JsonManager.API_NODE + "?id=" + id;
        MyGsonRequest<NodeModel> simple = new MyGsonRequest<>(url, NodeModel.class, new Response.Listener<NodeModel>() {
            @Override
            public void onResponse(NodeModel response) {

                holder.nivNodeIcon.setImageUrl(response.getAvatarLargeUrl(), imageLoader);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleyHelper.getInstance().addToRequestQueue(simple);

    }

    @Override
    public int getItemCount() {
        return nodeModels.size();
    }


    void updateData(List<NodeModel> nodeModels) {
        this.nodeModels = nodeModels;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tvNodeName;

        public NetworkImageView nivNodeIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvNodeName = (TextView) itemView.findViewById(R.id.tv_node_name);
//            nivNodeIcon = (NetworkImageView) itemView.findViewById(R.id.iv_node_image);
//            nivNodeIcon.setDefaultImageResId(R.drawable.ic_profile);


        }
    }
}
