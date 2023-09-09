package im.fdx.v2ex.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.API_HEATED
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.URL_FOLLOWING
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.Parser.Source.*
import im.fdx.v2ex.ui.NODE_TYPE
import im.fdx.v2ex.ui.isUsePageNum
import im.fdx.v2ex.ui.main.model.SearchResult
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.EndlessOnScrollListener
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.ViewUtil
import im.fdx.v2ex.utils.extensions.*
import im.fdx.v2ex.view.PageNumberView
import okhttp3.*
import im.fdx.v2ex.utils.extensions.toast
import java.io.IOException
import java.util.*

/**
 * 主题列表页，核心页面。
 * 存在问题:  重构复杂，代码复杂。 代码不优雅，扩展性不够
 */
class TopicsFragment : Fragment() {

    private lateinit var mAdapter: TopicsRVAdapter
    private lateinit var mSwipeLayout: SwipeRefreshLayout
    private var mRecyclerView: RecyclerView? = null
    private var fab: FloatingActionButton? = null //有可能为空
    private lateinit var flContainer: FrameLayout
    private var pageNumberView: PageNumberView? = null
    var mRequestURL: String = ""
    private lateinit var mScrollListener: EndlessOnScrollListener
    var currentMode = FROM_HOME
    var totalPage = 0
    var isEndlessMode = true // 模式无限滚动模式

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            logd("getAction: $action")
            val itemId = intent.getStringExtra(Keys.KEY_TOPIC_ID)
            when (action) {
                Keys.ACTION_HIDE_TOPIC -> {

                    itemId?.let { mAdapter.removeItem(it) }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        LocalBroadcastManager.getInstance(myApp).registerReceiver(receiver, IntentFilter(Keys.ACTION_HIDE_TOPIC))
    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(myApp).registerReceiver(receiver, IntentFilter(Keys.ACTION_HIDE_TOPIC))
    }


    fun togglePageNum(on: Boolean) {
        isEndlessMode = !on
        if (isEndlessMode) {
            mScrollListener?.let { mRecyclerView?.addOnScrollListener(it) }
        } else {
            mScrollListener?.let { mRecyclerView?.removeOnScrollListener(it) }
        }
        pageNumberView?.let {
            it.globalVisible = on
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_tab_article, container, false)
        mSwipeLayout = layout.findViewById(R.id.swipe_container)
        mSwipeLayout.initTheme()
        mSwipeLayout.setOnRefreshListener { refresh() }
        pageNumberView = layout.findViewById(R.id.pageNumberView)
        mRecyclerView = layout.findViewById(R.id.rv_container)
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView?.layoutManager = layoutManager

        mScrollListener = object : EndlessOnScrollListener(mRecyclerView!!, layoutManager) {
            override fun onCompleted() {
                activity?.toast(getString(R.string.no_more_data))
            }

            override fun onLoadMore(currentPage: Int) {
                mScrollListener.loading = true
                mSwipeLayout.isRefreshing = true
                loadMoreTopic(currentPage)
            }
        }


        mAdapter = TopicsRVAdapter(this)
        mRecyclerView?.adapter = mAdapter

        flContainer = layout.findViewById(R.id.fl_container)


        val args: Bundle? = arguments
        when {
            args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 1 -> {
                currentMode = FROM_FAVOR
                mRequestURL = "$HTTPS_V2EX_BASE/my/topics"
            }
            args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 2 -> {
                currentMode = FROM_FAVOR
                mRequestURL = URL_FOLLOWING
            }
            args?.getString(Keys.KEY_TAB) == "recent" -> mRequestURL = "$HTTPS_V2EX_BASE/recent"
            args?.getString(Keys.KEY_TAB) == "heated" -> mRequestURL = API_HEATED
            args?.getString(Keys.KEY_TAB) != null -> {
                if (args.getInt(Keys.KEY_TYPE) == NODE_TYPE) {
                    currentMode = FROM_NODE
                    mRequestURL = "$HTTPS_V2EX_BASE/go/${args.getString(Keys.KEY_TAB)}"
                } else {
                    mRequestURL = "$HTTPS_V2EX_BASE/?tab=${args.getString(Keys.KEY_TAB)}"
                }
            }
            args?.getString(Keys.KEY_USERNAME) != null -> {
                currentMode = FROM_MEMBER
                mRequestURL = "$HTTPS_V2EX_BASE/member/${args.getString(Keys.KEY_USERNAME)}/topics"
            }
            args?.getString(Keys.KEY_NODE_NAME) != null -> {
                currentMode = FROM_NODE
                mRequestURL = "$HTTPS_V2EX_BASE/go/${args.getString(Keys.KEY_NODE_NAME)}"
            }
            args?.getBoolean("search", false) == true -> {
                currentMode = FROM_SEARCH
            }
        }

        when (currentMode) {
            FROM_NODE, FROM_SEARCH, FROM_FAVOR, FROM_MEMBER -> mRecyclerView?.addOnScrollListener(mScrollListener)
            FROM_HOME -> {}
        }
        when(currentMode) {
            FROM_MEMBER, FROM_FAVOR -> {
                togglePageNum(isUsePageNum)
            }
            else -> {
                togglePageNum(false)
            }
        }

        val topicList: ArrayList<Topic>? = args?.getParcelableArrayList(Keys.KEY_TOPIC_LIST)
        if (currentMode == FROM_SEARCH) {
            flContainer.showNoContent(getString(R.string.please_input_key_to_search))
        } else if (currentMode == FROM_NODE && topicList != null) {
            // 已有数据
            val totalPage = args.getInt(Keys.KEY_PAGE_NUM, 1) ?: 1
            logd(topicList)
            mScrollListener.totalPage = totalPage
            setUIData(topicList)
        } else {
            flContainer.hideNoContent()
            mSwipeLayout.isRefreshing = true
            getTopics(mRequestURL)
        }

        pageNumberView?.setSelectNumListener {
            mSwipeLayout.isRefreshing = true
            getTopics(mRequestURL, it)
        }

        return layout
    }

    // 禁用隐藏fab
    @Deprecated("花里胡哨")
    private fun setUpFabAnimation() {
        fab?.let { fab ->
            mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                var isFabShowing = true

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) =
                    if (dy > 0) hideFab() else showFab()

                private fun hideFab() {
                    if (isFabShowing) {
                        isFabShowing = false
                        val translation = fab.y.minus(ViewUtil.screenHeight)
                        fab.animate().translationYBy(-translation).setDuration(500)?.start()
                    }
                }

                private fun showFab() {
                    if (!isFabShowing) {
                        isFabShowing = true
                        fab.animate().translationY(0f).setDuration(500).start()
                    }
                }
            })
        }
    }

    fun startQuery(q: SearchOption) {
        query = q
        makeQuery(query)
    }

    var query: SearchOption = SearchOption("")

    fun showRefresh(show: Boolean) {
        activity?.runOnUiThread {
            mSwipeLayout.isRefreshing = show
            mScrollListener.loading = show
        }

    }

    private fun loadMoreTopic(currentPage: Int) {

        if (currentMode == FROM_SEARCH) {
            makeQuery(query, currentPage)
        } else {
            getTopics(mRequestURL, currentPage)
        }
    }

    private fun refresh() {
        mScrollListener.restart()
        loadMoreTopic(1)
    }

