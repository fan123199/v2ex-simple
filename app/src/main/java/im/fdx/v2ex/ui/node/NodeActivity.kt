package im.fdx.v2ex.ui.node

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.squareup.picasso.Picasso
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.main.NewTopicActivity
import im.fdx.v2ex.ui.main.TopicModel
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.utils.HintUI
import im.fdx.v2ex.utils.Keys
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*


class NodeActivity : AppCompatActivity() {
    private lateinit var rlNodeList: View
    private lateinit var rlNodeHeader: View
    private lateinit var ivNodeIcon: ImageView
    private lateinit var tvNodeHeader: TextView
    private lateinit var tvNodeNum: TextView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    internal var mTopicModels: MutableList<TopicModel> = ArrayList()
    private var mAdapter: TopicsRVAdapter? = null

    @SuppressLint("HandlerLeak")
    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {

            XLog.i("get handler msg " + msg.what)
            when {
                msg.what == MSG_GET_NODE_INFO -> {

                    Picasso.with(this@NodeActivity).load(mNodeModel?.avatarLargeUrl).into(ivNodeIcon)
                    XLog.d(mNodeModel?.title)
                    collapsingToolbarLayout?.title = mNodeModel?.title
                    collapsingToolbarLayout?.isTitleEnabled = true
                    tvNodeHeader.text = mNodeModel?.header
                    tvNodeNum.text = getString(R.string.topic_number, mNodeModel?.topics)
                }
                msg.what == MSG_GET_TOPICS -> {
                    mAdapter!!.notifyDataSetChanged()
                    mSwipeRefreshLayout.isRefreshing = false
                }
                msg.what == MSG_ERROR_AUTH -> {
                    HintUI.toa(this@NodeActivity, "需要登录")
                    finish()
                }
            }
        }
    }
    private lateinit var nodeName: String
    private var mNodeModel: NodeModel? = null
    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_node)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) //很关键，不会一闪而过一个东西
        toolbar.setNavigationOnClickListener { onBackPressed() }


        val appBarLayout: AppBarLayout = findViewById(R.id.appbar_node)

        collapsingToolbarLayout = findViewById(R.id.ctl_node)

        appBarLayout.addOnOffsetChangedListener { appBarLayout1, verticalOffset ->

            val maxScroll = appBarLayout1.totalScrollRange
            val percentage = Math.abs(verticalOffset).toDouble() / maxScroll.toDouble()
            handleAlphaOnTitle(percentage.toFloat())
        }


        val fabNode: FloatingActionButton = findViewById(R.id.fab_node)
        fabNode.setOnClickListener {
            val intent = Intent(this@NodeActivity, NewTopicActivity::class.java)
            intent.putExtra(Keys.KEY_NODE_NAME, nodeName)
            startActivity(intent)
        }
        if (!MyApp.get().isLogin()) {
            fabNode.hide()
        }

        rlNodeHeader = findViewById(R.id.rl_node_header)
        rlNodeList = findViewById(R.id.rl_node_list)
        ivNodeIcon = findViewById(R.id.iv_node_image)
        tvNodeHeader = findViewById(R.id.tv_node_details)
        tvNodeNum = findViewById(R.id.tv_topic_num)


        val rvTopicsOfNode: RecyclerView = findViewById(R.id.rv_topics_of_node)

        mAdapter = TopicsRVAdapter(this, mTopicModels)
        rvTopicsOfNode.adapter = mAdapter
        rvTopicsOfNode.layoutManager = LinearLayoutManager(this)

        mSwipeRefreshLayout = findViewById(R.id.swipe_of_node)

        mSwipeRefreshLayout.isRefreshing = true
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_orange)
        mSwipeRefreshLayout.setOnRefreshListener { getNodeInfoAndTopicByOK(nodeName) }
        parseIntent(intent)

    }

    private fun handleAlphaOnTitle(percentage: Float) {
        XLog.tag("collapse").d(percentage)
        when (percentage) {
            in 0..1 -> rlNodeHeader.alpha = 1 - percentage
        }
    }

    private fun parseIntent(intent: Intent) {

        if (intent.data != null) {
            val params = intent.data.pathSegments
            nodeName = params[1]
        } else if (intent.getStringExtra(Keys.KEY_NODE_NAME) != null) {
            nodeName = intent.getStringExtra(Keys.KEY_NODE_NAME)
        }
        getNodeInfoAndTopicByOK(nodeName)

    }

    private fun getNodeInfoAndTopicByOK(nodeName: String) {
        val requestURL = NetManager.HTTPS_V2EX_BASE + "/go/" + nodeName
        XLog.tag(TAG).d("url:" + requestURL)
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders).url(requestURL).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@NodeActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {

                val code = response.code()
                if (code == 302) {
                    handler.sendEmptyMessage(MSG_ERROR_AUTH)
                    return
                } else if (code != 200) {
                    NetManager.dealError(this@NodeActivity, code)
                    return
                }
                val body = response.body()?.string()
                val html = Jsoup.parse(body)
                mNodeModel = NetManager.parseToNode(html)
                Message.obtain(handler, MSG_GET_NODE_INFO).sendToTarget()

                mTopicModels.clear()
                mTopicModels.addAll(NetManager.parseTopicLists(html, NetManager.Source.FROM_NODE))
                handler.sendEmptyMessage(MSG_GET_TOPICS)
            }
        })
    }

    companion object {
        private val TAG = NodeActivity::class.java.simpleName
        private val MSG_GET_TOPICS = 1
        private val MSG_GET_NODE_INFO = 0
        private val MSG_ERROR_AUTH = 2
    }

}
