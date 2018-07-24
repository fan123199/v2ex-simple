package im.fdx.v2ex.ui.details

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.synthetic.main.activity_details_content.*
import kotlinx.android.synthetic.main.footer_reply.*
import okhttp3.*
import org.jetbrains.anko.share
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.regex.Pattern

class DetailsActivity : BaseActivity() {

    private lateinit var mAdapter: DetailsAdapter
    //    private val mAllContent = mutableListOf<BaseModel>()
    private var mMenu: Menu? = null

    private lateinit var tvToolbar: TextView

    private lateinit var pb: ProgressBar
    private lateinit var mTopicId: String
    private var topicHeader: Topic? = null
    private var token: String? = null
    private var isFavored: Boolean = false
    private var once: String? = null
    private var currentPage: Int = 0

    private val callback = object : DetailsAdapter.AdapterCallback {
        override fun onMethodCallback(type: Int, position: Int) {
            when (type) {
                1 -> {
                }
                2 -> getMoreRepliesByOrder(totalPage = 1, scrollToBottom = false)
                -1 -> detail_recycler_view.smoothScrollToPosition(position)
            }
        }
    }


    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            XLog.tag("DetailsActivity").d("get in broadcast: " + intent.action)
            if (intent.action == Keys.ACTION_LOGIN) {
                invalidateOptionsMenu()
                setFootView(true)
            } else if (intent.action == Keys.ACTION_LOGOUT) {
                invalidateOptionsMenu()
                setFootView(false)
            } else if (intent.action == "im.fdx.v2ex.reply") {
                XLog.tag("HEHE").d("MSG_GET  LocalBroadCast")
                token = intent.getStringExtra("token")
                val rm = intent.getParcelableArrayListExtra<ReplyModel>("replies")
                mAdapter.addItems(rm)
                if (intent.getBooleanExtra("bottom", false)) {
                    detail_recycler_view.scrollToPosition(mAdapter.itemCount - 1)
                }
            }
        }

    }
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_OK_GET_TOPIC -> swipe_details.isRefreshing = false
            MSG_ERROR_AUTH -> {
                toast("需要登录后查看该主题")
                this@DetailsActivity.finish()
            }
            MSG_ERROR_IO -> {
                swipe_details.isRefreshing = false
                toast("无法打开该主题")
            }
        }
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val filter = IntentFilter(Keys.ACTION_LOGIN)
        filter.addAction(Keys.ACTION_LOGOUT)
        filter.addAction("im.fdx.v2ex.reply")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
        setFootView(MyApp.get().isLogin())

        setUpToolbar()
        tvToolbar = findViewById(R.id.tv_toolbar)

        pb = findViewById(R.id.pb_send)
        //// 这个Scroll 到顶部的bug，是focus的原因，focus会让系统自动滚动
        val mLayoutManager = LinearLayoutManager(this)
        detail_recycler_view.layoutManager = mLayoutManager
        detail_recycler_view.smoothScrollToPosition(POSITION_START)
        detail_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var currentPosition = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                    if (currentPosition != 0) {
                        startAlphaAnimation(tvToolbar, 500, false)
                        currentPosition = 0
                    }
                } else {
                    if (currentPosition == 0 && topicHeader != null) {
                        tvToolbar.text = topicHeader?.title
                        startAlphaAnimation(tvToolbar, 500, true)
                        currentPosition = -1
                    }
                }
            }
        })

        mAdapter = DetailsAdapter(this@DetailsActivity, callback)
        detail_recycler_view.adapter = mAdapter

        swipe_details.initTheme()
        swipe_details.setOnRefreshListener { getRepliesPageOne(mTopicId, false) }

        et_post_reply.setOnFocusChangeListener { v, hasFocus ->

            if (!hasFocus) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        iv_send.setOnClickListener {
            postReply()
        }

        et_post_reply.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) = if (s.isNullOrEmpty()) {
                iv_send.isClickable = false
                iv_send.imageTintList = null
            } else {
                iv_send.isClickable = true
                iv_send.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@DetailsActivity, R.color.primary))
            }
        })
        parseIntent(intent)

    }

    // 设置渐变的动画
    fun startAlphaAnimation(v: View, duration: Int, show: Boolean) {
        val anim = when {
            show -> AnimationUtils.loadAnimation(this, R.anim.show_toolbar)
            else -> AnimationUtils.loadAnimation(this, R.anim.hide_toolbar)
        }
        anim.duration = duration.toLong()
        anim.fillAfter = true
        v.startAnimation(anim)
    }


    private fun setFootView(beVisible: Boolean) {
        findViewById<View>(R.id.foot_container).visibility = if (beVisible) View.VISIBLE else View.GONE
    }

    private fun parseIntent(intent: Intent) {
        val data = intent.data
        mTopicId = when {
            data != null -> data.pathSegments[1]
            intent.getParcelableExtra<Parcelable>("model") != null -> {
                val topicModel = intent.getParcelableExtra<Topic>("model")
                mAdapter.mAllList.add(0, topicModel)
                mAdapter.notifyDataSetChanged()
                topicModel.id
            }
            intent.getStringExtra(Keys.KEY_TOPIC_ID) != null -> intent.getStringExtra(Keys.KEY_TOPIC_ID)
            else -> ""
        }

        swipe_details.isRefreshing = true
        getRepliesPageOne(mTopicId, false)
        XLog.tag("DetailsActivity").d("TopicUrl: ${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    private fun getRepliesPageOne(topicId: String, scrollToBottom: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/t/$topicId?p=1").start(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                handler.sendEmptyMessage(MSG_ERROR_IO)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val code = response.code()
                if (code == 302) {
                    //权限问题，需要登录
                    handler.sendEmptyMessage(MSG_ERROR_AUTH)
                    return
                }
                if (code != 200) {
                    handler.sendEmptyMessage(MSG_ERROR_IO)
                    return
                }

                val bodyStr = response.body()!!.string()
                val body = Jsoup.parse(bodyStr)

                topicHeader = NetManager.parseResponseToTopic(body, topicId)
                val repliesOne = NetManager.parseResponseToReplay(body)

                if (MyApp.get().isLogin()) {
                    token = NetManager.parseToVerifyCode(body)

                    if (token == null) {
                        MyApp.get().setLogin(false)
                        LocalBroadcastManager.getInstance(this@DetailsActivity).sendBroadcast(Intent(Keys.ACTION_LOGOUT))
                        handler.sendEmptyMessage(MSG_ERROR_AUTH)
                        return
                    }
                    XLog.tag("DetailsActivity").d("verifyCode is :" + token!!)
                    mAdapter.verifyCode = token!!
                    isFavored = parseIsFavored(body)

                    XLog.tag("DetailsActivity").d("is favored: " + isFavored.toString())
                    runOnUiThread {
                        if (isFavored) {
                            mMenu?.findItem(R.id.menu_favor)?.setIcon(R.drawable.ic_favorite_white_24dp)
                            mMenu?.findItem(R.id.menu_favor)?.setTitle(R.string.unFavor)
                        } else {
                            mMenu?.findItem(R.id.menu_favor)?.setIcon(R.drawable.ic_favorite_border_white_24dp)
                            mMenu?.findItem(R.id.menu_favor)?.setTitle(R.string.favor)
                        }
                    }

                    once = NetManager.parseOnce(body)
                }

                val mAllContent = mutableListOf<BaseModel>()
                mAllContent.clear()
                mAllContent.add(0, topicHeader!!)
                mAllContent.addAll(repliesOne)

                XLog.tag("DetailsActivity").d("got page 1 , next is more page")

                val totalPage = NetManager.getPageValue(body)[1]  // [2,3]

                currentPage = NetManager.getPageValue(body)[0]
                runOnUiThread {
                    swipe_details.isRefreshing = false
                    mAdapter.updateItems(mAllContent)
                    if (totalPage == 1 && scrollToBottom) {
                        detail_recycler_view.scrollToPosition(mAdapter.itemCount - 1)
                    }
                }

                if (totalPage > 1) {
                    XLog.tag("DetailsActivity").d(totalPage.toString())
                    getMoreRepliesByOrder(totalPage, scrollToBottom)
                }
            }
        })
    }

    private fun parseIsFavored(body: Element): Boolean {
        val p = Pattern.compile("un(?=favorite/topic/\\d{1,10}\\?t=)")
        val matcher = p.matcher(body.outerHtml())
        return matcher.find()
    }

    private fun getMoreRepliesByOrder(totalPage: Int, scrollToBottom: Boolean) {
        val intentGetMoreReply = Intent(this@DetailsActivity, MoreReplyService::class.java)
        intentGetMoreReply.action = "im.fdx.v2ex.get.other.more"
        intentGetMoreReply.putExtra("page", totalPage)
        intentGetMoreReply.putExtra("topic_id", mTopicId)
        intentGetMoreReply.putExtra("bottom", scrollToBottom)
        startService(intentGetMoreReply)
        XLog.tag("DetailsActivity").d("yes I startIntentService")
    }


    @Suppress("unused")
    private fun getNextReplies(totalPage: Int, currentPage: Int) {
        val intentGetMoreReply = Intent(this@DetailsActivity, MoreReplyService::class.java)
        intentGetMoreReply.action = "im.fdx.v2ex.get.one.more"
        intentGetMoreReply.putExtra("page", totalPage)
        intentGetMoreReply.putExtra("topic_id", mTopicId)
        intentGetMoreReply.putExtra("currentPage", currentPage)
        startService(intentGetMoreReply)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        if (MyApp.get().isLogin()) {
            menu.findItem(R.id.menu_favor).isVisible = true
            menu.findItem(R.id.menu_reply)?.isVisible = true
        } else {
            menu.findItem(R.id.menu_favor).isVisible = false
            menu.findItem(R.id.menu_reply)?.isVisible = false
        }
        mMenu = menu
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menu_favor -> favorOrNot(mTopicId, token!!, isFavored)
            R.id.menu_reply -> {
                et_post_reply.requestFocus()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            R.id.menu_refresh -> {
                swipe_details.isRefreshing = true
                getRepliesPageOne(mTopicId, false)
            }
            R.id.menu_item_share -> share("来自V2EX的帖子：${(mAdapter.mAllList[0] as Topic).title} \n" +
                    " ${NetManager.HTTPS_V2EX_BASE}/t/${(mAdapter.mAllList[0] as Topic).id}")
            R.id.menu_item_open_in_browser -> {

                val topicId = (mAdapter.mAllList[0] as Topic).id
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

        return super.onOptionsItemSelected(item)
    }

    private fun favorOrNot(topicId: String, token: String, doFavor: Boolean) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url("${NetManager.HTTPS_V2EX_BASE}/${if (doFavor) "un" else ""}favorite/topic/$topicId?t=$token")
                .get().build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                dealError(this@DetailsActivity, swipe = swipe_details)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 302) {
                    runOnUiThread {
                        toast("${if (doFavor) "取消" else ""}收藏成功")
                        swipe_details.isRefreshing = true
                        getRepliesPageOne(mTopicId, false)
                    }
                }
            }
        })
    }


    fun postReply() {
        et_post_reply.clearFocus()
        logd("I clicked")
        val content = et_post_reply.text.toString()
        val requestBody = FormBody.Builder()
                .add("content", content)
                .add("once", once!!)
                .build()

        pb.visibility = View.VISIBLE
        iv_send.visibility = View.INVISIBLE

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .header("Origin", NetManager.HTTPS_V2EX_BASE)
                .header("Referer", NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .post(requestBody)
                .build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    pb.visibility = View.GONE
                    iv_send.visibility = View.VISIBLE
                }
                dealError(this@DetailsActivity, swipe = swipe_details)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                runOnUiThread {
                    pb.visibility = View.GONE
                    iv_send.visibility = View.VISIBLE
                    if (response.code() == 302) {
                        logd("成功发布")
                        toast("发表评论成功")
                        et_post_reply.setText("")
                        swipe_details.isRefreshing = true
                        getRepliesPageOne(mTopicId, true)
                    } else {
                        toast("发表评论失败")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.tag("DetailsActivity").d("onDestroy")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    companion object {

        const val POSITION_START = 0
        private const val MSG_OK_GET_TOPIC = 0
        private const val MSG_ERROR_AUTH = 1
        private const val MSG_ERROR_IO = 2
        @Suppress("unused")
        private val MSG_GET_MORE_REPLY = 4
    }
}
