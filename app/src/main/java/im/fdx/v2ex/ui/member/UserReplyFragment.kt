package im.fdx.v2ex.ui.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.FragmentTabArticleBinding
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
    private var currentPage = 1
    private var totalPage = -1
    private var _binding: FragmentTabArticleBinding? = null
    private val binding get() = _binding!!
    private var isEndlessMode = true
    private var mScrollListener: EndlessOnScrollListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTabArticleBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun togglePageNum() {
        isEndlessMode = !isEndlessMode

        if (isEndlessMode) {
            mScrollListener?.let { binding.rvContainer.addOnScrollListener(it) }
        } else {
            mScrollListener?.let { binding.rvContainer.removeOnScrollListener(it) }
        }

        _binding?.let {
            binding.pageNumberView.isVisible = !isEndlessMode
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout = binding.swipeContainer
        swipeRefreshLayout.initTheme()

        swipeRefreshLayout.setOnRefreshListener {
            if (isEndlessMode) {
                mScrollListener?.restart()
                getRepliesByWeb(1)
            } else {
                getRepliesByWeb(currentPage)/* 刷新则重头开始 */
            }
        }

        val layoutManager = LinearLayoutManager(requireActivity())
        binding.rvContainer.layoutManager = layoutManager
        mScrollListener = object : EndlessOnScrollListener(binding.rvContainer, layoutManager) {
            override fun onCompleted() {
                activity?.toast(getString(R.string.no_more_data))
            }

            override fun onLoadMore(currentPage: Int) {
                XLog.e("currentPage: $currentPage, totalPage: $totalPage")
                swipeRefreshLayout.isRefreshing = true
                mScrollListener?.loading = true
                getRepliesByWeb(currentPage)
            }
        }

        if (isEndlessMode) {
            binding.rvContainer.addOnScrollListener(mScrollListener!!)
        }


        binding.rvContainer.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                activity,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )

        adapter = ReplyAdapter(requireActivity())
        binding.rvContainer.adapter = adapter

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
                mScrollListener?.loading = false
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body!!.string()
                val parser = Parser(body)
                val replyModels = parser.getUserReplies()

                if (totalPage == -1) {
                    totalPage = parser.getTotalPageInMember()
                    activity?.runOnUiThread {
                        mScrollListener?.totalPage = totalPage
                        binding.pageNumberView.totalNum = totalPage
                        if (totalPage > 0) {
                            (activity as MemberActivity?)?.showMoreBtn(1)
                        }
                    }
                }
                activity?.runOnUiThread {
                    if (replyModels.isEmpty()) {
                        if (page == 1) {
                            binding.flContainer.showNoContent()
                        }
                    } else {
                        if (isEndlessMode) {
                            if (page == 1) {
                                adapter.updateItem(replyModels)
                            } else {
                                mScrollListener?.success()
                                adapter.addItems(replyModels)
                            }
                            mScrollListener?.loading = false
                        } else {
                            adapter.updateItem(replyModels)
                            binding.rvContainer.scrollToPosition(0)
                        }
                        XLog.tag("__REPLY").i(replyModels[0].topic.title)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

}

