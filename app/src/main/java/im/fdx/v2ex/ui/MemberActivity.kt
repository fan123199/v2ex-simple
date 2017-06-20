package im.fdx.v2ex.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.MemberModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.API_TOPIC
import im.fdx.v2ex.network.NetManager.API_USER
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.NetManager.myGson
import im.fdx.v2ex.ui.main.TopicModel
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.utils.HintUI
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.showNoContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


/**
 * 获取user的主题，依然使用api的方式
 */
class MemberActivity : AppCompatActivity() {
    private var mTvUsername: TextView? = null
    private var mIvAvatar: ImageView? = null
    private var mTvId: TextView? = null
    private var mTvUserCreated: TextView? = null
    private var mTvIntro: TextView? = null
    private var mTvLocation: TextView? = null
    private var mTvBitCoin: TextView? = null
    private var mTvGithub: TextView? = null
    private var mTvTwitter: TextView? = null
    private var mTvWebsite: TextView? = null
    private val mTopics = ArrayList<TopicModel>()

    private var username: String? = null
    private var mAdapter: TopicsRVAdapter? = null
    private var urlTopic: String? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private var member: MemberModel? = null
    private var blockOfT: String? = null
    private var followOfOnce: String? = null
    private var isBlocked: Boolean = false
    private var isFollowed: Boolean = false
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var container: FrameLayout
    private var mMenu: Menu? = null
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_GET_USER_INFO -> showUser(msg.obj as String)
            MSG_GET_TOPIC -> {
                mAdapter?.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mTvUsername = findViewById(R.id.tv_username_profile)
        mIvAvatar = findViewById(R.id.iv_avatar_profile)
        mTvId = findViewById(R.id.tv_id)
        mTvUserCreated = findViewById(R.id.tv_created)
        mTvIntro = findViewById(R.id.tv_intro)
        mTvLocation = findViewById(R.id.tv_location)
        mTvBitCoin = findViewById(R.id.tv_bitcoin)
        mTvGithub = findViewById(R.id.tv_github)
        mTvTwitter = findViewById(R.id.tv_twitter)
        mTvWebsite = findViewById(R.id.tv_website)

        run {
            mTvLocation!!.visibility = View.GONE
            mTvBitCoin!!.visibility = View.GONE
            mTvGithub!!.visibility = View.GONE
            mTvTwitter!!.visibility = View.GONE
            mTvWebsite!!.visibility = View.GONE
        }


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressed() }

        swipeRefreshLayout = findViewById(R.id.swipe_container)
        swipeRefreshLayout.setOnRefreshListener({ this.getTopicsByUsernameAPI() })

        val appBarLayout: AppBarLayout = findViewById(R.id.al_profile)

        collapsingToolbarLayout = findViewById(R.id.ctl_profile)
        appBarLayout.addOnOffsetChangedListener { appBarLayout1, verticalOffset ->

            val maxScroll = appBarLayout1.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()
            handleAlphaOnTitle(percentage)
        }

        constraintLayout = findViewById(R.id.constraint_member)

        container = findViewById(R.id.fl_container)

        val rv: RecyclerView = findViewById(R.id.rv_container)
        val layoutManager = LinearLayoutManager(this@MemberActivity)

