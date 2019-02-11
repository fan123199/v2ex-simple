package im.fdx.v2ex.ui.details

import android.content.*
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.initTheme
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details_content.*
import kotlinx.android.synthetic.main.footer_reply.*
import okhttp3.*
import org.jetbrains.anko.share
import org.jetbrains.anko.toast
import java.io.IOException


class TopicActivity : BaseActivity() {

  private lateinit var mAdapter: TopicDetailAdapter
  private var mMenu: Menu? = null

  private lateinit var mTopicId: String
  private var token: String? = null
  private var once: String = ""

  private var topicHeader: Topic? = null
  private var isFavored: Boolean = false
  private var isThanked: Boolean = false


  private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      XLog.tag("TopicActivity").d("get in broadcast: " + intent.action)
      if (intent.action == Keys.ACTION_LOGIN) {
        invalidateOptionsMenu()
        setFootView(true)
      } else if (intent.action == Keys.ACTION_LOGOUT) {
        invalidateOptionsMenu()
        setFootView(false)
      } else if (intent.action == Keys.ACTION_GET_MORE_REPLY) {
        logd("MSG_GET  LocalBroadCast")
        if(intent.getStringExtra(Keys.KEY_TOPIC_ID) == mTopicId) {
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
  private val handler = Handler(Handler.Callback { msg ->
    when (msg.what) {
      MSG_OK_GET_TOPIC -> swipe_details.isRefreshing = false
      MSG_ERROR_AUTH -> {
        toast("需要登录后查看该主题")
        this@TopicActivity.finish()
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
    filter.addAction(Keys.ACTION_GET_MORE_REPLY)
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    setFootView(MyApp.get().isLogin)

    setUpToolbar()

    //// 这个Scroll 到顶部的bug，是focus的原因，focus会让系统自动滚动
    val mLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    detail_recycler_view.layoutManager = mLayoutManager
    detail_recycler_view.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

      private var currentPosition = 0

      override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {}

      override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
          if (currentPosition != 0) {
            startAlphaAnimation(tv_toolbar, 500, false)
            currentPosition = 0
          }
        } else {
          if (currentPosition == 0 && topicHeader != null) {
            tv_toolbar.text = topicHeader?.title
            startAlphaAnimation(tv_toolbar, 500, true)
            currentPosition = -1
          }
        }
      }
    })

    mAdapter = TopicDetailAdapter(this@TopicActivity) { position: Int ->
      detail_recycler_view.smoothScrollToPosition(position)
    }
    detail_recycler_view.adapter = mAdapter
    swipe_details.initTheme()
    swipe_details.setOnRefreshListener { getRepliesPageOne(false) }

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

      override fun afterTextChanged(s: Editable) = if (s.isEmpty()) {
        iv_send.isClickable = false
        iv_send.imageTintList = null
      } else {
        iv_send.isClickable = true
        iv_send.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@TopicActivity, R.color.primary))
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
        mAdapter.topics[0] = topicModel
        mAdapter.notifyDataSetChanged()
        topicModel.id
      }
      intent.getStringExtra(Keys.KEY_TOPIC_ID) != null -> intent.getStringExtra(Keys.KEY_TOPIC_ID)
      else -> ""
    }

    swipe_details.isRefreshing = true
    getRepliesPageOne(false)
    logd("TopicUrl: ${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId")
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    parseIntent(intent)
  }

  private fun getRepliesPageOne(scrollToBottom: Boolean) {
    vCall("${NetManager.HTTPS_V2EX_BASE}/t/$mTopicId?p=1").start(object : Callback {

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

        val parser = Parser(bodyStr)
        topicHeader = parser.parseResponseToTopic(mTopicId)
        val repliesFirstPage = parser.getReplies()

        if (myApp.isLogin) {
          token = parser.getVerifyCode()

          if (token == null) {
            myApp.setLogin(false)
            handler.sendEmptyMessage(MSG_ERROR_AUTH)
            return
          }
          logd("verifyCode is :" + token!!)
          mAdapter.verifyCode = token!!
          isFavored = parser.isTopicFavored()
          isThanked = parser.isTopicThanked()
          once = parser.getOnceNum()

          logd("is favored: " + isFavored.toString())
          runOnUiThread {
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
        runOnUiThread {
          swipe_details.isRefreshing = false
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
    val intentGetMoreReply = Intent(this@TopicActivity, MoreReplyService::class.java)
    intentGetMoreReply.action = "im.fdx.v2ex.get.other.more"
    intentGetMoreReply.putExtra("page", totalPage)
    intentGetMoreReply.putExtra("topic_id", mTopicId)
    intentGetMoreReply.putExtra("bottom", scrollToBottom)
    startService(intentGetMoreReply)
    logd("yes I startIntentService")
  }


  @Suppress("unused")
  private fun getNextReplies(totalPage: Int, currentPage: Int) {
    val intentGetMoreReply = Intent(this@TopicActivity, MoreReplyService::class.java)
    intentGetMoreReply.run {
      action = "im.fdx.v2ex.get.one.more"
      putExtra("page", totalPage)
      putExtra("topic_id", mTopicId)
      putExtra("currentPage", currentPage)
    }
    startService(intentGetMoreReply)
  }


  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_details, menu)
    if (MyApp.get().isLogin) {
      menu.findItem(R.id.menu_favor).isVisible = true
      menu.findItem(R.id.menu_thank_topic)?.isVisible = true
    } else {
      menu.findItem(R.id.menu_favor).isVisible = false
      menu.findItem(R.id.menu_thank_topic)?.isVisible = false
    }
    mMenu = menu
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {

      R.id.menu_favor -> token?.let { favorOrNot(mTopicId, it, isFavored) }
      R.id.menu_thank_topic -> { token?.let { thankTopic(mTopicId, it, isThanked) } }
      R.id.menu_item_share -> share("来自V2EX的帖子：${(mAdapter.topics[0]).title} \n" +
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

    return super.onOptionsItemSelected(item)
  }

  private fun favorOrNot(topicId: String, token: String, doFavor: Boolean) {
    vCall("${NetManager.HTTPS_V2EX_BASE}/${if (doFavor) "un" else ""}favorite/topic/$topicId?t=$token")
        .start(object : Callback {

          override fun onFailure(call: Call, e: IOException) {
            dealError(this@TopicActivity, swipe = swipe_details)
          }

          @Throws(IOException::class)
          override fun onResponse(call: Call, response: Response) {
            if (response.code() == 302) {
              runOnUiThread {
                toast("${if (doFavor) "取消" else ""}收藏成功")
                swipe_details.isRefreshing = true
                getRepliesPageOne(false)
              }
            }
          }
        })
  }

//    https@ //www.v2ex.com/thank/topic/529363?t=emdnsipiydbckrgywnjvwjcgkhandjud
  private fun thankTopic(topicId :String, token :String, isThanked :Boolean) {
    if (isThanked) return
    val body = FormBody.Builder().add("t", token).build()
    HttpHelper.OK_CLIENT.newCall(Request.Builder()
            .url("https://www.v2ex.com/thank/topic/$topicId")
            .post(body)
            .build())
            .start(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        NetManager.dealError(this@TopicActivity)
      }

      override fun onResponse(call: Call, response: Response) {
        if (response.code() == 200) {
          runOnUiThread {
            toast("感谢成功")
            mMenu?.findItem(R.id.menu_thank_topic)?.setTitle(R.string.already_thank)
          }
        } else {
          NetManager.dealError(this@TopicActivity, response.code())
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
        runOnUiThread {
          pb_send.visibility = View.GONE
          iv_send.visibility = View.VISIBLE
        }
        dealError(this@TopicActivity, swipe = swipe_details)
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: okhttp3.Response) {
        runOnUiThread {
          pb_send.visibility = View.GONE
          iv_send.visibility = View.VISIBLE
          if (response.code() == 302) {
            logd("成功发布")
            toast("发表评论成功")
            et_post_reply.setText("")
            swipe_details.isRefreshing = true
            getRepliesPageOne(true)
          } else {
            toast("发表评论失败")
            swipe_details.isRefreshing = true
            getRepliesPageOne(true)
          }
        }
      }
    })
  }

  override fun onDestroy() {
    super.onDestroy()
    XLog.tag("TopicActivity").d("onDestroy")
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
  }

  companion object {

    private const val MSG_OK_GET_TOPIC = 0
    private const val MSG_ERROR_AUTH = 1
    private const val MSG_ERROR_IO = 2
  }
}
