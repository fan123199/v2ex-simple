package im.fdx.v2ex.ui.member

import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.API_TOPIC
import im.fdx.v2ex.network.NetManager.API_USER
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.NetManager.myGson
import im.fdx.v2ex.ui.main.TopicModel
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.view.CustomChrome
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


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
    private lateinit var llInfo: ViewGroup
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    private lateinit var mMenu: Menu

    private lateinit var member: MemberModel
    private val mTopics = ArrayList<TopicModel>()

    private var username: String? = null
    private var urlTopic: String? = null
    private var blockOfT: String? = null
    private var followOfOnce: String? = null
    private var isBlocked: Boolean = false
    private var isFollowed: Boolean = false


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

        constraintLayout = findViewById(R.id.constraint_member)
        collapsingToolbarLayout = findViewById(R.id.ctl_profile)
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

        val upToolbar = setUpToolbar()

        username = getName()
        upToolbar.title = username
        val tabLayout: TabLayout = findViewById(R.id.tl_member)
        val viewpager: ViewPager = findViewById(R.id.viewpager)
        viewpager.adapter = MemberViewpagerAdapter(fragmentManager, username!!)
        tabLayout.setupWithViewPager(viewpager)

        val appBarLayout: AppBarLayout = findViewById(R.id.al_profile)

        appBarLayout.addOnOffsetChangedListener { appBarLayout1, verticalOffset ->

            val maxScroll = appBarLayout1.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()
            when (percentage) {
                in 0f..1f -> constraintLayout.alpha = 1 - percentage
            }
        }
        getData()
    }

    private fun getName() = when {
        intent.data != null -> intent.data.pathSegments[1]
        intent.extras != null -> intent.extras.getString(Keys.KEY_USERNAME)
        BuildConfig.DEBUG -> "Livid"
        else -> "null"
    }

    private fun getData() {
        val urlUserInfo = API_USER + "?username=" + username  //Livid's profile
        collapsingToolbarLayout.title = username
        urlTopic = "$API_TOPIC?username=$username"
        Log.i(TAG, "$urlUserInfo: \t$urlTopic")
        getUserInfoAPI(urlUserInfo)
        getBlockAndFollowWeb()
    }

    private fun getBlockAndFollowWeb() {
        if (username == PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.KEY_USERNAME, "")) {
            return
        }
        val webUrl = "https://www.v2ex.com/member/" + username
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url(webUrl)
                .get().build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 200) {
                    val html = response.body()!!.string()
                    isBlocked = isBlock(html)
                    isFollowed = isFollowed(html)
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

                    blockOfT = geOnceInBlock(html)

                    if (blockOfT == null) {
                        MyApp.get().setLogin(false)
                    }
                    followOfOnce = getOnceInFollow(html)
                }
            }
        })
    }

    private fun getUserInfoAPI(urlUserInfo: String) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url(urlUserInfo).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.code() != 200) {
                    dealError(this@MemberActivity)
                } else {
                    val body = response.body()?.string()
                    runOnUiThread {
                        body?.let { showUser(it) }
                    }
                }
            }
        })
    }

    private var listener: View.OnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.tv_location -> {
            }
            R.id.tv_github -> if (!(member.github).isEmpty()) CustomChrome(this).load("https://www.github.com/" + member.github)
            R.id.tv_twitter -> {
                if (!(member.twitter).isEmpty()) {
                    val intent: Intent
                    try {
                        packageManager.getPackageInfo("com.twitter.android", 0)
                        intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("twitter://user?screen_name=" + member.twitter))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: Exception) {
                        CustomChrome(this).load("https://twitter.com/" + member.twitter)
                    }
                }
            }
            R.id.tv_website -> when {
                !(member.website).isEmpty() ->
                    CustomChrome(this).load(if (!member.website.contains("http")) "http://"
                            + member.website else member.website)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showUser(response: String) {
        member = myGson.fromJson(response, MemberModel::class.java)

        mIvAvatar.load(member.avatarLargeUrl)
        mTvIntro.text = member.bio
        mTvUserCreatedPrefix.text = "加入于${TimeUtil.getAbsoluteTime((member.created).toLong())},${getString(R.string.the_n_member, member.id)}"

        mTvBitCoin.visibility = if ((member.btc).isEmpty()) View.GONE else View.VISIBLE
        mTvGithub.visibility = if ((member.github).isEmpty()) View.GONE else View.VISIBLE
        mTvLocation.visibility = if ((member.location).isEmpty()) View.GONE else View.VISIBLE
        mTvTwitter.visibility = if ((member.twitter).isEmpty()) View.GONE else View.VISIBLE
        mTvWebsite.visibility = if ((member.website).isEmpty()) View.GONE else View.VISIBLE

        mTvIntro.visibility = if ((member.bio).isEmpty()) View.GONE else View.VISIBLE

        llInfo.visibility = when {
            !(member.website + member.twitter + member.github + member.btc + member.location).isEmpty() -> View.VISIBLE
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
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
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
                        toast("${if (isFollowed) "取消" else ""}关注成功")
                    }
                }
            }
        })
    }

    private fun switchBlockAndRefresh(isBlocked: Boolean) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
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
                                toast("${if (isBlocked) "取消" else ""}屏蔽成功")
                            }
                        }
                    }
                })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        username = getName()
        getData()
    }

    companion object {
        var TAG: String = MemberActivity::class.java.simpleName

        private fun isFollowed(html: String) = Regex("un(?=follow/\\d{1,8}\\?once=)").containsMatchIn(html)

        private fun getOnceInFollow(html: String): String? = Regex("follow/\\d{1,8}\\?once=\\d{1,10}").find(html)?.value

        private fun isBlock(html: String) = Regex("un(?=block/\\d{1,8}\\?t=)").containsMatchIn(html)

        private fun geOnceInBlock(html: String): String? = Regex("block/\\d{1,8}\\?t=\\d{1,20}").find(html)?.value

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

    inner class MemberViewpagerAdapter(fm: FragmentManager?, var username: String) : FragmentPagerAdapter(fm) {

        private val titles = arrayOf("主题", "评论")

        override fun getItem(position: Int) = when (position) {
            0 -> TopicsFragment().apply { arguments = bundleOf(Keys.KEY_USERNAME to username) }
            else -> ReplyFragment().apply { arguments = bundleOf(Keys.KEY_USERNAME to username) }
        }

        override fun getCount() = titles.size
        override fun getPageTitle(position: Int) = titles[position]

    }
}
