package im.fdx.v2ex.ui.member

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import im.fdx.v2ex.*
import im.fdx.v2ex.databinding.ActivityMemberBinding
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.API_USER
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.NetManager.myGson
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.favor.FavorViewPagerAdapter.Companion.titles
import im.fdx.v2ex.ui.isUsePageNum
import im.fdx.v2ex.ui.main.NewTopicActivity
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.*
import im.fdx.v2ex.view.BottomSheetMenu
import im.fdx.v2ex.view.CustomChrome
import im.fdx.v2ex.view.ViewPagerHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import im.fdx.v2ex.utils.extensions.toast
import java.io.IOException
import kotlin.math.abs


/**
 * 获取user的主题，依然使用api的方式
 */
class MemberActivity : BaseActivity() {

    private var helper: ViewPagerHelper? = null
    private lateinit var mMenu: Menu

    private lateinit var member: Member

    private var username: String? = null
    private var blockOfT: String? = null
    private var followOfOnce: String? = null
    private var isBlocked: Boolean = false
    private var isFollowed: Boolean = false
    private var isOnline: Boolean = false

    private lateinit var binding: ActivityMemberBinding

    private var isMe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        run {
            binding.tvTagline.visibility = View.GONE
            binding.tvIntro.visibility = View.GONE
            binding.ivLocation.visibility = View.GONE
            binding.ivBitcoin.visibility = View.GONE
            binding.ivGithub.visibility = View.GONE
            binding.ivTwitter.visibility = View.GONE
            binding.tvWebsite.visibility = View.GONE
            binding.llInfo.visibility = View.GONE

            binding.ivLocation.setOnClickListener(listener)
            binding.ivBitcoin.setOnClickListener(listener)
            binding.ivGithub.setOnClickListener(listener)
            binding.ivTwitter.setOnClickListener(listener)
            binding.tvWebsite.setOnClickListener(listener)


//            binding.tvMore.setOnClickListener {
//                supportFragmentManager.fragments.forEach {
//                    if (it.isVisible) {
//                        if (it is UserReplyFragment) {
//                            it.togglePageNum(it.isEndlessMode)
//                        } else if (it is TopicsFragment) {
//                            it.togglePageNum(it.isEndlessMode)
//                        }
//                    }
//                }
//            }
        }

        setUpToolbar()

        username = getName(intent)

        if (username.isNullOrEmpty()) {
            toast("未知问题，无法访问用户信息")
            return
        }
        binding.alProfile.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1, verticalOffset ->

            val maxScroll = appBarLayout1.totalScrollRange
            when (val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()) {
                in 0f..1f -> binding.constraintMember.alpha = 1 - percentage
            }
        })


        helper = ViewPagerHelper(binding.viewpager)
//        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                if (position == 0) {
//                    binding.tvMore.isVisible = isHasContentPage1
//                } else {
//                    binding.tvMore.isVisible = isHasContentPage2
//                }
//            }
//        })
        getData()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        helper?.dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun getName(intent: Intent): String? = when {
        intent.data != null -> intent.data!!.pathSegments.getOrNull(1)
        intent.extras != null -> intent.extras!!.getString(Keys.KEY_USERNAME)
        BuildConfig.DEBUG -> "Livid"
        else -> null
    }

    private fun getData() {
        if (username == pref.getString(Keys.PREF_USERNAME, "")) {
            isMe = true
        }
        val urlUserInfo = "$API_USER?username=$username"  //Livid's profile
        binding.ctlProfile.title = username

        getByAPI(urlUserInfo)

        if (isMe) {
        } else {
            getByHtml()
        }
    }

    private fun getByAPI(urlUserInfo: String) {
        vCall(urlUserInfo).start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.code != 200) {
                    dealError(this@MemberActivity)
                } else {
                    val body = response.body!!.string()
                    logi("body:" + body)
                    member = myGson.fromJson(body, Member::class.java)
                    runOnUiThread { showUser() }
                }
            }
        })
    }

    private fun getByHtml() {
        val webUrl = "https://www.v2ex.com/member/$username"
        vCall(webUrl).start(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val html = response.body!!.string()
                    isBlocked = isBlock(html)
                    isFollowed = isFollowed(html)
                    isOnline = isOnline(html)
                    logd("isBlocked: $isBlocked|isFollowed: $isFollowed|isOnline: $isOnline")


                    runOnUiThread {
                        if (isBlocked) {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp)
                            mMenu.findItem(R.id.menu_block).setTitle(R.string.cancel_block)
                        } else {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp)
                            mMenu.findItem(R.id.menu_block).setTitle(R.string.block)
                        }

                        if (isFollowed) {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_blue_24dp)
                            mMenu.findItem(R.id.menu_follow).setTitle(R.string.unfollow)
                        } else {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp)
                            mMenu.findItem(R.id.menu_follow).setTitle(R.string.follow)
                        }

