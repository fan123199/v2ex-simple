package im.fdx.v2ex.ui.topic

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elvishew.xlog.XLog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.database.DbHelper
import im.fdx.v2ex.databinding.ActivityDetailsContentBinding
import im.fdx.v2ex.myApp
import im.fdx.v2ex.network.*
import im.fdx.v2ex.ui.BaseFragment
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.showLoginHint
import im.fdx.v2ex.utils.extensions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.jetbrains.anko.share
import java.io.IOException
import java.lang.Exception

/**
 * Create by fandongxiao on 2019/5/16
 */
class TopicFragment : BaseFragment() {

    private lateinit var mAdapter: TopicAdapter
    private var mMenu: Menu? = null

    private lateinit var mTopicId: String

    /**
     * 用于后续操作的token，类似登录信息，每次刷新页面都会更新。
     */
    private var token: String? = null
    private var once: String? = null
    private var topicHeader: Topic? = null
    private var isFavored: Boolean = false
    private var isThanked: Boolean = false
    private var isIgnored: Boolean = false

    private var temp :String = ""
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            XLog.tag("TopicActivity").d("get in broadcast: " + intent.action)
            if (intent.action == Keys.ACTION_LOGIN) {
                activity?.invalidateOptionsMenu()
                setFootView()
                binding.swipeDetails?.isRefreshing = true
                getRepliesPageOne(false)
            } else if (intent.action == Keys.ACTION_LOGOUT) {
                activity?.invalidateOptionsMenu()
                setFootView()
            } else if (intent.action == Keys.ACTION_GET_MORE_REPLY) {
                logd("MSG_GET  LocalBroadCast")
                if (intent.getStringExtra(Keys.KEY_TOPIC_ID) == mTopicId) {
                    token = intent.getStringExtra("token")
                    val rm: ArrayList<Reply>? = intent.getParcelableArrayListExtra("replies")
                    rm?.let { mAdapter.addItems(it) }
                    if (intent.getBooleanExtra("bottom", false)) {
                        binding.detailRecyclerView.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }
            }
        }

    }

    private var _binding: ActivityDetailsContentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityDetailsContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LocalBroadcastManager.getInstance(myApp)
                .registerReceiver(receiver, IntentFilter()
                        .apply {
                            addAction(Keys.ACTION_LOGIN)
                            addAction(Keys.ACTION_LOGOUT)
                            addAction(Keys.ACTION_GET_MORE_REPLY)
                        })
        setFootView()

        mTopicId = (arguments?.get(Keys.KEY_TOPIC_ID) as String?) ?: ""
        FirebaseCrashlytics.getInstance().setCustomKey("topic_id", mTopicId)


        binding.toolbar.run {
            inflateMenu(R.menu.menu_details)
            setNavigationIcon(R.drawable.ic_arrow_back_primary_24dp)
            setNavigationOnClickListener {
                activity?.finish()
            }

            setOnMenuItemClickListener { it ->
                when (it.itemId) {

                    R.id.menu_favor -> token?.let { token -> favorOrNot(mTopicId, token, isFavored) }
                    R.id.menu_ignore_topic -> {
                        once?.let {  ignoreTopicOrNot(mTopicId, it, isIgnored)  }
                    }
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
                            //ignore who has no chrome
                        }
                    }
                }
                true
            }
        }


        mMenu = binding.toolbar.menu
        if (MyApp.get().isLogin) {
            mMenu?.findItem(R.id.menu_favor)?.isVisible = true
            mMenu?.findItem(R.id.menu_thank_topic)?.isVisible = true
            mMenu?.findItem(R.id.menu_ignore_topic)?.isVisible = true
        } else {
            mMenu?.findItem(R.id.menu_favor)?.isVisible = false
            mMenu?.findItem(R.id.menu_thank_topic)?.isVisible = false
            mMenu?.findItem(R.id.menu_ignore_topic)?.isVisible = false
        }
        //// 这个Scroll 到顶部的bug，是focus的原因，focus会让系统自动滚动
        val mLayoutManager = LinearLayoutManager(activity)
        binding.detailRecyclerView.layoutManager = mLayoutManager
        binding.detailRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var currentPosition = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    if (currentPosition != 0) {
                        startAlphaAnimation(binding.tvToolbar, 500, false)
                        currentPosition = 0
                    }
                } else {
                    if (currentPosition == 0) {
                        binding.tvToolbar.text = topicHeader?.title
                        startAlphaAnimation(binding.tvToolbar, 500, true)
                        currentPosition = -1
                    }
                }
            }
        })

        mAdapter = TopicAdapter(requireActivity()) { position: Int ->
            binding.detailRecyclerView.smoothScrollToPosition(position)
        }
        binding.detailRecyclerView.adapter = mAdapter
        binding.swipeDetails.initTheme()
        binding.swipeDetails.setOnRefreshListener { getRepliesPageOne(false) }

        binding.etPostReply.setOnFocusChangeListener { v, hasFocus ->

            if (!hasFocus) {
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        binding.ivSend.setOnClickListener {
            postReply()
        }

        binding.etPostReply.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    binding.ivSend.isClickable = false
                    binding.ivSend.imageTintList = null
                } else {
                    binding.ivSend.isClickable = true
                    binding.ivSend.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.primary))
                }
                temp =  s.toString()
            }

        })
        val models: Topic? = arguments?.get(Keys.KEY_TOPIC_MODEL) as Topic?
        models?.let {
            it.created = 0L
            mAdapter.topics[0] = it
            mAdapter.notifyDataSetChanged()
        }

        uiScope.launch {
            val text = DbHelper.db.myReplyDao().getMyReplyById(mTopicId)?.content?:""
            binding.etPostReply.setText(text)
        }

        binding.swipeDetails.isRefreshing = true
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


    private fun setFootView() {
        activity?.findViewById<View>(R.id.foot_container)?.isVisible = myApp.isLogin
    }


    private fun getRepliesPageOne(scrollToBottom: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId?p=1").start(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    binding.swipeDetails?.isRefreshing = false
                    toast("无法打开该主题")

                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                if (code == 302) {
                    //权限问题，需要登录
                    activity?.runOnUiThread {
                        binding.swipeDetails?.isRefreshing = false
                    if(!myApp.isLogin) {
                        activity?.showLoginHint(binding.etPostReply)
                    } else {
                        if(this@TopicFragment.isVisible){
                            toast("你要查看的页面可能遭遇权限问题");
                        }
                    }
                    }
                    return
                }
                if (code != 200) {
                    activity?.runOnUiThread {
                        binding.swipeDetails?.isRefreshing = false
                        toast("无法打开该主题")
                    }
                    return
                }

                val bodyStr = response.body!!.string()

                var repliesFirstPage: List<Reply> = arrayListOf()
                val parser = Parser(bodyStr)
                try {
                    topicHeader = parser.parseResponseToTopic(mTopicId)
                    repliesFirstPage = parser.getReplies()
                } catch (e: Exception){
                    FirebaseCrashlytics.getInstance().recordException(e)
                    activity?.runOnUiThread {
                        toast("未知内容错误")
                    }
                }

                if (myApp.isLogin) {
                    token = parser.getVerifyCode()

                    if (token == null) {
                        activity?.runOnUiThread {
                            toast("登录状态过期，请重新登录")
                            activity?.finish()
                        }
                        return
                    }
                    logd("verifyCode is :" + token!!)
                    once = parser.getOnceNum()
                    mAdapter.once = once
                    isFavored = parser.isTopicFavored()
                    isThanked = parser.isTopicThanked()
                    isIgnored = parser.isIgnored()

                    logd("is favored: $isFavored")
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

                        if (isIgnored) {
                            mMenu?.findItem(R.id.menu_ignore_topic)?.setTitle(R.string.already_ignore)
                        } else {
                            mMenu?.findItem(R.id.menu_ignore_topic)?.setTitle(R.string.ignore)
                        }
                    }

                }

                logd("got page 1")

                val totalPage = parser.getPageValue()[1]  // [2,3]
                activity?.runOnUiThread {
                    binding.swipeDetails?.isRefreshing = false
                    topicHeader?.let {
                        mAdapter.updateItems(it, repliesFirstPage)
                        if (totalPage == 1 && scrollToBottom) {
                            binding.detailRecyclerView?.scrollToPosition(mAdapter.itemCount - 1)
                        }
                    }

                }

                if (totalPage > 1) {
                    logd("totalPage: $totalPage")
                    getMoreRepliesByOrder(totalPage, scrollToBottom)
                }
            }
        })
    }

    private fun getMoreRepliesByOrder(totalPage: Int, scrollToBottom: Boolean) {


        val compressionWork = OneTimeWorkRequestBuilder<GetMoreRepliesWorker>()
                .setInputData(workDataOf(
                        "page" to totalPage,
                        "topic_id" to mTopicId,
                        "bottom" to scrollToBottom
                ))
                .build()
        WorkManager.getInstance().enqueue(compressionWork)
    }

    private fun favorOrNot(topicId: String, token: String, doFavor: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/${if (doFavor) "un" else ""}favorite/topic/$topicId?t=$token")
                .start(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(activity, swipe = binding.swipeDetails)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code == 302) {
                            activity?.runOnUiThread {
                                toast("${if (doFavor) "取消" else ""}收藏成功")
                                binding.swipeDetails?.isRefreshing = true
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
                        if (response.code == 200) {
                            activity?.runOnUiThread {
                                toast("感谢成功")
                                mMenu?.findItem(R.id.menu_thank_topic)?.setTitle(R.string.already_thank)
                            }
                        } else {
                            NetManager.dealError(activity, response.code)
                        }
                    }
                })
    }


    ///unignore/topic/605954?once=10562
    ///ignore/topic/605954?once=10562
    private fun ignoreTopicOrNot(topicId: String, once: String, isIgnored: Boolean) {
        vCall("https://www.v2ex.com/${if (isIgnored) "un" else ""}ignore/topic/$topicId?once=$once")
                .start(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        NetManager.dealError(activity)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.code == 302) {
                            activity?.runOnUiThread {
                                toast("${if (isIgnored) "取消" else ""}忽略成功")
                            }
                        } else {
                            NetManager.dealError(activity, response.code)
                        }
                    }
                })

    }

    private fun postReply() {
        binding.etPostReply.clearFocus()
        logd("I clicked")
        if(once == null ) {
            toast("发布失败，请刷新后重试")
            return
        }
        val content = binding.etPostReply.text.toString()
        val requestBody = FormBody.Builder()
                .add("content", content)
                .add("once", once!!)
                .build()

        binding.pbSend.visibility = View.VISIBLE
        binding.ivSend.visibility = View.INVISIBLE

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .header("Origin", NetManager.HTTPS_V2EX_BASE)
                .header("Referer", NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .post(requestBody)
                .build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    binding.pbSend.visibility = View.GONE
                    binding.ivSend.visibility = View.VISIBLE
                }
                NetManager.dealError(activity, swipe = binding.swipeDetails)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    binding.pbSend.visibility = View.GONE
                    binding.ivSend.visibility = View.VISIBLE
                    if (response.code == 302) {
                        logd("成功发布")
                        toast("发表评论成功")
                        binding.etPostReply.setText("")
                        binding.swipeDetails?.isRefreshing = true
                        getRepliesPageOne(true)
                    } else {
                        toast("发表评论失败")
                        binding.swipeDetails?.isRefreshing = true
                        getRepliesPageOne(true)
                    }
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        LocalBroadcastManager.getInstance(myApp).unregisterReceiver(receiver)
        uiScope.launch {
            DbHelper.db.myReplyDao().insert(MyReply(mTopicId, temp))
        }
        logd("onDestroyView")

    }
}
