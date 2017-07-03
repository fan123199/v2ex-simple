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
import android.view.ViewGroup
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
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.showNoContent
import im.fdx.v2ex.utils.extensions.t
import im.fdx.v2ex.view.CustomChrome
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


/**
 * 获取user的主题，依然使用api的方式
 */
class MemberActivity : AppCompatActivity() {

    private lateinit var mIvAvatar: ImageView
    private lateinit var mTvUserCreatedPrefix: TextView
    private lateinit var mTvIntro: TextView
    private lateinit var mTvLocation: ImageView
    private lateinit var mTvBitCoin: ImageView
    private lateinit var mTvGithub: ImageView
    private lateinit var mTvTwitter: ImageView
    private lateinit var mTvWebsite: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    private lateinit var llInfo: ViewGroup


    private val mTopics = ArrayList<TopicModel>()

    private var username: String? = null
    private var mAdapter: TopicsRVAdapter? = null
    private var urlTopic: String? = null
    private lateinit var member: MemberModel
    private var blockOfT: String? = null
    private var followOfOnce: String? = null
    private var isBlocked: Boolean = false
    private var isFollowed: Boolean = false
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var container: FrameLayout
    private lateinit var mMenu: Menu
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

        mIvAvatar = findViewById(R.id.iv_avatar_profile)
        mTvUserCreatedPrefix = findViewById(R.id.tv_prefix_created)
        mTvIntro = findViewById(R.id.tv_intro)


        mTvLocation = findViewById(R.id.tv_location)
        mTvBitCoin = findViewById(R.id.tv_bitcoin)
        mTvGithub = findViewById(R.id.tv_github)
        mTvTwitter = findViewById(R.id.tv_twitter)
        mTvWebsite = findViewById(R.id.tv_website)

        llInfo = findViewById(R.id.ll_info)

        run {
            mTvIntro.visibility = View.GONE
            mTvLocation.visibility = View.GONE
            mTvBitCoin.visibility = View.GONE
            mTvGithub.visibility = View.GONE
            mTvTwitter.visibility = View.GONE
            mTvWebsite.visibility = View.GONE
            llInfo.visibility = View.GONE

            mTvLocation.setOnClickListener(listener)
            mTvBitCoin.setOnClickListener(listener)
            mTvGithub.setOnClickListener(listener)
            mTvTwitter.setOnClickListener(listener)
            mTvWebsite.setOnClickListener(listener)
        }


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressed() }

        swipeRefreshLayout = findViewById(R.id.swipe_container)
        swipeRefreshLayout.setOnRefreshListener { getTopicsByUsernameAPI() }

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
        when (percentage) {
            in 0f..1f -> constraintLayout.alpha = 1 - percentage
        }
    }

    private fun parseIntent(intent: Intent) {

        val appLinkData = intent.data
        var urlUserInfo = ""
        when {
            appLinkData != null -> {
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
                NetManager.dealError(this@MemberActivity, swipe = swipeRefreshLayout)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 200) {
                    val html = response.body()!!.string()
                    isBlocked = parseIsBlock(html)
                    isFollowed = parseIsFollowed(html)
                    XLog.d("isBlocked: $isBlocked|isFollowed: $isFollowed")


                    runOnUiThread {
                        if (isBlocked) {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp)
                        } else {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp)
                        }

                        if (isFollowed) {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_white_24dp)
                        } else {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp)
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
                dealError(this@MemberActivity, swipe = swipeRefreshLayout)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body()?.string()
                Message.obtain(handler, MSG_GET_USER_INFO, body).sendToTarget()
            }
        })
    }

    private fun getTopicsByUsernameAPI() {

        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(urlTopic!!).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity, swipe = swipeRefreshLayout)
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


    private var listener: View.OnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.tv_location -> {
            }
            R.id.tv_github -> openGithub()
            R.id.tv_twitter -> openTwitter()
            R.id.tv_website -> openWeb()
        }
    }

    fun openTwitter() {
        if (TextUtils.isEmpty(member.twitter)) {
            return
        }
        val intent: Intent
        try {
            // get the Twitter app if possible
            this.packageManager.getPackageInfo("com.twitter.android", 0)
            intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=" + member.twitter))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            // no Twitter app, revert to browser
            CustomChrome(this).load("https://twitter.com/" + member.twitter)
        }
    }

    fun openWeb() {
        if (TextUtils.isEmpty(member.website)) {
            return
        }
        CustomChrome(this).load(if (!member.website.contains("http")) "http://" + member.website else member.website)
    }

    fun openGithub() {
        if (TextUtils.isEmpty(member.github)) return
        CustomChrome(this).load("https://www.github.com/" + member.github)
    }

    private fun showUser(response: String) {
        member = myGson.fromJson(response, MemberModel::class.java)

        Picasso.with(this).load(member.avatarLargeUrl)
                .error(R.drawable.ic_person_outline_black_24dp).into(mIvAvatar)
        mTvIntro.text = member.bio
        mTvUserCreatedPrefix.text = "加入于${TimeUtil.getAbsoluteTime((member.created).toLong())},${getString(R.string.the_n_member, member.id)}"

        mTvBitCoin.visibility = when {
            TextUtils.isEmpty(member.btc) -> View.GONE
            else -> View.VISIBLE
        }
        mTvGithub.visibility = when {
            TextUtils.isEmpty(member.github) -> View.GONE
            else -> View.VISIBLE
        }

        mTvLocation.visibility = when {
            TextUtils.isEmpty(member.location) -> View.GONE
            else -> View.VISIBLE
        }

        mTvTwitter.visibility = when {
            TextUtils.isEmpty(member.twitter) -> View.GONE
            else -> View.VISIBLE
        }

        mTvWebsite.visibility = when {
            TextUtils.isEmpty(member.website) -> View.GONE
            else -> View.VISIBLE
        }

        mTvIntro.visibility = when {
            TextUtils.isEmpty(member.bio) -> View.GONE
            else -> View.VISIBLE
        }
        llInfo.visibility = when {
            !TextUtils.isEmpty(member.website + member.twitter + member.github + member.btc + member.location) -> View.VISIBLE
            else -> View.GONE
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
                dealError(this@MemberActivity, swipe = swipeRefreshLayout)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 302) {
                    getBlockAndFollowWeb()
                    runOnUiThread {
                        t("${if (isFollowed) "取消" else ""}关注成功")
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
                        dealError(this@MemberActivity, swipe = swipeRefreshLayout)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 302) {
                            getBlockAndFollowWeb()
                            runOnUiThread {
                                t("${if (isBlocked) "取消" else ""}屏蔽成功")
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


        fun parseToOnce(html: String): String? {

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