//                        binding.tvMore.text = "显示页码"
                        binding.tvOnline.isVisible = isOnline
                        binding.viewpager.isVisible = !isBlocked

                    }

                    blockOfT = getOnceInBlock(html)
                    followOfOnce = getOnceInFollow(html)

                    if (blockOfT == null || followOfOnce == null) {
                        setLogin(false)
                    }
                }
            }
        })
    }

    private var listener: View.OnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.iv_location -> {
                if (!member.location.isNullOrEmpty()) {
                    val contentView = TextView(this@MemberActivity)
                    contentView.text = member.location
                    val popupWindow = PopupWindow(contentView, WRAP_CONTENT, WRAP_CONTENT)
                    popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                    popupWindow.isOutsideTouchable = true
                    popupWindow.showAsDropDown(it)
                }
            }
            R.id.iv_github -> if (!(member.github).isNullOrEmpty()) CustomChrome(this).load("https://www.github.com/" + member.github)
            R.id.iv_twitter -> {
                if (!member.twitter.isNullOrEmpty()) {
                    val intent: Intent
                    try {
                        packageManager.getPackageInfo("com.twitter.android", 0)
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("twitter://user?screen_name=" + member.twitter)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: Exception) {
                        CustomChrome(this).load("https://twitter.com/" + member.twitter)
                    }
                }
            }
            R.id.tv_website -> when {
                !(member.website).isNullOrEmpty() ->
                    CustomChrome(this).load(
                        if (!member.website!!.contains("http")) "http://"
                                + member.website else member.website!!
                    )
            }
        }
    }