        rv.layoutManager = layoutManager
        mAdapter = TopicsRVAdapter(this, mTopics)
        rv.adapter = mAdapter
        parseIntent(intent)
    }

    private fun handleAlphaOnTitle(percentage: Float) {
        if (percentage > 0.8f && percentage <= 1f) {
            constraintLayout.visibility = View.INVISIBLE
        } else if (percentage <= 0.8f && percentage >= 0f) {
            constraintLayout.visibility = View.VISIBLE
        }

    }

    private fun parseIntent(intent: Intent) {

        val appLinkData = intent.data
        var urlUserInfo = ""
        when {
            appLinkData != null -> {
                val scheme = appLinkData.scheme
                val host = appLinkData.host
                val params = appLinkData.pathSegments
                if (host.contains("v2ex.com") && params[0].contains("member")) {
                    username = params[1]
                    urlUserInfo = API_USER + "?username=" + username
                }
            }
            intent.extras != null -> {
                username = getIntent().extras.getString(Keys.KEY_USERNAME)
                urlUserInfo = API_USER + "?username=" + username
            }
            BuildConfig.DEBUG -> {
                username = "Livid"
                urlUserInfo = API_USER + "?username=" + username  //Livid's profile
            }
        }
        collapsingToolbarLayout.title = username
        urlTopic = "$API_TOPIC?username=$username"
        Log.i(TAG, "$urlUserInfo: \t$urlTopic")
        //// TODO: 2017/3/20 可以改成一步，分析下性能
        getUserInfoAPI(urlUserInfo)
        getTopicsByUsernameAPI()
        getBlockAndFollowWeb()
    }

    private fun getBlockAndFollowWeb() {
        if (username == PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.KEY_USERNAME, "")) {
            return
        }
        val webUrl = "https://www.v2ex.com/member/" + username
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(webUrl)
                .get().build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 200) {
                    val html = response.body()!!.string()
                    val body = Jsoup.parse(html).body()
                    isBlocked = parseIsBlock(html)
                    isFollowed = parseIsFollowed(html)
                    XLog.d("isBlocked: $isBlocked|isFollowed: $isFollowed")


                    runOnUiThread {
                        if (isBlocked) {
                            mMenu!!.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp)
                        } else {
                            mMenu!!.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp)
                        }

                        if (isFollowed) {
                            mMenu!!.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_white_24dp)
                        } else {
                            mMenu!!.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp)
                        }

                    }


                    blockOfT = parseToBlock(html)

                    if (blockOfT == null) {
                        MyApp.get().setLogin(false)
                    }
                    followOfOnce = parseToOnce(html)
                }
            }
        })
    }

    private fun parseIsFollowed(html: String): Boolean {
        val pFollow = Pattern.compile("un(?=follow/\\d{1,8}\\?once=)")
        val matcher = pFollow.matcher(html)
        return matcher.find()

    }

    private fun parseIsBlock(html: String): Boolean {
        val pFollow = Pattern.compile("un(?=block/\\d{1,8}\\?t=)")
        val matcher = pFollow.matcher(html)
        return matcher.find()
    }

    private fun parseToOnce(html: String): String? {

        //        <input type="button" value="加入特别关注"
        // onclick="if (confirm('确认要开始关注 SoulGem？'))
        // { location.href = '/follow/209351?once=61676'; }" class="super special button">

        val pFollow = Pattern.compile("follow/\\d{1,8}\\?once=\\d{1,10}")
        val matcher = pFollow.matcher(html)
        if (matcher.find()) {
            return matcher.group()
        }
        return null
    }

    /**
     * @param html
     * *
     * @return the whole path url
     */
    private fun parseToBlock(html: String): String? {
        //        <input type="button" value="Block" onclick="if (confirm('确认要屏蔽 SoulGem？'))
        // { location.href = '/block/209351?t=1490028444'; }" class="super normal button">
        val pFollow = Pattern.compile("block/\\d{1,8}\\?t=\\d{1,20}")
        val matcher = pFollow.matcher(html)
        if (matcher.find()) {
            return matcher.group()
        }
        return null
    }

    private fun getUserInfoAPI(urlUserInfo: String) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(urlUserInfo).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body()!!.string()
                Message.obtain(handler, MSG_GET_USER_INFO, body).sendToTarget()
            }
        })
    }

    private fun getTopicsByUsernameAPI() {

        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(urlTopic!!).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {

                val body = response.body()!!.string()
                val type = object : TypeToken<ArrayList<TopicModel>>() {

                }.type
                val topicModels = myGson.fromJson<List<TopicModel>>(body, type)
                if (topicModels == null || topicModels.isEmpty()) {
                    runOnUiThread {
                        swipeRefreshLayout.isRefreshing = false
                        container.showNoContent()
                    }
                    return
                }
                mAdapter!!.updateData(topicModels)
                XLog.tag("profile").i(topicModels[0].title)
                Message.obtain(handler, MSG_GET_TOPIC).sendToTarget()
            }
        })
    }

    fun openTwitter(view: View) {
        if (TextUtils.isEmpty(member!!.twitter)) {
            return
        }
        var intent: Intent
        try {
            // get the Twitter app if possible
            this.packageManager.getPackageInfo("com.twitter.android", 0)
            intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=" + member!!.twitter))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (e: Exception) {
            // no Twitter app, revert to browser
            intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/" + member!!.twitter))
        }

        startActivity(intent)
    }

    fun openWeb(view: View) {
        if (TextUtils.isEmpty(member?.website)) {
            return
        }
        val text: String
        if (!member?.website?.contains("http")!!) {
            text = "http://" + member!!.website
        } else {
            text = member!!.website
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
        startActivity(intent)
    }

    fun openGithub(view: View) {
        if (TextUtils.isEmpty(member!!.github)) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.github.com/" + member!!.github))
        startActivity(intent)
    }

    private fun showUser(response: String) {
        member = myGson.fromJson(response, MemberModel::class.java)
        mTvUsername!!.text = member!!.username

        Picasso.with(this).load(member!!.avatarLargeUrl)
                .error(R.drawable.ic_person_outline_black_24dp).into(mIvAvatar)
        mTvId!!.text = getString(R.string.the_n_member, member!!.id)
        mTvIntro!!.text = member!!.bio
        mTvUserCreated!!.text = TimeUtil.getAbsoluteTime(java.lang.Long.parseLong(member!!.created))

        val debug_view = false
        if (debug_view || TextUtils.isEmpty(member!!.btc)) {
            mTvBitCoin!!.visibility = View.GONE
        } else {
            mTvBitCoin!!.visibility = View.VISIBLE
            mTvBitCoin!!.text = member!!.btc
        }
        if (debug_view || TextUtils.isEmpty(member!!.github)) {
            mTvGithub!!.visibility = View.GONE
        } else {
            mTvGithub!!.visibility = View.VISIBLE
            mTvGithub!!.text = member!!.github
        }

        if (debug_view || TextUtils.isEmpty(member!!.location)) {
            mTvLocation!!.visibility = View.GONE
        } else {
            mTvLocation!!.visibility = View.VISIBLE
            mTvLocation!!.text = member!!.location
        }

        if (debug_view || TextUtils.isEmpty(member!!.twitter)) {
            mTvTwitter!!.visibility = View.GONE
        } else {
            mTvTwitter!!.visibility = View.VISIBLE
            mTvTwitter!!.text = member!!.twitter
        }

        if (debug_view || TextUtils.isEmpty(member!!.website)) {
            mTvWebsite!!.visibility = View.GONE
        } else {
            mTvWebsite!!.visibility = View.VISIBLE
            mTvWebsite!!.text = member!!.website

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_member, menu)
        this.mMenu = menu
        if (username == PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.KEY_USERNAME, "")) {
            menu.findItem(R.id.menu_block).isVisible = false
            menu.findItem(R.id.menu_follow).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        XLog.d("onOptionsItemSelected")

        when (item.itemId) {
            R.id.menu_follow -> switchFollowAndRefresh(isFollowed)
            R.id.menu_block -> switchBlockAndRefresh(isBlocked)
            else -> return super.onOptionsItemSelected(item)
        }
        return true

    }

    private fun switchFollowAndRefresh(isFollowed: Boolean) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url("${NetManager.HTTPS_V2EX_BASE}/${if (isFollowed) "un" else ""}$followOfOnce")
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 302) {
                    getBlockAndFollowWeb()
                    runOnUiThread {
                        HintUI.toa(this@MemberActivity, "${if (isFollowed) "取消" else ""}关注成功")
                    }
                }
            }
        })
    }

    private fun switchBlockAndRefresh(isBlocked: Boolean) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url("$HTTPS_V2EX_BASE/${if (isBlocked) "un" else ""}$blockOfT").build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dealError(this@MemberActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 302) {
                            getBlockAndFollowWeb()
                            runOnUiThread {
                                HintUI.toa(this@MemberActivity, "${if (isBlocked) "取消" else ""}屏蔽成功")
                            }
                        }
                    }
                })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    companion object {


        var TAG: String? = MemberActivity::class.java.simpleName
        private val MSG_GET_USER_INFO = 0
        private val MSG_GET_TOPIC = 1

        // 设置渐变的动画
        fun startAlphaAnimation(v: View, duration: Long, visibility: Int) {
            val alphaAnimation = if (visibility == View.VISIBLE)
                AlphaAnimation(0f, 1f)
            else
                AlphaAnimation(1f, 0f)

            alphaAnimation.duration = duration
            alphaAnimation.fillAfter = true
            v.startAnimation(alphaAnimation)
        }
    }

}
