package im.fdx.v2ex.ui.node

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.load
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by fdx on 2016/9/13.
 * 所有节点页面
 */
@Deprecated("不够细化，一页显示太少，被淘汰")
class AllNodesAdapter(val isShowImg: Boolean = false) : RecyclerView.Adapter<AllNodesAdapter.AllNodeViewHolder>() {

    private var mNodes: MutableList<Node> = ArrayList()
    private var realAllNodes: MutableList<Node> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AllNodeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_all_nodes, parent, false))

    override fun onBindViewHolder(holder: AllNodeViewHolder, position: Int) {
        XLog.tag("RV_INNER").e("$position")
        val node = mNodes[position]

        if (isShowImg) holder.ivNodeIcon.load(node.avatarLargeUrl)
        else {
            holder.ivNodeIcon.visibility = View.GONE
        }
        holder.tvNodeName.text = node.title
        holder.itemView.setOnClickListener {
            it.context.startActivity<NodeActivity>(Keys.KEY_NODE_NAME to node.name)
        }

    }

    override fun getItemCount() = mNodes.size

    fun setAllData(nodes: MutableList<Node>) {
        mNodes = nodes
        realAllNodes = nodes
    }

    fun addAll(nodes: List<Node>) {
        mNodes.addAll(nodes)
    }

    fun clear() = mNodes.clear()

    fun filter(newText: String) {

        if (newText.isEmpty()) {
            mNodes = realAllNodes
            notifyDataSetChanged()
            return
        }

        mNodes = realAllNodes.filter {
            it.name.contains(newText) || it.title.contains(newText) || it.title_alternative.contains(newText)
        }.toMutableList()
        notifyDataSetChanged()
    }

    class AllNodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNodeName: TextView = itemView.findViewById(R.id.tv_node_name)
        var ivNodeIcon: ImageView = itemView.findViewById(R.id.iv_node_image)
    }
}