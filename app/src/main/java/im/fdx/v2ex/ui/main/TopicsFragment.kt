package im.fdx.v2ex.ui.main

import android.animation.ValueAnimator
import android.app.Fragment
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.showNoContent
import im.fdx.v2ex.view.SmoothLayoutManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*


class TopicsFragment : Fragment() {

    private val mTopicModels = ArrayList<TopicModel>()
    private var mAdapter: TopicsRVAdapter? = null
    private lateinit var mSwipeLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private var fab: FloatingActionButton? = null
    private lateinit var flContainer: FrameLayout

    private lateinit var mRequestURL: String

    internal var handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_GET_DATA_BY_OK -> {
                mAdapter!!.notifyDataSetChanged()
                mSwipeLayout.isRefreshing = false
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
        val args = arguments
        mRequestURL = when {
            args.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 1 -> "$HTTPS_V2EX_BASE/my/topics"
            args.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 2 -> "$HTTPS_V2EX_BASE/my/following"
            args.getString(Keys.KEY_TAB) == "recent" -> "$HTTPS_V2EX_BASE/recent"
            else -> "$HTTPS_V2EX_BASE/?tab=${args.getString(Keys.KEY_TAB)}"
        }
        mSwipeLayout = layout.findViewById(R.id.swipe_container)
        mSwipeLayout.setColorSchemeResources(R.color.accent_orange)
        mSwipeLayout.setOnRefreshListener { getTopics(mRequestURL) }

        mSwipeLayout.isRefreshing = true
        getTopics(mRequestURL)


        //找出recyclerview,并赋予变量 //fdx最早的水平
        mRecyclerView = layout.findViewById(R.id.rv_container)
        mRecyclerView.layoutManager = SmoothLayoutManager(activity)

        fab = activity.findViewById(R.id.fab_main)
        if (fab != null)
            mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                internal var isFabShowing = true

                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                    val animator = ValueAnimator.ofInt(100, 200, 300, 400)
                    animator.duration = 1000
                    val animation = TranslateAnimation(Animation.ABSOLUTE.toFloat(), Animation.ABSOLUTE.toFloat(), Animation.ABSOLUTE.toFloat(), 100f)
                    animation.repeatCount = 1
                    when {
                        dy > 0 -> hideFab()
                        else -> showFab()
                    }
                }

                private fun hideFab() {
                    if (isFabShowing) {
                        isFabShowing = false
                        val point = Point()
                        activity.window.windowManager.defaultDisplay.getSize(point)
                        val translation = fab?.y?.minus(point.y)
                        fab?.animate()?.translationYBy(-translation!!)?.setDuration(500)?.start()
                    }
                }

                private fun showFab() {
                    if (!isFabShowing) {
                        isFabShowing = true
                        fab?.animate()?.translationY(0f)?.setDuration(500)?.start()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })


        mAdapter = TopicsRVAdapter(activity, mTopicModels)
        mAdapter?.isNodeClickable = false
        mRecyclerView.adapter = mAdapter //大工告成

        flContainer = layout.findViewById(R.id.fl_container)
        return layout
    }


    private fun getTopics(requestURL: String) {

        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(requestURL)
                .get()
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                XLog.tag("TopicFragment").e("error OK")
                handler.sendEmptyMessage(MSG_FAILED)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {

                if (response.code() != 200) {
                    dealError(activity, response.code(), mSwipeLayout)
                    return
                }

                val html = Jsoup.parse(response.body()?.string())
                val topicList = NetManager.parseTopicLists(html, NetManager.Source.FROM_HOME)

                XLog.i("TOPICS" + topicList)
                if (topicList.isEmpty()) {
                    activity.runOnUiThread {
                        flContainer.showNoContent()
                        mSwipeLayout.isRefreshing = false
                    }
                    return
                }

                mTopicModels.clear()
                mTopicModels.addAll(topicList)
                handler.sendEmptyMessage(MSG_GET_DATA_BY_OK)
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_refresh) {
            mSwipeLayout.isRefreshing = true
            getTopics(mRequestURL)
        }
        return super.onOptionsItemSelected(item)
    }

    fun scrollToTop() = mRecyclerView.smoothScrollToPosition(0)

    companion object {

        private val TAG = TopicsFragment::class.java.simpleName
        private val MSG_FAILED = 3
        private val MSG_GET_DATA_BY_OK = 1
    }

}
