package im.fdx.v2ex.ui.node

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.logi

class AllNodesAdapterNew(val context: Context, val action: (Node) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<AllNodesAdapterNew.NodeVH>() {

    private var filterMap = mutableMapOf<String, MutableList<Node>>()
    private var map = mapOf<String, MutableList<Node>>()

    fun setData(amap: List<Node>) {
        val listToMap = listToMap(amap)
        filterMap = listToMap.toMutableMap()
        map = listToMap.toMap()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeVH {
        return NodeVH(LayoutInflater.from(parent.context).inflate(R.layout.item_node_with_category, parent, false))
    }

    override fun getItemCount() = filterMap.size

    override fun onBindViewHolder(holder: NodeVH, position: Int) {

        logi("$position")
        val key = filterMap.keys.elementAt(position)
        holder.tvCategory.text = key
        val simpleNodesTextAdapter = SimpleNodesTextAdapter(filterMap[key]!!, action)
        holder.rv.adapter = simpleNodesTextAdapter
    }


  inner class NodeVH(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        internal val tvCategory = itemView.findViewById<TextView>(R.id.tv_node_category).apply {
            isFocusable = true
            setTextIsSelectable(true)
        }
    internal val rv = itemView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv)!!

        init {
            rv.layoutManager = FlexboxLayoutManager(itemView.context, FlexDirection.ROW).apply {
                justifyContent = JustifyContent.FLEX_START
            }
        }
    }


    fun filter(newText: String) {
        if (newText.isEmpty()) {
            filterMap = map.toMutableMap()
            notifyDataSetChanged()
            return
        }

        for (entry in map) {
            val value = entry.value
            val filterNodeModel = value.asSequence().filter {
                it.name.contains(newText) || it.title.contains(newText) || it.title_alternative.contains(newText)
            }.toMutableList()

            if (filterNodeModel.isNotEmpty()) {
                filterMap[entry.key] = filterNodeModel
            } else {
                filterMap.remove(entry.key)
            }
        }

        notifyDataSetChanged()
    }
}