package im.fdx.v2ex.ui.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ActivityDetailsContentBinding
import im.fdx.v2ex.databinding.FragmentTabArticle2Binding
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
class UserReplyFragment2 : androidx.fragment.app.Fragment() {

    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private lateinit var adapter: ReplyAdapter //
    private var currentPage  = 1
    private var totalPage = -1
    private var _binding: FragmentTabArticle2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentTabArticle2Binding.inflate(inflater, container, false)
        return  binding.root
    }


    fun togglePageNum() {
        _binding?.let {
            binding.pageNumberView.isVisible = !binding.pageNumberView.isVisible
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        swipeRefreshLayout = binding.swipeContainer
        swipeRefreshLayout.initTheme()
        swipeRefreshLayout.setOnRefreshListener {
            getRepliesByWeb(currentPage)/* 刷新则重头开始 */
        }

        val rvReply: androidx.recyclerview.widget.RecyclerView = view.findViewById(R.id.rv_container)
        val layoutManager = LinearLayoutManager(requireActivity())

        rvReply.layoutManager = layoutManager
        rvReply.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(activity, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))

        adapter = ReplyAdapter(requireActivity())
        rvReply.adapter = adapter

        binding.pageNumberView.setSelectNumListener {
            swipeRefreshLayout.isRefreshing = true
            getRepliesByWeb(it)
        }


        swipeRefreshLayout.isRefreshing = true
        getRepliesByWeb(currentPage)
    }


    private fun getRepliesByWeb(page: Int) {

        val url = "${NetManager.HTTPS_V2EX_BASE}/member/${arguments?.getString(Keys.KEY_USERNAME)}/replies?p=$page"

        vCall(url).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity, -1)
                swipeRefreshLayout.isRefreshing = false
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body!!.string()
                val parser = Parser(body)
                val replyModels = parser.getUserReplies()

                if (totalPage == -1) {
                    totalPage = parser.getTotalPageInMember()
                    activity?.runOnUiThread {
                        binding.pageNumberView.totalNum = totalPage
                    }
                }
                activity?.runOnUiThread {
                    if (replyModels.isEmpty()) {
                        if (page == 1) {
                          binding.flContainer.showNoContent()
                        }
                    } else {
                        adapter.updateItem(replyModels)
                        binding.rvContainer.scrollToPosition(0)
                        XLog.tag("__REPLY").i(replyModels[0].topic.title)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

}

