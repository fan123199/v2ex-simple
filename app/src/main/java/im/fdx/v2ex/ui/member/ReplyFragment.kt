package im.fdx.v2ex.ui.member

import android.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.utils.EndlessRecyclerOnScrollListener
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 */
class ReplyFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: ReplyAdapter //
    private lateinit var flcontainer: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_reply, container, false)

    private var mScrollListener: EndlessRecyclerOnScrollListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        flcontainer = view.findViewById(R.id.fl_container)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        swipeRefreshLayout.initTheme()
        swipeRefreshLayout.setOnRefreshListener {
            mScrollListener?.current_page = 1
            getRepliesByWeb(1)/* 刷新则重头开始 */
        }

        val rvReply: RecyclerView = view.findViewById(R.id.rv_container)
        val layoutManager = LinearLayoutManager(activity)


        rvReply.layoutManager = layoutManager
        rvReply.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        // enable pull up for endless loading
        mScrollListener = object : EndlessRecyclerOnScrollListener(layoutManager, rvReply) {
            override fun onCompleted() {
                toast(getString(R.string.no_more_data))
            }

            override fun onLoadMore(current_page: Int) {
                XLog.e("currentPage: $current_page, totalPage: $totalPage")
                swipeRefreshLayout.isRefreshing = true
                mScrollListener?.loading = true
                getRepliesByWeb(current_page)

            }
        }

        rvReply.addOnScrollListener(mScrollListener)
        adapter = ReplyAdapter(activity)
        rvReply.adapter = adapter
        swipeRefreshLayout.isRefreshing = true
        getRepliesByWeb(1)
    }


    private fun getRepliesByWeb(page: Int) {

        val url = "${NetManager.HTTPS_V2EX_BASE}/member/${arguments.getString(Keys.KEY_USERNAME)}/replies?p=$page"
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity, -1, swipeRefreshLayout)
                mScrollListener?.loading = false
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body()!!.string()
                val replyModels = parseToRepliess(body)

                if (totalPage == 0) {
                    totalPage = getTotalPage(body)
                    mScrollListener?.totalPage = totalPage
                }
                runOnUiThread {
                    if ((replyModels == null || replyModels.isEmpty())) {
                        if (page == 1) {
                            flcontainer.showNoContent()
                        }
                    } else {
                        if (page == 1) {
                            adapter.firstLoadItems(replyModels)
                        } else {
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

    private fun getTotalPage(body: String): Int {

        val re = Regex("(?<=全部回复第\\s\\d\\s页 / 共 )\\d+")
        val find = re.find(body)
        return find?.value?.toInt() ?: 0


    }

    private fun parseToRepliess(body: String?): List<MemberReplyModel>? {
        val list = mutableListOf<MemberReplyModel>()
        val html = Jsoup.parse(body)

        val elements = html.getElementsByAttributeValue("id", "Main")?.first()?.getElementsByClass("box")?.first()
        if (elements != null) {

            for (e in elements.getElementsByClass("dock_area")) {
                val model = MemberReplyModel()
                val titleElement = e.getElementsByAttributeValueContaining("href", "/t/").first()
                val title = titleElement.text()
                // TODO: 2017/7/16 暂时不知道怎么Id有用吗？但是没有Id的model好像没有什么灵魂
                val fakeId = titleElement.attr("href").removePrefix("/t/")
                val create = e.getElementsByClass("fade").first().ownText()
                model.topic.title = title
                model.topic.id = fakeId.split("#")[0]
                model.create = TimeUtil.toUtcTime(create)
                val contentElement: Element? = e.nextElementSibling()
                val content = contentElement?.getElementsByClass("reply_content")?.first()
                model.content = content?.html()
                list.add(model)
            }
        }
        return list
    }
}

