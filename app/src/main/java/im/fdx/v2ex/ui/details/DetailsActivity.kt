package im.fdx.v2ex.ui.details

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.ui.main.TopicModel
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.t
import im.fdx.v2ex.view.SmoothLayoutManager
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class DetailsActivity : AppCompatActivity() {

    private var mAdapter: DetailsAdapter? = null
    private val mAllContent = ArrayList<BaseModel>()
    private var mMenu: Menu? = null

    private lateinit var ivSend: ImageView
    private lateinit var mSwipe: SwipeRefreshLayout
    private lateinit var rvDetail: RecyclerView
    private lateinit var etSendReply: EditText
    private lateinit var tvToolbar: TextView

    private lateinit var mTopicId: String
    private var topicHeader: TopicModel? = null
    private var token: String? = null
    private var isFavored: Boolean = false
    private var once: String? = null
    private var currentPage: Int = 0 // TODO: 2017/5/30 滑动加载，减少流量

    private val callback = object : DetailsAdapter.AdapterCallback {
        override fun onMethodCallback(type: Int) {
            when (type) {
                1 -> {
                }
                2 -> getMoreRepliesByOrder(1, false)
            }
        }
    }


    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            XLog.tag(TAG).d("get in lbc:" + intent.action)
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
                mAllContent.addAll(rm)
                mAdapter!!.notifyDataSetChanged()

                if (intent.getBooleanExtra("bottom", false)) {
                    rvDetail.scrollToPosition(mAllContent.size - 1)
                }
            }
        }

    }
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_OK_GET_TOPIC -> mSwipe.isRefreshing = false
            MSG_ERROR_AUTH -> {
                t("需要登录后查看该主题")
                this@DetailsActivity.finish()
            }
            MSG_GO_TO_BOTTOM -> rvDetail.scrollToPosition(mAllContent.size - 1)
            MSG_ERROR_IO -> {
                mSwipe.isRefreshing = false
                t("无法打开该主题")
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
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        tvToolbar = toolbar.findViewById(R.id.tv_toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = ""
        }


        rvDetail = findViewById(R.id.detail_recycler_view)
        //// 这个Scroll 到顶部的bug，卡了我一个星期，用了SO上的方法，自定义了一个LinearLayoutManager
        val mLayoutManager = SmoothLayoutManager(this)
        rvDetail.layoutManager = mLayoutManager
        rvDetail.smoothScrollToPosition(POSITION_START)
//        rvDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && etSendReply.hasFocus()) {
//                    etSendReply.clearFocus()
//                }
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//            }
//        })

        rvDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var currentPosition = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

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

        mAdapter = DetailsAdapter(this@DetailsActivity, mAllContent, callback)
        rvDetail.adapter = mAdapter

        mSwipe = findViewById(R.id.swipe_details)
        mSwipe.setColorSchemeResources(R.color.accent_orange)
        mSwipe.setOnRefreshListener { getRepliesPageOne(mTopicId, false) }

        ivSend = findViewById(R.id.iv_send)
        etSendReply = findViewById(R.id.et_post_reply)
        etSendReply.setOnFocusChangeListener { v, hasFocus ->

            if (!hasFocus) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        etSendReply.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) = if (s.isNullOrEmpty()) {
                ivSend.isClickable = false
                ivSend.imageTintList = null
            } else {
                ivSend.isClickable = true
                ivSend.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@DetailsActivity, R.color.primary))
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
            data != null -> {
                val params = data.pathSegments
                params[1]
            }
            intent.getParcelableExtra<Parcelable>("model") != null -> {
                val topicModel = intent.getParcelableExtra<TopicModel>("model")
                mAllContent.add(0, topicModel)
                topicModel.id
            }
            intent.getStringExtra(Keys.KEY_TOPIC_ID) != null -> intent.getStringExtra(Keys.KEY_TOPIC_ID)
            else -> ""
        }

        getRepliesPageOne(mTopicId, false)

        XLog.tag(TAG).d("TopicUrl: ${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    private fun getRepliesPageOne(topicId: String, scrollToBottom: Boolean) {

        mSwipe.isRefreshing = true
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(NetManager.HTTPS_V2EX_BASE + "/t/" + topicId + "?p=" + "1")
                .build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                handler.sendEmptyMessage(MSG_ERROR_IO)
                XLog.tag("DetailsActivity").d("failed " + e.message)
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
                    dealError(this@DetailsActivity, code, mSwipe)
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
                    XLog.tag(TAG).d("verify" + token!!)
                    mAdapter!!.verifyCode = token!!
                    isFavored = parseIsFavored(body)

                    XLog.tag(TAG).d("isfavored" + isFavored.toString())
                    runOnUiThread {
                        if (isFavored) {
                            mMenu!!.findItem(R.id.menu_favor).setIcon(R.drawable.ic_favorite_white_24dp)
                            mMenu!!.findItem(R.id.menu_favor).setTitle(R.string.unFavor)
                        } else {
                            mMenu!!.findItem(R.id.menu_favor).setIcon(R.drawable.ic_favorite_border_white_24dp)
                            mMenu!!.findItem(R.id.menu_favor).setTitle(R.string.favor)
                        }
                    }
                }

                once = NetManager.parseOnce(body)

                mAllContent.clear()
                mAllContent.add(0, topicHeader!!)
                mAllContent.addAll(repliesOne)

                XLog.tag(TAG).d("get first page done, next is get more page")

                val totalPage = NetManager.getPageValue(body)[1]  // [2,3]

                currentPage = NetManager.getPageValue(body)[0]
                handler.post {
                    mAdapter?.notifyDataSetChanged()
                    mSwipe.isRefreshing = false
                    if (totalPage == 1 && scrollToBottom) {
                        handler.sendEmptyMessage(MSG_GO_TO_BOTTOM)
                    }
                }

                if (totalPage > 1) {
                    XLog.tag(TAG).d(totalPage)
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
        XLog.tag(TAG).d("yes I startIntentService")
    }


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
                etSendReply.requestFocus()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            R.id.menu_refresh -> {
                mSwipe.isRefreshing = true
                getRepliesPageOne(mTopicId, false)
            }
            R.id.menu_item_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "来自V2EX的帖子：${(mAllContent[0] as TopicModel).title}  " +
                                " ${NetManager.HTTPS_V2EX_BASE}/t/${(mAllContent[0] as TopicModel).id}")
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "分享到"))
            }
            R.id.menu_item_open_in_browser -> {

                val topicId = (mAllContent[0] as TopicModel).id
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
                .headers(HttpHelper.baseHeaders)
                .url("${NetManager.HTTPS_V2EX_BASE}/${if (doFavor) "un" else ""}favorite/topic/$topicId?t=$token")
                .get().build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                dealError(this@DetailsActivity, swipe = mSwipe)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 302) {
                    runOnUiThread {
                        t("${if (doFavor) "取消" else ""}收藏成功")
                        getRepliesPageOne(mTopicId, false)
                    }
                }
            }
        })
    }


    @Suppress("UNUSED_PARAMETER")
    fun postReply(view: View) {
        etSendReply.clearFocus()
        XLog.tag(TAG).d("I clicked")
        val content = etSendReply.text.toString()
        val requestBody = FormBody.Builder()
                .add("content", content)
                .add("once", once!!)
                .build()
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .header("Origin", NetManager.HTTPS_V2EX_BASE)
                .header("Referer", NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(NetManager.HTTPS_V2EX_BASE + "/t/" + mTopicId)
                .post(requestBody)
                .build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                dealError(this@DetailsActivity, swipe = mSwipe)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.code() == 302) {
                    XLog.tag(TAG).d("成功发布")
                    handler.post {
                        t("发表评论成功")
                        etSendReply.setText("")
                        getRepliesPageOne(mTopicId, true)
                    }
                } else {
                    t("发表评论失败")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.tag(TAG).d("onDestroy")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    companion object {

        private val TAG = DetailsActivity::class.java.simpleName
        val POSITION_START = 0
        private val MSG_OK_GET_TOPIC = 0
        private val MSG_ERROR_AUTH = 1
        private val MSG_ERROR_IO = 2
        private val MSG_GO_TO_BOTTOM = 3
        private val MSG_GET_MORE_REPLY = 4
    }
}