//  fun updateAvatar(avatar: String) {
//      mAdapter.getList().forEach{
//        it.member?.avatar_normal =  avatar
//      }
//      mAdapter.notifyDataSetChanged()
//  }

    private fun getTopics(requestURL: String, currentPage: Int = 1) {

        if (requestURL == API_HEATED) {
            vCall(API_HEATED)
                .start(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        showRefresh(false)
                        activity?.runOnUiThread {
                            activity?.toast("获取热议主题失败，请重试")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        showRefresh(false)

                        val str = response.body!!.string()
                        val type = object : TypeToken<List<Topic>>() {}.type
                        val topicList = Gson().fromJson<List<Topic>>(str, type)
                        topicList.forEach {
                            logi(it.id + ":" + it.title)
                        }

                        activity?.runOnUiThread {
                            if (topicList.isEmpty()) {
                                flContainer.showNoContent()
                                mAdapter.clearAndNotify()
                            } else {
                                mAdapter.updateItems(topicList)
                            }
                        }
                    }
                })

            return
        }


        val url = if (currentMode == FROM_HOME) requestURL else "$requestURL?p=$currentPage"
        vCall(url, if(currentMode == FROM_FAVOR) "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:90.0) Gecko/20100101 Firefox/90.0" else null)
            .start(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showRefresh(false)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if (response.code == 302) {
                        showRefresh(false)
                        if (Objects.equals("/2fa", response.header("Location"))) {
                        }
                    } else if (response.code != 200) {
                        showRefresh(false)
                        dealError(activity, response.code)
                        return
                    }

                    val str = response.body?.string()!!
                    val parser = Parser(str)
                    if (totalPage == 0) {
                        totalPage = parser.getTotalPageForTopics()
                        mScrollListener.totalPage = totalPage
                        if (currentMode == FROM_MEMBER) {
                            val msg = parser.getContentMsg()
                            if (msg.contains("主题列表被隐藏")) {
                                activity?.runOnUiThread {
                                    showRefresh(false)
                                    pageNumberView?.totalNum = 0
                                    flContainer.showNoContent(msg)
                                }
                                return
                            }
                        }

                        activity?.runOnUiThread {
                            if (currentMode == FROM_MEMBER) {
                                val total = parser.getTotalTopics()
                                (activity as MemberActivity?)?.changeTitle(0, total.toString())
                            }
                        }
                    }
                    val topicList = parser.parseTopicLists(currentMode)
                    logd(topicList)
                    setUIData(topicList)
                }
            })
    }

    private fun setUIData(topicList: List<Topic>) {
        activity?.runOnUiThread {
            mSwipeLayout.isRefreshing = false
            mScrollListener.loading = false
            if (topicList.isEmpty()) {
                flContainer.showNoContent()
                mAdapter.clearAndNotify()
            } else {
                flContainer.hideNoContent()
                when (currentMode) {
                    FROM_MEMBER -> {
                        pageNumberView?.totalNum = totalPage
                        topicList.forEach {
                            it.member?.avatar_normal = arguments?.getString(Keys.KEY_AVATAR) ?: ""
                            logi(it.id + ":" + it.title + " -- " + it.member?.avatar_normal)
                        }
                        if (isEndlessMode) {
                            if (mScrollListener.isRestart()) {
                                topicList.let { mAdapter.updateItems(it) }
                            } else {
                                mScrollListener.success()
                                topicList.let { mAdapter.addAllItems(it) }
                            }
                        } else {
                            topicList.let { mAdapter.updateAllItemsWithoutDiff(it) }
                        }
                    }

                    FROM_NODE -> {
                        if (mScrollListener.isRestart()) {
                            topicList.let { mAdapter.updateItems(it) }
                        } else {
                            mScrollListener.success()
                            topicList.let { mAdapter.addAllItems(it) }
                        }
                    }
                    FROM_FAVOR -> {
                        pageNumberView?.totalNum = totalPage
                        if (isEndlessMode) {
                            if (mScrollListener.isRestart()) {
                                topicList.let { mAdapter.updateItems(it) }
                            } else {
                                mScrollListener.success()
                                topicList.let { mAdapter.addAllItems(it) }
                            }
                        } else {
                            topicList.let { mAdapter.updateAllItemsWithoutDiff(it) }
                        }
                    }
                    FROM_HOME -> {
                        topicList.let { mAdapter.updateAllItemsWithoutDiff(it) }
                    }
                    FROM_SEARCH -> {
                    }
                }
            }
        }
    }

    fun scrollToTop() = mRecyclerView?.smoothScrollToPosition(0)

    /**
     * nextIndex, not page index, is the item offset
     */
    private fun makeQuery(option: SearchOption, currentPage: Int = 1) {


        val nextIndex = (currentPage - 1) * NUMBER_PER_PAGE

        val url: HttpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(NetManager.API_SEARCH_HOST)
            .addEncodedPathSegments("api/search")

            .addEncodedQueryParameter("q", option.q)
            .addEncodedQueryParameter("from", nextIndex.toString()) // 偏移量, 默认0
            .addEncodedQueryParameter("size", NUMBER_PER_PAGE.toString()) //数量，默认10
            .addEncodedQueryParameter("sort", option.sort)
            .addEncodedQueryParameter("order", "0")
            .addEncodedQueryParameter("gte", option.gte)
            .addEncodedQueryParameter("lte", option.lte)
            .addEncodedQueryParameter("node", option.node)
            .addEncodedQueryParameter("opterator", "or")
            .addEncodedQueryParameter("username", option.username)
            .build()

        showRefresh(true)
        HttpHelper.OK_CLIENT
            .newCall(
                Request.Builder()
                    .addHeader("accept", "application/json")
                    .url(url)
                    .build()
            )
            .start(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    showRefresh(false)
                    dealError(context)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    loge(body)
                    val result: SearchResult = Gson().fromJson(body, SearchResult::class.java)
                    if (nextIndex == 0) {
                        result.total?.let {
                            mScrollListener.totalPage = (it / NUMBER_PER_PAGE + 1)
                        }
                    }

                    val topics = result.hits?.map {
                        val topic = Topic()
                        topic.id = it.id.toString()
                        topic.title = it.source?.title.toString()
                        topic.content = it.source?.content
                        topic.created = TimeUtil.toUtcTime(it.source?.created.toString())
                        topic.member = Member().apply { username = it.source?.member.toString() }
                        topic.replies = it.source?.replies
//                        topic.node //  todo需要去做一个node id -> node obj的遍历查找，
                        topic
                    }

                    activity?.runOnUiThread {
                        showRefresh(false)
                        if (topics == null) {
                            flContainer.showNoContent("未查询到内容，请重新查询")
                            return@runOnUiThread
                        }
                        if (topics.isEmpty()) {
                            flContainer.showNoContent("未查询到内容，请重新查询")
                            mAdapter.clearAndNotify()
                        } else {
                            flContainer.hideNoContent()
                            if (nextIndex == 0) {
                                topics.let { mAdapter.updateItems(it) }
                            } else {
                                mScrollListener.success()
                                topics.let { mAdapter.addAllItems(it) }
                            }
                        }
                    }

                }
            })
    }

    companion object {
        const val NUMBER_PER_PAGE = 10
    }
}

//0 sumup,1  created

const val SUMUP = "sumup"
const val CREATED = "created"


data class SearchOption(val q:String, val sort: String = SUMUP, val gte : String? = null,val lte: String? = null, val node : String? = null, val username:String? =null)
