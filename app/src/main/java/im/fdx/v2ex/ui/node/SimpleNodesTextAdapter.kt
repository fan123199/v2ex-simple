package im.fdx.v2ex.ui.node

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.Keys
import org.jetbrains.anko.startActivity

class SimpleNodesTextAdapter(private var mNodes: MutableList<Node> = mutableListOf<Node>())
    : RecyclerView.Adapter<SimpleNodesTextAdapter.SimpleVH>() {

    override fun onBindViewHolder(holder: SimpleVH, position: Int) {
        holder.tvTitle.text = mNodes[position].title
        holder.tvTitle.setOnClickListener {
            it.context.startActivity<NodeActivity>(Keys.KEY_NODE_NAME to mNodes[position].name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            SimpleVH(LayoutInflater.from(parent?.context).inflate(R.layout.item_node_simple, parent, false))

    override fun getItemCount() = mNodes.size

    inner class SimpleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView as TextView
    }
}