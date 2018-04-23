package im.fdx.v2ex.ui.main

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import im.fdx.v2ex.R
import im.fdx.v2ex.database.DbHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.Source.*
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.utils.EndlessOnScrollListener
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.ViewUtil
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.logi
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*


class TopicsFragment : Fragment() {

    private lateinit var mAdapter: TopicsRVAdapter
    private lateinit var mSwipeLayout: SwipeRefreshLayout
    private var mRecyclerView: RecyclerView? = null
    private var fab: FloatingActionButton? = null //有可能为空
    private lateinit var flContainer: FrameLayout

    private lateinit var mRequestURL: String

    lateinit var smoothLayoutManager: LinearLayoutManager
    lateinit var mScrollListener: EndlessOnScrollListener
    var currentMode = NetManager.Source.FROM_HOME
    var totalPage = 0

    internal var handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_GET_DATA_BY_OK -> {
                mSwipeLayout.isRefreshing = false
                mAdapter.notifyDataSetChanged()
            }
            MSG_FAILED -> mSwipeLayout.isRefreshing = false
        }
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_tab_article, container, false)
        val args: Bundle? = arguments
        when {
            args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 1 -> mRequestURL = "$HTTPS_V2EX_BASE/my/topics"
            args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 2 -> mRequestURL = "$HTTPS_V2EX_BASE/my/following"
            args?.getString(Keys.KEY_TAB) == "recent" -> mRequestURL = "$HTTPS_V2EX_BASE/recent"
            args?.getString(Keys.KEY_USERNAME) != null -> {
                currentMode = FROM_MEMBER
                mRequestURL = "$HTTPS_V2EX_BASE/member/${args.getString(Keys.KEY_USERNAME)}/topics"
            }
            args?.getString(Keys.KEY_NODE_NAME) != null -> {
                currentMode = FROM_NODE
                mRequestURL = "$HTTPS_V2EX_BASE/go/${args.getString(Keys.KEY_NODE_NAME)}"
            }
            else -> mRequestURL = "$HTTPS_V2EX_BASE/?tab=${args?.getString(Keys.KEY_TAB)}"
        }

        mSwipeLayout = layout.findViewById(R.id.swipe_container)
        mSwipeLayout.initTheme()
        mSwipeLayout.setOnRefreshListener { getTopics(mRequestURL) }

        mSwipeLayout.isRefreshing = true



        //找出recyclerview,并赋予变量 //fdx最早的水平
        mRecyclerView = layout.findViewById(R.id.rv_container)
        smoothLayoutManager = LinearLayoutManager(activity)
        mRecyclerView?.layoutManager = smoothLayoutManager

        mScrollListener = object : EndlessOnScrollListener(smoothLayoutManager, mRecyclerView!!) {
            override fun onCompleted() {
                toast(getString(R.string.no_more_data))
            }

            override fun onLoadMore(current_page: Int) {
                mScrollListener.loading = true
                mSwipeLayout.isRefreshing = true
                getTopics(mRequestURL, current_page)
            }
        }
        when (currentMode) {
            FROM_MEMBER, FROM_NODE -> mRecyclerView?.addOnScrollListener(mScrollListener)
            else -> {
            }
        }


        if (fab != null)
            mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                internal var isFabShowing = true

                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int)
                        = if (dy > 0) hideFab() else showFab()

                private fun hideFab() {
                    if (isFabShowing) {
                        isFabShowing = false
                        val translation = fab?.y?.minus(ViewUtil.screenSize[1])
                        fab?.animate()?.translationYBy(-translation!!)?.setDuration(500)?.start()
                    }
                }

                private fun showFab() {
                    if (!isFabShowing) {
                        isFabShowing = true
                        fab?.animate()?.translationY(0f)?.setDuration(500)?.start()
                    }
                }

            })


        mAdapter = TopicsRVAdapter(activity)
        mAdapter.isNodeClickable = false
        mRecyclerView?.adapter = mAdapter //大工告成
        //大工告成

        flContainer = layout.findViewById(R.id.fl_container)
        getTopics(mRequestURL)
        return layout
    }

//    private val currentPage = 0

    private fun getTopics(requestURL: String, currentPage: Int = 1) {

        vCall(if (currentPage != 1) "$requestURL?p=$currentPage" else requestURL).start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mScrollListener.loading = false
                handler.sendEmptyMessage(MSG_FAILED)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                runOnUiThread {
                    mSwipeLayout.isRefreshing = false
                    mScrollListener.loading = false
                }
                if (response.code() == 302) {
                    if (Objects.equals("/2fa", response.header("Location"))) {
                        runOnUiThread {
                            NetManager.showTwoStepDialog(activity)

                        }
                    }
                } else if (response.code() != 200) {
                    dealError(activity, response.code(), mSwipeLayout)
                    return
                }

                val time = System.currentTimeMillis()
                val bodyStr = response.body()?.string()
                val html = Jsoup.parse(bodyStr)
                logi("time cost in parseTopicLists2:" + (System.currentTimeMillis() - time).toString())
                val topicList = try {
                    NetManager.parseTopicLists(html, currentMode)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
                logi("time cost in parseTopicLists3:" + (System.currentTimeMillis() - time).toString())

                if (totalPage == 0) {
                    totalPage = getTotalPage(bodyStr!!)
                    mScrollListener.totalPage = totalPage
                }
                logi("time cost in parseTopicLists4:" + (System.currentTimeMillis() - time).toString())
                logd(topicList)

                DbHelper.db.topicDao().insertUsers(*topicList.toTypedArray())

                runOnUiThread {
                    logi("time cost in parseTopicLists5:" + (System.currentTimeMillis() - time).toString())
                    if (topicList.isEmpty()) {
                        flContainer.showNoContent()
                        mAdapter.clear()
                    } else {
                        flContainer.showNoContent(false)
                        when (currentMode) {
                            FROM_MEMBER, FROM_NODE ->
                                if (mScrollListener.pageToLoad == 1) {
                                    topicList.let { mAdapter.updateItems(it) }
                                } else {
                                    mScrollListener.pageAfterLoaded = currentPage
                                    topicList.let { mAdapter.addAllItems(it) }
                                }
                            FROM_HOME -> topicList.let { mAdapter.updateItems(it) }
                        }
                    }
                    logi("time cost in parseTopicLists6:" + (System.currentTimeMillis() - time).toString())
                }
            }
        })
    }


    //        <input type="number" class="page_input" autocomplete="off" value="1" min="1" max="8"
    private fun getTotalPage(bodyStr: String) = Regex("(?<=max=\")\\d{1,8}").find(bodyStr)?.value?.toInt()
            ?: 0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_refresh) {
            mSwipeLayout.isRefreshing = true
            getTopics(mRequestURL)
        }
        return super.onOptionsItemSelected(item)
    }

    fun scrollToTop() = mRecyclerView?.smoothScrollToPosition(0)

    @Suppress("unused")
    companion object {

        private val TAG = TopicsFragment::class.java.simpleName
        private const val MSG_FAILED = 3
        private const val MSG_GET_DATA_BY_OK = 1
    }

}
