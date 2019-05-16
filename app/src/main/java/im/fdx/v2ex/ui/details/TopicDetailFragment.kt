package im.fdx.v2ex.ui.details

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.network.*
import im.fdx.v2ex.ui.BaseFragment
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.toast
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details_content.*
import kotlinx.android.synthetic.main.footer_reply.*
import okhttp3.*
import org.jetbrains.anko.share
import java.io.IOException

/**
 * Create by fandongxiao on 2019/5/16
 */
class TopicDetailFragment : BaseFragment() {

    private lateinit var mAdapter: TopicDetailAdapter
    private var mMenu: Menu? = null

    private lateinit var mTopicId: String

    /**
     * 用于后续操作的token，类似登录信息，每次刷新页面都会更新。
     */
    private var token: String? = null
    private var once: String = ""

    private var topicHeader: Topic? = null
    private var isFavored: Boolean = false
    private var isThanked: Boolean = false


    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            XLog.tag("TopicActivity").d("get in broadcast: " + intent.action)
            if (intent.action == Keys.ACTION_LOGIN) {
                activity?.invalidateOptionsMenu()
                setFootView(true)
            } else if (intent.action == Keys.ACTION_LOGOUT) {
                activity?.invalidateOptionsMenu()
                setFootView(false)
            } else if (intent.action == Keys.ACTION_GET_MORE_REPLY) {
                logd("MSG_GET  LocalBroadCast")
                if (intent.getStringExtra(Keys.KEY_TOPIC_ID) == mTopicId) {
                    token = intent.getStringExtra("token")
                    val rm = intent.getParcelableArrayListExtra<Reply>("replies")
                    mAdapter.addItems(rm)
                    if (intent.getBooleanExtra("bottom", false)) {
                        detail_recycler_view.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_details_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filter = IntentFilter(Keys.ACTION_LOGIN)
        filter.addAction(Keys.ACTION_LOGOUT)
        filter.addAction(Keys.ACTION_GET_MORE_REPLY)
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(receiver, filter)
        setFootView(MyApp.get().isLogin)


        toolbar.run {
            inflateMenu(R.menu.menu_details)
            setNavigationIcon(R.drawable.ic_arrow_back_primary_24dp);
            setNavigationOnClickListener {
                activity?.finish()
            }

            setOnMenuItemClickListener {
                when (it.itemId) {

                    R.id.menu_favor -> token?.let { favorOrNot(mTopicId, it, isFavored) }
                    R.id.menu_thank_topic -> {
                        token?.let { thankTopic(mTopicId, it, isThanked) }
                    }
                    R.id.menu_item_share -> activity?.share("来自V2EX的帖子：${(mAdapter.topics[0]).title} \n" +
                            " ${NetManager.HTTPS_V2EX_BASE}/t/${mAdapter.topics[0].id}")
                    R.id.menu_item_open_in_browser -> {
                        val topicId = mAdapter.topics[0].id
                        val url = NetManager.HTTPS_V2EX_BASE + "/t/" + topicId
                        val uri = Uri.parse(url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.`package` = "com.android.chrome"
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            intent.`package` = null
                            startActivity(intent)
                        }
                    }
                }
                true
            }
        }


        mMenu = toolbar.menu
        if (MyApp.get().isLogin) {
            mMenu?.findItem(R.id.menu_favor)?.isVisible = true
            mMenu?.findItem(R.id.menu_thank_topic)?.isVisible = true
        } else {
            mMenu?.findItem(R.id.menu_favor)?.isVisible = false
            mMenu?.findItem(R.id.menu_thank_topic)?.isVisible = false
        }
        //// 这个Scroll 到顶部的bug，是focus的原因，focus会让系统自动滚动
        val mLayoutManager = LinearLayoutManager(activity)
        detail_recycler_view.layoutManager = mLayoutManager
        detail_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var currentPosition = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    if (currentPosition != 0) {
                        startAlphaAnimation(tv_toolbar, 500, false)
                        currentPosition = 0
                    }
                } else {
                    if (currentPosition == 0 && topicHeader != null) {
                        tv_toolbar?.text = topicHeader?.title
                        startAlphaAnimation(tv_toolbar, 500, true)
                        currentPosition = -1
                    }
                }
            }
        })

        mAdapter = TopicDetailAdapter(activity!!) { position: Int ->
            detail_recycler_view.smoothScrollToPosition(position)
        }
        detail_recycler_view.adapter = mAdapter
        swipe_details?.initTheme()
        swipe_details?.setOnRefreshListener { getRepliesPageOne(false) }

        et_post_reply.setOnFocusChangeListener { v, hasFocus ->

            if (!hasFocus) {
                val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        iv_send.setOnClickListener {
            postReply()
        }

        et_post_reply.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) = if (s.isEmpty()) {
                iv_send.isClickable = false
                iv_send.imageTintList = null
            } else {
                iv_send.isClickable = true
                iv_send.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.primary))
            }
        })
        val models: Topic? = arguments?.get(Keys.KEY_TOPIC_MODEL) as Topic?
        models?.let {
            mAdapter.topics[0] = it
            mAdapter.notifyDataSetChanged()
        }

        mTopicId = (arguments?.get(Keys.KEY_TOPIC_ID) as String?) ?: ""

        getRepliesPageOne(false)
    }

    // 设置渐变的动画
    fun startAlphaAnimation(v: View?, duration: Int, show: Boolean) {
        val anim = when {
            show -> AnimationUtils.loadAnimation(activity, R.anim.show_toolbar)
            else -> AnimationUtils.loadAnimation(activity, R.anim.hide_toolbar)
        }
        anim.duration = duration.toLong()
        anim.fillAfter = true
        v?.startAnimation(anim)
    }


    private fun setFootView(isVisible: Boolean) {
        activity?.findViewById<View>(R.id.foot_container)?.isVisible = isVisible
    }


    private fun getRepliesPageOne(scrollToBottom: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId?p=1").start(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    swipe_details?.isRefreshing = false
                    toast("无法打开该主题")

                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val code = response.code()
                if (code == 302) {
                    //权限问题，需要登录
                    activity?.runOnUiThread {
                        toast("需要登录后查看该主题")
                        activity?.finish()
                    }
                    return
                }
                if (code != 200) {
                    activity?.runOnUiThread {
                        swipe_details?.isRefreshing = false
                        toast("无法打开该主题")

                    }
                    return
                }

                val bodyStr = response.body()!!.string()

                val parser = Parser(bodyStr)
                topicHeader = parser.parseResponseToTopic(mTopicId)
                val repliesFirstPage = parser.getReplies()

                if (myApp.isLogin) {
                    token = parser.getVerifyCode()

                    if (token == null) {
                        myApp.setLogin(false)
                        activity?.runOnUiThread {
                            toast("需要登录后查看该主题")
                            activity?.finish()
                        }
                        return
                    }
                    logd("verifyCode is :" + token!!)
                    mAdapter.verifyCode = token!!
                    isFavored = parser.isTopicFavored()
                    isThanked = parser.isTopicThanked()
                    once = parser.getOnceNum()

                    logd("is favored: " + isFavored.toString())
                    activity?.runOnUiThread {
                        if (isFavored) {
                            mMenu?.findItem(R.id.menu_favor)?.setIcon(R.drawable.ic_favorite_white_24dp)
                            mMenu?.findItem(R.id.menu_favor)?.setTitle(R.string.unFavor)
                        } else {
                            mMenu?.findItem(R.id.menu_favor)?.setIcon(R.drawable.ic_favorite_border_white_24dp)
                            mMenu?.findItem(R.id.menu_favor)?.setTitle(R.string.favor)
                        }


                        if (isThanked) {
                            mMenu?.findItem(R.id.menu_thank_topic)?.setTitle(R.string.already_thank)
                        } else {
                            mMenu?.findItem(R.id.menu_thank_topic)?.setTitle(R.string.thanks)
                        }


                    }

                }

                logd("got page 1 , next is more page")

                val totalPage = parser.getPageValue()[1]  // [2,3]
                activity?.runOnUiThread {
                    swipe_details?.isRefreshing = false
                    mAdapter.updateItems(topicHeader!!, repliesFirstPage)
                    if (totalPage == 1 && scrollToBottom) {
                        detail_recycler_view.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }

                if (totalPage > 1) {
                    XLog.tag("TopicActivity").d(totalPage.toString())
                    getMoreRepliesByOrder(totalPage, scrollToBottom)
                }
            }
        })
    }

    private fun getMoreRepliesByOrder(totalPage: Int, scrollToBottom: Boolean) {
        val intentGetMoreReply = Intent(activity, MoreReplyService::class.java)
        intentGetMoreReply.action = "im.fdx.v2ex.get.other.more"
        intentGetMoreReply.putExtra("page", totalPage)
        intentGetMoreReply.putExtra("topic_id", mTopicId)
        intentGetMoreReply.putExtra("bottom", scrollToBottom)
        activity?.startService(intentGetMoreReply)
        logd("yes I startIntentService")
    }


    @Suppress("unused")
    private fun getNextReplies(totalPage: Int, currentPage: Int) {
        val intentGetMoreReply = Intent(activity, MoreReplyService::class.java)
        intentGetMoreReply.run {
            action = "im.fdx.v2ex.get.one.more"
            putExtra("page", totalPage)
            putExtra("topic_id", mTopicId)
            putExtra("currentPage", currentPage)
        }
        activity?.startService(intentGetMoreReply)
    }

    private fun favorOrNot(topicId: String, token: String, doFavor: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/${if (doFavor) "un" else ""}favorite/topic/$topicId?t=$token")
                .start(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(activity, swipe = swipe_details)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 302) {
                            activity?.runOnUiThread {
                                toast("${if (doFavor) "取消" else ""}收藏成功")
                                swipe_details?.isRefreshing = true
                                getRepliesPageOne(false)
                            }
                        }
                    }
                })
    }

    //    https@ //www.v2ex.com/thank/topic/529363?t=emdnsipiydbckrgywnjvwjcgkhandjud
    private fun thankTopic(topicId: String, token: String, isThanked: Boolean) {
        if (isThanked) return
        val body = FormBody.Builder().add("t", token).build()
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url("https://www.v2ex.com/thank/topic/$topicId")
                .post(body)
                .build())
                .start(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(activity)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 200) {
                            activity?.runOnUiThread {
                                toast("感谢成功")
                                mMenu?.findItem(R.id.menu_thank_topic)?.setTitle(R.string.already_thank)
                            }
                        } else {
                            NetManager.dealError(activity, response.code())
                        }
                    }
                })
    }


    private fun postReply() {
        et_post_reply.clearFocus()
        logd("I clicked")
        val content = et_post_reply.text.toString()
        val requestBody = FormBody.Builder()
                .add("content", content)
                .add("once", once)
                .build()

        pb_send.visibility = View.VISIBLE
        iv_send.visibility = View.INVISIBLE

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .header("Origin", NetManager.HTTPS_V2EX_BASE)
                .header("Referer", NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .post(requestBody)
                .build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    pb_send.visibility = View.GONE
                    iv_send.visibility = View.VISIBLE
                }
                NetManager.dealError(activity, swipe = swipe_details)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                activity?.runOnUiThread {
                    pb_send.visibility = View.GONE
                    iv_send.visibility = View.VISIBLE
                    if (response.code() == 302) {
                        logd("成功发布")
                        toast("发表评论成功")
                        et_post_reply.setText("")
                        swipe_details?.isRefreshing = true
                        getRepliesPageOne(true)
                    } else {
                        toast("发表评论失败")
                        swipe_details?.isRefreshing = true
                        getRepliesPageOne(true)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.tag("TopicActivity").d("onDestroy")
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(receiver)
    }
}
