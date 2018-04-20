@file:Suppress("DEPRECATION")

package im.fdx.v2ex.ui.main

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.MyJobSchedule
import im.fdx.v2ex.R
import im.fdx.v2ex.network.NetManager.DAILY_CHECK
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.vCall
import im.fdx.v2ex.ui.LoginActivity
import im.fdx.v2ex.ui.NotificationActivity
import im.fdx.v2ex.ui.SettingsActivity
import im.fdx.v2ex.ui.WebViewActivity
import im.fdx.v2ex.ui.favor.FavorActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.JOB_ID_GET_NOTIFICATION
import im.fdx.v2ex.utils.extensions.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.email
import org.jetbrains.anko.share
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mDrawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mViewPager: ViewPager
    private lateinit var fab: FloatingActionButton

    private var mAdapter: MyViewPagerAdapter? = null
    private val shortcutId = "create_topic"
    private var shortcutManager: ShortcutManager? = null
    private val shortcutIds = listOf("create_topic")
    private var createTopicInfo: ShortcutInfo? = null
    private var isGetNotification: Boolean = false

    private var count: Int = -1
    val LOG_IN = 0


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            logd("getAction: " + action)
            when (action) {
                Keys.ACTION_LOGIN -> {
                    showIcon(true)
                    val username = intent.getStringExtra(Keys.KEY_USERNAME)
                    val avatar = intent.getStringExtra(Keys.KEY_AVATAR)
                    setUserInfo(username, avatar)
                    fab.show()
                    mAdapter?.initFragment()
                    mAdapter?.notifyDataSetChanged()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        shortcutManager?.addDynamicShortcuts(listOfNotNull<ShortcutInfo>(createTopicInfo))
                    }
                }
                Keys.ACTION_LOGOUT -> {
                    showIcon(false)
                    setUserInfo(null, null)
                    fab.hide()
                    mAdapter?.initFragment()
                    mAdapter?.notifyDataSetChanged()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        shortcutManager?.removeDynamicShortcuts(shortcutIds)
                    }
                }
                Keys.ACTION_GET_NOTIFICATION -> {

                    count = intent.getIntExtra(Keys.KEY_UNREAD_COUNT, -1)
                    isGetNotification = true
                    invalidateOptionsMenu()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav_drawer)
        logd("onCreate")

        val intentFilter = IntentFilter().apply {
            addAction(Keys.ACTION_LOGIN)
            addAction(Keys.ACTION_LOGOUT)
            addAction(Keys.ACTION_GET_NOTIFICATION)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        val mToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        createShortCut()


        mDrawer = findViewById(R.id.drawer_layout)
        val mDrawToggle = ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mDrawer.addDrawerListener(mDrawToggle)
        mDrawToggle.syncState()

        mDrawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                val menu = navigationView.menu
                (0 until menu.size()).forEach { j -> menu.getItem(j).isChecked = false }
            }
        })

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        fab = findViewById(R.id.fab_main)
        fab.setOnClickListener { startActivity(Intent(this@MainActivity, NewTopicActivity::class.java)) }
        val ivMode = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.iv_night_mode)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        ivMode.setOnClickListener {
            if (sharedPreferences.getBoolean("NIGHT_MODE", false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit().putBoolean("NIGHT_MODE", false).apply()
                recreate()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences.edit().putBoolean("NIGHT_MODE", true).apply()
                recreate()
            }
        }

        if (MyApp.get().isLogin()) {
            showIcon(true)
            val username = sharedPreferences.getString(Keys.KEY_USERNAME, "")
            val avatar = sharedPreferences.getString(Keys.KEY_AVATAR, "")
            logd("$username//// $avatar")
            if (!username.isNullOrEmpty() && !avatar.isNullOrEmpty()) {
                setUserInfo(username, avatar)
            }
        } else {
            showIcon(false)
            fab.hide()
        }

        mViewPager = findViewById(R.id.viewpager_main)
        mAdapter = MyViewPagerAdapter(fragmentManager, this@MainActivity)
        mViewPager.adapter = mAdapter

        val mTabLayout: TabLayout = findViewById(R.id.sliding_tabs)


        //这句话可以省略，主要用于如果在其他地方对tablayout自定义title的话，
        // 忽略自定义，只从pageAdapter中获取title
        //        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        //内部实现就是加入一堆的listener给viewpager，不用自己实现
        mTabLayout.setupWithViewPager(mViewPager)

        mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                mAdapter?.getItem(tab.position)?.scrollToTop()
            }
        })

        startGetNotification()
    }

    private fun createShortCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            val intent = Intent(this, NewTopicActivity::class.java)
            intent.action = "android.intent.action.MAIN"
            createTopicInfo = ShortcutInfo.Builder(this, shortcutId)
                    .setActivity(componentName)
                    .setShortLabel(getString(R.string.create_topic))
                    .setLongLabel(getString(R.string.create_topic))
                    .setIntent(intent)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_create))
                    .build()
            when {
                MyApp.get().isLogin() -> shortcutManager?.addDynamicShortcuts(listOfNotNull(createTopicInfo))
                else -> shortcutManager?.removeDynamicShortcuts(shortcutIds)
            }
        }
    }

    private fun startGetNotification() {
        if (MyApp.get().isLogin() && isOpenMessage) {
            val mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val builder = JobInfo.Builder(JOB_ID_GET_NOTIFICATION,
                    ComponentName(MyApp.get().packageName, MyJobSchedule::class.java.name))
            val timeSec = sharedPreferences.getString("pref_msg_period", "30").toInt()
            builder.setPeriodic((timeSec * 1000).toLong())
            mJobScheduler.schedule(builder.build())
        }
    }

    private fun stopGetNotification() {
        if (MyApp.get().isLogin() && isOpenMessage && !isBackground) {
            val mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            mJobScheduler.cancel(JOB_ID_GET_NOTIFICATION)
        }
    }

    private fun showIcon(visible: Boolean) {
        navigationView.menu.findItem(R.id.nav_daily).isVisible = visible
        navigationView.menu.findItem(R.id.nav_favor).isVisible = visible
        invalidateOptionsMenu()
    }

    @Deprecated("好像很费cpu")
    private fun shrinkFab() {
        fab.animate().rotation(360f)
                .setDuration(1000).start()
    }


    private fun setUserInfo(username: String?, avatar: String?) {
        val tvMyName: TextView = navigationView.getHeaderView(0).findViewById(R.id.tv_my_username)
        val ivMyAvatar: CircleImageView = navigationView.getHeaderView(0).findViewById(R.id.iv_my_avatar)
        tvMyName.text = username
        ivMyAvatar.load(avatar)
        ivMyAvatar.setOnClickListener {
            username?.let { startActivity<MemberActivity>(Keys.KEY_USERNAME to it) }
        }


    }

    override fun onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        if (MyApp.get().isLogin()) {
            menu.findItem(R.id.menu_login).isVisible = false
            menu.findItem(R.id.menu_notification).isVisible = true
        } else {
            menu.findItem(R.id.menu_login).isVisible = true
            menu.findItem(R.id.menu_notification).isVisible = false
        }

        if (isGetNotification) {
            menu.findItem(R.id.menu_notification).icon = resources.getDrawable(R.drawable.ic_notification_with_red_point, theme)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> startActivityForResult(Intent(this@MainActivity, LoginActivity::class.java), LOG_IN)
            R.id.menu_notification -> {
                item.icon = resources.getDrawable(R.drawable.ic_notifications_white_24dp, theme)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(Keys.notifyID)
                val intent = Intent(this, NotificationActivity::class.java)
                when {
                    count != -1 -> intent.putExtra(Keys.KEY_UNREAD_COUNT, count)
                }
                startActivity(intent)
                count = -1
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        @Suppress("DEPRECATION")
        when (item.itemId) {
            R.id.nav_daily -> dailyCheck()
            R.id.nav_node -> startActivity<AllNodesActivity>()
            R.id.nav_favor -> startActivity<FavorActivity>()
            R.id.nav_change_daylight -> startActivity<WebViewActivity>()
            R.id.nav_testNotify -> {
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 110)
            }
            R.id.nav_share -> share("https://play.google.com/store/apps/details?id=$packageName")
            R.id.nav_feedback -> email(Keys.AUTHOR_EMAIL, getString(R.string.feedback_subject), getString(R.string.feedback_hint))
            R.id.nav_setting -> startActivity<SettingsActivity>()
        }
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }


    private fun dailyCheck() {
        vCall(DAILY_CHECK).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "daily mission failed")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() == 302) {
                    runOnUiThread { toast("还未登录，请先登录") }
                    return
                }

                val body = response.body()!!.string()

                if (body.contains("每日登录奖励已领取")) {
                    logi("已领取")
                    runOnUiThread { toast("每日登录奖励已领取") }
                    return
                }

                val once = parseDailyOnce(body)

                if (once == null) {
                    loge("null once")
                    return
                }
                postDailyCheck(once)
            }
        })
    }

    private fun postDailyCheck(once: String) {
        vCall("$HTTPS_V2EX_BASE/mission/daily/redeem?once=$once")
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("MainActivity", "daily mission failed")
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        Log.w("MainActivity", "daily check ok")
                        runOnUiThread { toast("每日登录奖励领取成功") }
                    }
                })
    }

    private fun parseDailyOnce(string: String): String? {

        val body = Jsoup.parse(string).body()
        val onceElement = body.getElementsByAttributeValue("value", "领取 X 铜币").first()
                ?: return null
//        location.href = '/mission/daily/redeem?once=83270';
        val onceOriginal = onceElement.attr("onClick")
        return onceOriginal.getNum()
    }

    override fun onResume() {
        super.onResume()
        logd("onResume")
    }

    override fun onPause() {
        super.onPause()
        logd("onPause")
    }

    override fun onRestart() {
        super.onRestart()
        logd("onRestart")
    }

    override fun onStop() {
        super.onStop()
        logd("onStop")

    }

    override fun onDestroy() {
        super.onDestroy()
        logd("onDestroy")
        stopGetNotification()
        mViewPager.clearOnPageChangeListeners()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private val isBackground: Boolean
        get() = sharedPreferences.getBoolean("pref_background_msg", false)

    private val isOpenMessage: Boolean
        get() = sharedPreferences.getBoolean("pref_msg", true)
}

