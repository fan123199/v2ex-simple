package im.fdx.v2ex.ui.node

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import im.fdx.v2ex.R

class AllNodesAdapterNew(val context: Context) : RecyclerView.Adapter<AllNodesAdapterNew.NodeVH>() {

    private var filterMap = mutableMapOf<String, MutableList<Node>>()
    private var map = mapOf<String, MutableList<Node>>()

    fun setData(amap: MutableMap<String, MutableList<Node>>) {
        filterMap = LinkedHashMap(amap)
        map = LinkedHashMap(amap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeVH {
        return NodeVH(LayoutInflater.from(parent?.context).inflate(R.layout.item_node_with_category, null, false))
    }

    override fun getItemCount() = filterMap.size

    override fun onBindViewHolder(holder: NodeVH, position: Int) {

        XLog.tag("RV_OUTER").e("$position")
        val key = filterMap.keys.elementAt(position)
        holder.tvCategory.text = key
        val simpleNodesTextAdapter = SimpleNodesTextAdapter(filterMap[key]!!)
        holder.rv.setHasFixedSize(true)
        simpleNodesTextAdapter.setHasStableIds(true)
        holder.rv.adapter = simpleNodesTextAdapter
    }


    inner class NodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val tvCategory = itemView.findViewById<TextView>(R.id.tv_node_category).apply {
            isFocusable = true
            setTextIsSelectable(true)
        }
        internal val rv = itemView.findViewById<RecyclerView>(R.id.rv)!!

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
            val filterNodeModel = value.filter {
                it.name.contains(newText) || it.title.contains(newText) || it.title_alternative.contains(newText)
            }.toMutableList()

            if (filterNodeModel.isNotEmpty()) {
                filterMap[entry.key] = filterNodeModel
            } else {
                filterMap.remove(entry.key)
            }
        }

        Log.e("fffff", filterMap.toString())

        notifyDataSetChanged()
    }
}