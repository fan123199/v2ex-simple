package im.fdx.v2ex.ui.node

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.utils.extensions.dealError
import im.fdx.v2ex.utils.extensions.initTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import java.io.IOException

class AllNodesActivity() : AppCompatActivity() {
    private lateinit var mAdapter: AllNodesAdapterNew
    private lateinit var swipe: SwipeRefreshLayout

    lateinit var rvNode: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_nodes)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "所有节点"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvNode = findViewById(R.id.rv_node)
        with(rvNode) {

            //这里是后续不卡的关键，但是第一次滑动还是卡
            setHasFixedSize(true) //要做filter，不能用
            setItemViewCacheSize(20);
        }


        swipe = findViewById(R.id.swipe_container)
        swipe.initTheme()
        swipe.setOnRefreshListener { getAllNodes() }
        swipe.isRefreshing = true

        mAdapter = AllNodesAdapterNew(context = this)
        rvNode.layoutManager = LinearLayoutManager(this)
        mAdapter.setHasStableIds(true)
        getAllNodes()
    }


    private fun getAllNodes() {
        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(NetManager.URL_ALL_NODE_WEB).build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dealError(-1, swipe)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: okhttp3.Response) {

                        if (response.code() != 200) {
                            dealError(response.code())
                            return
                        }

                        val nodeModels = NetManager.getAllNode(response.body()?.string()!!)
                        mAdapter.setData(nodeModels)
                        runOnUiThread {
                            swipe.isRefreshing = false
                            rvNode.adapter = mAdapter
                        }
                    }
                })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_all_node, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuItemCompat = menu.findItem(R.id.search_node)
        val searchView = menuItemCompat.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String) = false
            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter.filter(newText)
                return true
            }
        })
        return true
    }
}

class AllNodesAdapterNew(val context: Context) : RecyclerView.Adapter<AllNodesAdapterNew.NodeVH>() {

    private var filterMap = mutableMapOf<String, MutableList<NodeModel>>()
    private var map = mapOf<String, MutableList<NodeModel>>()

    fun setData(amap: MutableMap<String, MutableList<NodeModel>>) {
        filterMap = LinkedHashMap(amap)
        map = LinkedHashMap(amap)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NodeVH {
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

