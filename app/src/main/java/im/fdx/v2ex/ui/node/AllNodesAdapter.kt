package im.fdx.v2ex.ui.node

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.load
import java.util.*

/**
 * Created by fdx on 2016/9/13.
 * fdx will maintain it
 */

class AllNodesAdapter(private val context: Context, private val isShowImg: Boolean) : RecyclerView.Adapter<AllNodesAdapter.AllNodeViewHolder>() {
    private var mNodeModels: MutableList<NodeModel> = ArrayList()

    private var realAllNodes: MutableList<NodeModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllNodeViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_nodes, parent, false)

        return AllNodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllNodeViewHolder, position: Int) {
        val node = mNodeModels[position]

        if (isShowImg) {
            holder.ivNodeIcon.load(node.avatarLargeUrl)
        } else {
            holder.ivNodeIcon.visibility = View.GONE
        }
        holder.tvNodeName.text = String.format(Locale.CHINA, "%s (%s)", node.title, node.topics)

        holder.itemView.setOnClickListener { v ->
            val intent = Intent().apply {
                setClass(v.context, NodeActivity::class.java)
                putExtra(Keys.KEY_NODE_NAME, node.name)
            }
            v.context.startActivity(intent)
        }

    }


    override fun getItemCount() = mNodeModels.size


    fun setAllData(nodeModels: MutableList<NodeModel>) {
        mNodeModels = nodeModels
        realAllNodes = nodeModels
    }

    fun addAll(nodeModels: List<NodeModel>) {
        mNodeModels.addAll(nodeModels)
    }

    fun clear() {
        mNodeModels.clear()
    }

    fun filter(newText: String) {

        if (TextUtils.isEmpty(newText)) {
            mNodeModels = realAllNodes
            notifyDataSetChanged()
            return
        }

        mNodeModels = realAllNodes
                .filter {
                    it.name.contains(newText) || it.title.contains(newText)
                            || it.title_alternative.contains(newText)
                }
                .toMutableList()
        notifyDataSetChanged()
    }

    class AllNodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNodeName: TextView = itemView.findViewById(R.id.tv_node_name)
        var ivNodeIcon: ImageView = itemView.findViewById(R.id.iv_node_image)
    }
}
