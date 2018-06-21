package im.fdx.v2ex.ui.node

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.utils.extensions.dealError
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.synthetic.main.activity_all_nodes.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import java.io.IOException

class AllNodesActivity : BaseActivity() {
    private lateinit var mAdapter: AllNodesAdapterNew
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_nodes)

        setUpToolbar(getString(R.string.all_nodes))

        rv_node.apply {
            //这里是后续不卡的关键，但是第一次滑动还是卡
            setHasFixedSize(true) //要做filter，不能用
            setItemViewCacheSize(20);
        }

        val linearLayoutManager = object : LinearLayoutManager(this) {
            override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
                return 300
            }
        }
        rv_node.layoutManager = linearLayoutManager

        swipe_container.initTheme()
        swipe_container.setOnRefreshListener { getAllNodes() }


        mAdapter = AllNodesAdapterNew(context = this)

        mAdapter.setHasStableIds(true)
        swipe_container.isRefreshing = true
        getAllNodes()
    }

    private fun getAllNodes() {

        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(NetManager.URL_ALL_NODE_WEB).build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dealError(-1, swipe_container)
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
                            swipe_container.isRefreshing = false
                            rv_node.adapter = mAdapter
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

