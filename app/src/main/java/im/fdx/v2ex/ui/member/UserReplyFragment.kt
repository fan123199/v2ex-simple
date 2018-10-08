package im.fdx.v2ex.ui.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.Parser
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.utils.EndlessOnScrollListener
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import org.jetbrains.anko.toast
import java.io.IOException

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 *
 * 用户页的回复信息， 非主体下的回复
 */
class UserReplyFragment : androidx.fragment.app.Fragment() {

    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var adapter: ReplyAdapter //
    private lateinit var flcontainer: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_tab_article, container, false)

    private var mScrollListener: EndlessOnScrollListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        flcontainer = view.findViewById(R.id.fl_container)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        swipeRefreshLayout.initTheme()
        swipeRefreshLayout.setOnRefreshListener {
            mScrollListener?.pageToLoad = 1
            getRepliesByWeb(1)/* 刷新则重头开始 */
        }

        val rvReply: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.rv_container)
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity!!)


        rvReply.layoutManager = layoutManager
        rvReply.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(activity, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))

        // enable pull up for endless loading
        mScrollListener = object : EndlessOnScrollListener(layoutManager, rvReply) {
            override fun onCompleted() {
                activity?.toast(getString(R.string.no_more_data))
            }

            override fun onLoadMore(current_page: Int) {
                XLog.e("currentPage: $current_page, totalPage: $totalPage")
                swipeRefreshLayout.isRefreshing = true
                mScrollListener?.loading = true
                getRepliesByWeb(current_page)
            }
        }

        rvReply.addOnScrollListener(mScrollListener!!)
        adapter = ReplyAdapter(activity!!)
        rvReply.adapter = adapter
        swipeRefreshLayout.isRefreshing = true
        getRepliesByWeb(1)
    }


    private fun getRepliesByWeb(page: Int) {

        val url = "${NetManager.HTTPS_V2EX_BASE}/member/${arguments?.getString(Keys.KEY_USERNAME)}/replies?p=$page"

        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity, -1, swipeRefreshLayout)
                mScrollListener?.loading = false
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body()!!.string()
                val parser = Parser(body)
                val replyModels = parser.getUserReplies()

                if (totalPage == 0) {
                    totalPage = parser.getTotalPage()
                    mScrollListener?.totalPage = totalPage
                }
                activity?.runOnUiThread {
                    if (replyModels.isEmpty()) {
                        if (page == 1) {
                            flcontainer.showNoContent()
                        }
                    } else {
                        if (page == 1) {
                            adapter.firstLoadItems(replyModels)
                        } else {
                            mScrollListener?.pageAfterLoaded = page
                            adapter.addItems(replyModels)
                        }
                        XLog.tag("__REPLY").i(replyModels[0].topic.title)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
                mScrollListener?.loading = false
            }
        })
    }

    private var totalPage = 0
}