//    fun showMoreBtn(position: Int) {
//        if (position == 0 ) {
//            isHasContentPage1 = true
//        } else {
//            isHasContentPage2 = true
//        }
//        binding.tvMore.isVisible = true
//    }

    fun changeTitle(index:Int, name:String) {
        binding.tlMember.getTabAt(index)?.text = "${titles[index]} ($name)"
    }

    @SuppressLint("SetTextI18n")
    private fun showUser() {
        if (this.isDestroyed) return

        binding.viewpager.offscreenPageLimit = 2
        binding.viewpager.adapter = MemberViewpagerAdapter(this).apply {
            username = member.username
            avatar = member.avatar_normal
        }
        TabLayoutMediator(binding.tlMember, binding.viewpager) { tab, position ->
            tab.text = if (position == 0) "主题" else "回复"
        }.attach()
        binding.ivAvatarProfile.load(member.avatarLargeUrl)
        binding.tvTagline.text = member.tagline
        binding.tvIntro.text = member.bio
        binding.tvPrefixCreated.text =
            "加入于${TimeUtil.getAbsoluteTime((member.created))},${getString(R.string.the_n_member, member.id)}"

        binding.ivBitcoin.isGone = member.btc.isNullOrEmpty()
        binding.ivGithub.isGone = member.github.isNullOrEmpty()
        binding.ivLocation.isGone = member.location.isNullOrEmpty()
        binding.ivTwitter.isGone = member.twitter.isNullOrEmpty()
        binding.tvWebsite.isGone = member.website.isNullOrEmpty()

        binding.tvTagline.isGone = member.tagline.isNullOrEmpty()
        binding.tvIntro.isGone = member.bio.isNullOrEmpty()

        binding.llInfo.isGone = member.btc.isNullOrEmpty()
                && member.github.isNullOrEmpty()
                && member.location.isNullOrEmpty()
                && member.twitter.isNullOrEmpty()
                && member.website.isNullOrEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_member, menu)
        this.mMenu = menu
        if (username == pref.getString(Keys.PREF_USERNAME, "")) {
            menu.findItem(R.id.menu_block).isVisible = false
            menu.findItem(R.id.menu_follow).isVisible = false
            menu.findItem(R.id.menu_report).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_follow -> followOrNot(isFollowed)
            R.id.menu_block -> blockOrNot(isBlocked)
            R.id.menu_report -> reportAbuse()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun reportAbuse() {
        if (!myApp.isLogin) {
            showLoginHint(binding.root)
            return
        }
        if(!this::member.isInitialized) {
            toast("请等待用户信息获取")
            return;
        }

        BottomSheetMenu(this)
            .setTitle("请选择举报的理由")
            .addItems(listOf("大量发布广告", "冒充他人", "疑似机器帐号", "儿童安全", "其他")) { _, s ->
                startActivity(Intent(this, NewTopicActivity::class.java).apply {
                    action = Keys.ACTION_V2EX_REPORT
                    putExtra(Intent.EXTRA_TITLE, "报告用户 ${member.username} ")
                    putExtra(Intent.EXTRA_TEXT, "用户首页：https://www.v2ex.com/member/$username \n 该用户涉及 $s，请站长请处理")
                })
            }
            .show()


    }

    private fun followOrNot(isFollowed: Boolean) {
        if (!myApp.isLogin) {
            showLoginHint(binding.root)
            return
        }
        vCall("${NetManager.HTTPS_V2EX_BASE}/${if (isFollowed) "un" else ""}$followOfOnce")
            .start(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    dealError(this@MemberActivity)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 302) {
                        getByHtml()
                        runOnUiThread {
                            toast("${if (isFollowed) "取消" else ""}关注成功")
                        }
                    }
                }
            })
    }

    private fun blockOrNot(isBlocked: Boolean) {
        if (!myApp.isLogin) {
            showLoginHint(binding.root)
            return
        }
        vCall("$HTTPS_V2EX_BASE/${if (isBlocked) "un" else ""}$blockOfT")
            .start(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    dealError(this@MemberActivity)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 302) {
                        getByHtml()
                        runOnUiThread {
                            toast(if (isBlocked) "你已取消屏蔽该用户" else "屏蔽成功，你将无法看到该用户的帖子和评论")
                        }
                    }
                }
            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        username = getName(intent)
        getData()
    }

    companion object {
        var TAG: String = MemberActivity::class.java.simpleName
        val titles = arrayOf("主题", "回复")
        private fun isFollowed(html: String) = Regex("un(?=follow/\\d{1,8}\\?once=)").containsMatchIn(html)

        private fun getOnceInFollow(html: String): String? = Regex("follow/\\d{1,8}\\?once=\\d{1,10}").find(html)?.value

        private fun isBlock(html: String) = Regex("un(?=block/\\d{1,8}\\?once=)").containsMatchIn(html)

        private fun getOnceInBlock(html: String): String? = Regex("block/\\d{1,8}\\?once=\\d{1,20}").find(html)?.value

        private fun isOnline(html: String) = Regex("class=\"online\"").containsMatchIn(html)
    }

    inner class MemberViewpagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        lateinit var username: String

        lateinit var avatar: String
        override fun getItemCount() = titles.size

        override fun createFragment(position: Int) = when (position) {
            0 -> TopicsFragment()
            else -> UserReplyFragment()
        }.apply { arguments = bundleOf(Keys.KEY_USERNAME to username, Keys.KEY_AVATAR to avatar) }

    }
}
