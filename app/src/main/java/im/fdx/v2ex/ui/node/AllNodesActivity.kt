package im.fdx.v2ex.ui.node

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import java.io.IOException
import java.util.*

class AllNodesActivity : AppCompatActivity() {
    private var mAdapter: AllNodesAdapter? = null;


    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when {
                msg.what == 0 -> mAdapter!!.notifyDataSetChanged()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_nodes)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "所有节点"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rvNode = findViewById(R.id.rv_node) as RecyclerView

        HttpHelper.OK_CLIENT.newCall(Request.Builder().url(NetManager.URL_ALL_NODE).build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val type = object : TypeToken<ArrayList<NodeModel>>() {}.type
                        val nodeModels = NetManager.myGson.fromJson<ArrayList<NodeModel>>(response.body()?.string(), type)
                        mAdapter?.setAllData(nodeModels)
                        handler.sendEmptyMessage(0)
                    }
                })


        mAdapter = AllNodesAdapter(this, false)
        rvNode.layoutManager = StaggeredGridLayoutManager(3, VERTICAL)
        rvNode.adapter = mAdapter
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_node, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuItemCompat = menu.findItem(R.id.search_node)
        val searchView = menuItemCompat.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter?.filter(newText)
                return false
            }
        })
        return true
    }

}
