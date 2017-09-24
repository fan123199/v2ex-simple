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
            setHasFixedSize(true)
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
                        mAdapter.map.putAll(nodeModels)
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
//                mAdapter.filter(newText)
                // TODO: 2017/9/2  大重构
                return true
            }
        })
        return true
    }
}

class AllNodesAdapterNew(val map: MutableMap<String, MutableList<NodeModel>> = linkedMapOf(),
                         val context: Context) : RecyclerView.Adapter<AllNodesAdapterNew.NodeVH>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NodeVH {
        return NodeVH(LayoutInflater.from(parent?.context).inflate(R.layout.item_node_with_category, null, false))
    }

    override fun getItemCount() = map.size

    override fun onBindViewHolder(holder: NodeVH, position: Int) {

        XLog.tag("RV_OUTER").e("$position")
        val key = map.keys.elementAt(position)
        holder.tvCategory.text = key
        val simpleNodesTextAdapter = SimpleNodesTextAdapter(map[key]!!)
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
}

