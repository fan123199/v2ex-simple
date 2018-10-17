package im.fdx.v2ex.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.Parser.Source.*
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.utils.EndlessOnScrollListener
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.ViewUtil
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.loge
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*

/**
 * 主题列表页，核心页面。
 * 存在问题:  重构复杂，代码复杂。 代码不优雅，扩展性不够
 */
class TopicsFragment : Fragment() {

  private lateinit var mAdapter: TopicsRVAdapter
  private lateinit var mSwipeLayout: SwipeRefreshLayout
  private lateinit var mRecyclerView: RecyclerView
  private var fab: FloatingActionButton? = null //有可能为空
  private lateinit var flContainer: FrameLayout

  var mRequestURL: String = ""
  lateinit var mScrollListener: EndlessOnScrollListener
  var currentMode = Parser.Source.FROM_HOME
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


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val layout = inflater.inflate(R.layout.fragment_tab_article, container, false)
    val args: Bundle? = arguments
    when {
      args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 1 -> mRequestURL = "$HTTPS_V2EX_BASE/my/topics"
      args?.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 2 -> mRequestURL = "$HTTPS_V2EX_BASE/my/following"
      args?.getString(Keys.KEY_TAB) == "recent" -> mRequestURL = "$HTTPS_V2EX_BASE/recent"
      args?.getString(Keys.KEY_TAB) != null -> mRequestURL = "$HTTPS_V2EX_BASE/?tab=${args.getString(Keys.KEY_TAB)}"
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

    mSwipeLayout = layout.findViewById(R.id.swipe_container)
    mSwipeLayout.initTheme()
    mSwipeLayout.setOnRefreshListener { refresh() }

    //找出recyclerview,并赋予变量 //fdx最早的水平
    mRecyclerView = layout.findViewById(R.id.rv_container)
    val layoutManager = LinearLayoutManager(activity)
    mRecyclerView.layoutManager = layoutManager

    mScrollListener = object : EndlessOnScrollListener(mRecyclerView) {
      override fun onCompleted() {
        activity?.toast(getString(R.string.no_more_data))
      }

      override fun onLoadMore(current_page: Int) {
        mScrollListener.loading = true
        mSwipeLayout.isRefreshing = true
        refresh(current_page)
      }
    }
    when (currentMode) {
      FROM_MEMBER, FROM_NODE, FROM_SEARCH -> mRecyclerView.addOnScrollListener(mScrollListener)
      else -> {
      }
    }


    setUpFabAnimation()


    mAdapter = TopicsRVAdapter(activity!!)
    mRecyclerView.adapter = mAdapter //大工告成

    flContainer = layout.findViewById(R.id.fl_container)

    if (currentMode == FROM_SEARCH) {
      flContainer.showNoContent(true, "请输入关键字进行搜索")
    } else {
      flContainer.showNoContent(false)
      mSwipeLayout.isRefreshing = true
      getTopics(mRequestURL)
    }

    return layout
  }

  private fun setUpFabAnimation() {
    fab?.let { fab ->
      mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        var isFabShowing = true

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = if (dy > 0) hideFab() else showFab()

        private fun hideFab() {
          if (isFabShowing) {
            isFabShowing = false
            val translation = fab.y.minus(ViewUtil.screenSize[1])
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

  fun startQuery() {
    query = arguments?.getString("key") ?: ""
    makeQuery(query, 0)
  }

  var query: String = ""

  fun showRefresh(show: Boolean) {
    mSwipeLayout.isRefreshing = show
    mScrollListener.loading = show

  }

  fun refresh(current_page: Int = 0) {
    if (currentMode == FROM_SEARCH) {
      makeQuery(query, (current_page - 1) * NUMBER_PER_PAGE)
    } else {
      getTopics(mRequestURL, current_page)
    }
  }

  private fun getTopics(requestURL: String, currentPage: Int = 1) {

    vCall(if (currentPage != 1) "$requestURL?p=$currentPage" else requestURL)
        .start(object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            mScrollListener.loading = false
            handler.sendEmptyMessage(MSG_FAILED)
          }

          @Throws(IOException::class)
          override fun onResponse(call: Call, response: okhttp3.Response) {
            activity?.runOnUiThread {
              mSwipeLayout.isRefreshing = false
              mScrollListener.loading = false
            }
            if (response.code() == 302) {
              if (Objects.equals("/2fa", response.header("Location"))) {
              }
            } else if (response.code() != 200) {
              dealError(activity, response.code(), mSwipeLayout)
              return
            }

            val str = response.body()?.string()!!
            val parser = Parser(str)
            val topicList = parser.parseTopicLists(currentMode)

            if (totalPage == 0) {
              totalPage = parser.getTotalPageForTopics()
              mScrollListener.totalPage = totalPage
            }
            logd(topicList)

//                        DbHelper.db.topicDao().insertTopic(*topicList.toTypedArray())

            activity?.runOnUiThread {
              if (topicList.isEmpty()) {
                flContainer.showNoContent(true)
                mAdapter.clearAndNotify()
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
            }
          }
        })
  }

  fun scrollToTop() = mRecyclerView.smoothScrollToPosition(0)

  val NUMBER_PER_PAGE = 20


  /**
   * nextIndex, not page index, is the item offset
   */
  private fun makeQuery(query: String, nextIndex: Int = 0) {


    val url: HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host("www.sov2ex.com")
        .addEncodedPathSegments("/api/search")
        .addEncodedQueryParameter("q", query)
        .addEncodedQueryParameter("sort", "created")
        .addEncodedQueryParameter("from", nextIndex.toString()) // 偏移量
        .addEncodedQueryParameter("size", NUMBER_PER_PAGE.toString()) //数量，默认10
//          .addEncodedQueryParameter("node") //节点名称
        .build()

    showRefresh(true)
    HttpHelper.OK_CLIENT.newCall(Request.Builder()
        .addHeader("accept", "application/json")
        .url(url)
        .build())
        .start(object : Callback {
          override fun onFailure(call: Call?, e: IOException?) {
            showRefresh(false)
            dealError(context)
          }

          override fun onResponse(call: Call?, response: Response?) {
            showRefresh(false)
            val body = response?.body()!!.string()
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
              topic
            } ?: return

            activity?.runOnUiThread {
              if (topics.isEmpty()) {
                flContainer.showNoContent(true)
                mAdapter.clearAndNotify()
              } else {
                flContainer.showNoContent(false)
                if (mScrollListener.pageToLoad == 1) {
                  topics.let { mAdapter.updateItems(it) }
                } else {
                  mScrollListener.pageAfterLoaded = nextIndex
                  topics.let { mAdapter.addAllItems(it) }
                }
              }
            }

          }
        })
  }

  companion object {
    private const val MSG_FAILED = 3
    private const val MSG_GET_DATA_BY_OK = 1

    fun newInstance() = TopicsFragment()
  }

}
