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
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import org.jetbrains.anko.runOnUiThread
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        flcontainer = view.findViewById(R.id.fl_container)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        swipeRefreshLayout.initTheme()
        swipeRefreshLayout.setOnRefreshListener { getTopicsByUsernameAPI() }

        val rvReply: RecyclerView = view.findViewById(R.id.rv_container)
        val layoutManager = LinearLayoutManager(activity)

        rvReply.layoutManager = layoutManager
        rvReply.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        adapter = ReplyAdapter(activity)
        rvReply.adapter = adapter

        swipeRefreshLayout.isRefreshing = true
        getTopicsByUsernameAPI()


    }


    private fun getTopicsByUsernameAPI() {

        val url = "${NetManager.HTTPS_V2EX_BASE}/member/${arguments.getString(Keys.KEY_USERNAME)}/replies"
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity, -1, swipeRefreshLayout)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {

                val body = response.body()!!.string()
//                val topicModels = NetManager.myGson.fromJson<MutableList<TopicModel>>(body, type)

                val replyModels = parseToRepliess(body)
                if (replyModels == null || replyModels.isEmpty()) {
                    runOnUiThread {
                        swipeRefreshLayout.isRefreshing = false
                        flcontainer.showNoContent()
                    }
                    return
                }
                runOnUiThread {
                    adapter.updateItems(replyModels)
                    swipeRefreshLayout.isRefreshing = false
                }
                XLog.tag("__REPLY").i(replyModels[0].topic.title)
            }
        })
    }

    private fun parseToRepliess(body: String?): List<MemberReplyModel>? {
        val list = mutableListOf<MemberReplyModel>()
        val html = Jsoup.parse(body)

        val elements = html.getElementsByAttributeValue("id", "Main").first().getElementsByClass("box")?.first()
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