package im.fdx.v2ex.ui.member

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ActivityMemberBinding
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.API_TOPIC
import im.fdx.v2ex.network.NetManager.API_USER
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.NetManager.myGson
import im.fdx.v2ex.network.start
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.pref
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.logi
import im.fdx.v2ex.utils.extensions.setUpToolbar
import im.fdx.v2ex.view.CustomChrome
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.toast
import java.io.IOException


/**
 * 获取user的主题，依然使用api的方式
 */
class MemberActivity : BaseActivity() {

    private lateinit var mMenu: Menu

    private lateinit var member: Member

    private var username: String? = null
    private var urlTopic: String? = null
    private var blockOfT: String? = null
    private var followOfOnce: String? = null
    private var isBlocked: Boolean = false
    private var isFollowed: Boolean = false
    private lateinit var binding: ActivityMemberBinding


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
        }

        setUpToolbar()

        username = getName(intent)

        if(username.isNullOrEmpty()) {
            toast("未知问题，无法访问用户信息")
            return
        }
        binding.tlMember.setupWithViewPager(binding.viewpager)

        binding.alProfile.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1, verticalOffset ->

            val maxScroll = appBarLayout1.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()
            when (percentage) {
                in 0f..1f -> binding.constraintMember.alpha = 1 - percentage
            }
        })
        getData()
    }

    private fun getName(intent: Intent): String? = when {
        intent.data != null -> intent.data!!.pathSegments[1]
        intent.extras != null -> intent.extras!!.getString(Keys.KEY_USERNAME)
        BuildConfig.DEBUG -> "Livid"
        else -> null
    }

    private fun getData() {
        val urlUserInfo = "$API_USER?username=$username"  //Livid's profile
        binding.ctlProfile.title = username
        urlTopic = "$API_TOPIC?username=$username"
        Log.i(TAG, "$urlUserInfo: \t$urlTopic")
        getUserInfoAPI(urlUserInfo)
        getBlockAndFollowWeb()
    }

    private fun getBlockAndFollowWeb() {
        if (username == pref.getString(Keys.PREF_USERNAME, "")) {
            return
        }
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
                    logd("isBlocked: $isBlocked|isFollowed: $isFollowed")


                    runOnUiThread {
                        if (isBlocked) {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp)
                        } else {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp)
                        }

                        if (isFollowed) {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_blue_24dp)
                        } else {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp)
                        }

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

    private fun getUserInfoAPI(urlUserInfo: String) {
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
                    logi("body:" +body)
                    member = myGson.fromJson(body, Member::class.java)
                    runOnUiThread { showUser() }
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
                    val popupWindow = PopupWindow(contentView,WRAP_CONTENT,WRAP_CONTENT)
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
                !(member.website).isNullOrEmpty() ->
                    CustomChrome(this).load(if (!member.website!!.contains("http")) "http://"
                            + member.website else member.website!!)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showUser() {
        if(this.isDestroyed) return

        binding.viewpager.adapter = MemberViewpagerAdapter(supportFragmentManager).apply {
            username = member.username
            avatar = member.avatar_normal
        }
        binding.ivAvatarProfile.load(member.avatarLargeUrl)
        binding.tvTagline.text = member.tagline
        binding.tvIntro.text = member.bio
        binding.tvPrefixCreated.text = "加入于${TimeUtil.getAbsoluteTime((member.created))},${getString(R.string.the_n_member, member.id)}"

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
        if (username == pref.getString(Keys.KEY_USERNAME, "")) {
            menu.findItem(R.id.menu_block).isVisible = false
            menu.findItem(R.id.menu_follow).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_follow -> followOrNot(isFollowed)
            R.id.menu_block -> blockOrNot(isBlocked)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun followOrNot(isFollowed: Boolean) {
        vCall("${NetManager.HTTPS_V2EX_BASE}/${if (isFollowed) "un" else ""}$followOfOnce")
                .start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@MemberActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 302) {
                    getBlockAndFollowWeb()
                    runOnUiThread {
                        toast("${if (isFollowed) "取消" else ""}关注成功")
                    }
                }
            }
        })
    }

    private fun blockOrNot(isBlocked: Boolean) {
        vCall("$HTTPS_V2EX_BASE/${if (isBlocked) "un" else ""}$blockOfT")
                .start(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        dealError(this@MemberActivity)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code == 302) {
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
        username = getName(intent)
        getData()
    }

    companion object {
        var TAG: String = MemberActivity::class.java.simpleName

        private fun isFollowed(html: String) = Regex("un(?=follow/\\d{1,8}\\?once=)").containsMatchIn(html)

        private fun getOnceInFollow(html: String): String? = Regex("follow/\\d{1,8}\\?once=\\d{1,10}").find(html)?.value

        private fun isBlock(html: String) = Regex("un(?=block/\\d{1,8}\\?t=)").containsMatchIn(html)

        private fun getOnceInBlock(html: String): String? = Regex("block/\\d{1,8}\\?t=\\d{1,20}").find(html)?.value
    }

    inner class MemberViewpagerAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        lateinit var username: String

//        private var _avatar = ""
        //目前不好做，先留着
        lateinit var avatar: String
//            get() {
//                return _avatar
//            }
//            set(value) {
//                _avatar = value
//                (getItem(0) as TopicsFragment).updateAvatar(_avatar)
//            }
        private val titles = arrayOf("主题", "评论")

        override fun getItem(position: Int) = when (position) {
            0 -> TopicsFragment()
            else -> UserReplyFragment()
        }.apply { arguments = bundleOf(Keys.KEY_USERNAME to username, Keys.KEY_AVATAR to avatar) }

        override fun getCount() = titles.size
        override fun getPageTitle(position: Int) = titles[position]

    }
}
