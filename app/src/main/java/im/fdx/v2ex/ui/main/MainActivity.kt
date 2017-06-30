package im.fdx.v2ex.ui.main

import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
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
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.R.id.nav_testNotify
import im.fdx.v2ex.UpdateService
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager.DAILY_CHECK
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.ui.*
import im.fdx.v2ex.ui.favor.FavorActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.t
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    internal lateinit var mDrawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mViewPager: ViewPager
    private lateinit var fab: FloatingActionButton

    private var mAdapter: MyViewPagerAdapter? = null
    private val shortcutId = "create_topic"
    private var vitent: Intent? = null
    private var shortcutManager: ShortcutManager? = null
    private val shortcutIds = listOf("create_topic")
    private var createTopicInfo: ShortcutInfo? = null
    private var isGetNotification: Boolean = false

    private var count: Int = -1


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            XLog.tag(TAG).d("getAction: " + action)
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
                    removeUserInfo()
                    fab.hide()
                    mAdapter?.initFragment()
                    mAdapter?.notifyDataSetChanged()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        shortcutManager?.removeDynamicShortcuts(shortcutIds)
                    }
                }
                Keys.ACTION_GET_NOTIFICATION -> {

                    count = intent.getIntExtra(Keys.KEY_COUNT, -1)
                    isGetNotification = true
                    invalidateOptionsMenu()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav_drawer)
        XLog.tag(TAG).d("onCreate")

        val intentFilter = IntentFilter()
        intentFilter.addAction(Keys.ACTION_LOGIN)
        intentFilter.addAction(Keys.ACTION_LOGOUT)
        intentFilter.addAction(Keys.ACTION_GET_NOTIFICATION)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        val mToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
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
            if (MyApp.get().isLogin()) {
                shortcutManager?.addDynamicShortcuts(listOfNotNull<ShortcutInfo>(createTopicInfo))

            } else {
                shortcutManager?.removeDynamicShortcuts(shortcutIds)
            }
        }


        mDrawer = findViewById(R.id.drawer_layout)
        val mDrawToggle = ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mDrawer.addDrawerListener(mDrawToggle)
        mDrawToggle.syncState()

        mDrawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View?) {
                val menu = navigationView.menu
                (0..menu.size() - 1).forEach { j -> menu.getItem(j).isChecked = false }
            }
        })

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        fab = findViewById(R.id.fab_main)
        fab.setOnClickListener { startActivity(Intent(this@MainActivity, NewTopicActivity::class.java)) }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (MyApp.get().isLogin()) {
            showIcon(true)

            val username = sharedPreferences.getString(Keys.KEY_USERNAME, "")
            val avatar = sharedPreferences.getString(Keys.KEY_AVATAR, "")
            XLog.tag(TAG).d(username + "//// " + avatar)
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(avatar)) {
                setUserInfo(username, avatar)
            }

            shrinkFab()

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
                mAdapter!!.getItem(tab.position).scrollToTop()
            }
        })

        if (!BuildConfig.DEBUG) {
            navigationView.menu.removeItem(nav_testNotify)
            navigationView.menu.removeItem(R.id.nav_testMenu2)
        }

        vitent = Intent(this@MainActivity, UpdateService::class.java)
        vitent!!.action = Keys.ACTION_START_NOTIFICATION
        if (MyApp.get().isLogin() && isOpenMessage) {
            startService(vitent)
        }

    }

    private fun showIcon(visible: Boolean) {
        navigationView.menu.findItem(R.id.nav_daily).isVisible = visible
        navigationView.menu.findItem(R.id.nav_favor).isVisible = visible
        this@MainActivity.invalidateOptionsMenu()
    }


    private fun shrinkFab() {
        fab.animate().rotation(360f)
                .setDuration(1000).start()
    }


    private fun setUserInfo(username: String, avatar: String) {
        val tvMyName: TextView = navigationView.getHeaderView(0).findViewById(R.id.tv_my_username)
        tvMyName.text = username
        val ivMyAvatar: CircleImageView = navigationView.getHeaderView(0).findViewById(R.id.iv_my_avatar)
        ivMyAvatar.setOnClickListener {
            val intent = Intent(this@MainActivity, MemberActivity::class.java)
            intent.putExtra(Keys.KEY_USERNAME, username)
            startActivity(intent)
        }

        Picasso.with(this@MainActivity).load(avatar).into(ivMyAvatar)

    }

    private fun removeUserInfo() {
        val tvMyName: TextView = findViewById(R.id.tv_my_username)
        tvMyName.text = ""
        val imageView: CircleImageView = findViewById(R.id.iv_my_avatar)
        imageView.setImageDrawable(null)
        imageView.visibility = View.INVISIBLE

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
                val intent = Intent(this, NotificationActivity::class.java)
                when {
                    count != -1 -> intent.putExtra(Keys.KEY_COUNT, count)
                }
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_daily -> dailyCheck()
            R.id.nav_node -> startActivity(Intent(this, AllNodesActivity::class.java))
            R.id.nav_favor -> {
                val intentFavor = Intent(this, FavorActivity::class.java)
                startActivity(intentFavor)
            }
            R.id.nav_testMenu2 -> startActivity(Intent(this, WebViewActivity::class.java))
            R.id.nav_testNotify -> {
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 110)
            }
            R.id.nav_share -> {
                val intentShare = Intent(Intent.ACTION_SEND)
                intentShare.type = "text/plain"
                intentShare.putExtra(Intent.EXTRA_TEXT, "V2ex:" + "market://details?id=" + packageName)
                startActivity(Intent.createChooser(intentShare, getString(R.string.share_to)))
            }
            R.id.nav_feedback -> {
                val intentData = Intent(Intent.ACTION_SEND)

                intentData.type = "message/rfc822"
                intentData.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(Keys.AUTHOR_EMAIL))
                intentData.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
                intentData.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_hint) + "\n")
                try {
                    intentData.`package` = "com.google.android.apps.inbox"
                    startActivity(intentData)
                } catch (ex: ActivityNotFoundException) {
                    intentData.`package` = null
                    intentData.data = Uri.parse("mailto:${Keys.AUTHOR_EMAIL}")
                    startActivity(intentData)
                    //                    Toast.makeText(MainActivity.this, "There are no email clients installed.",
                    //                            Toast.LENGTH_SHORT).show();
                }

            }
            R.id.nav_setting -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//        when (requestCode) {
//            110 ->
//                if (resultCode == RESULT_OK) {
//                    try {
//                        val imageUri = imageReturnedIntent?.data
//                        val imageStream = contentResolver.openInputStream(imageUri);
//                        val bitmap = BitmapFactory.decodeStream(imageStream)
//
//                        val stream = ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                        val byteArray = stream.toByteArray();
//
//                        val url = "https://sm.ms/api/upload/"
//
//                        Thread(Runnable {
//                            HttpHelper.OK_CLIENT.newCall(Request.Builder().url("https://sm.ms").get().build()).execute()
//                            val pp: RequestBody = MultipartBody.create(MediaType.parse("multipart/jpeg"), byteArray)
//                            val body: RequestBody = MultipartBody.Builder()
//                                    .addFormDataPart("smfile", "nonono.png", pp).build()
//                            HttpHelper.OK_CLIENT.newCall(Request.Builder()
//                                    //                                .headers(HttpHelper.baseHeaders)
//                                    //                                .header("Host", "sm.ms")
//                                    //                                .header("Refer","http://sm.ms/")
//                                    .url(url)
//                                    .post(body)
//                                    .build()).enqueue(object : Callback {
//                                override fun onFailure(call: Call?, e: IOException?) {
//                                    e?.printStackTrace()
//                                }
//
//                                override fun onResponse(call: Call?, response: Response?) {
//                                    XLog.tag("smms").d(response?.body()?.string())
//                                }
//                            })
//                        }).start()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//        }
//    }


    private fun dailyCheck() {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(DAILY_CHECK).get().build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "daily mission failed")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() == 302) {
                    runOnUiThread { t("还未登录，请先登录") }
                    return
                }

                val body = response.body()!!.string()

                if (body.contains("每日登录奖励已领取")) {
                    XLog.tag("MainActivity").w("已领取")
                    runOnUiThread { t("已领取，明天再来") }
                    return
                }

                val once = parseDailyOnce(body)

                if (once == null) {
                    XLog.tag(TAG).e("null once")
                    return
                }
                postDailyCheck(once)
            }
        })
    }

    private fun postDailyCheck(once: String) {
        HttpHelper.OK_CLIENT.newCall(Request.Builder().headers(HttpHelper.baseHeaders)
                .url(HTTPS_V2EX_BASE + "/mission/daily/redeem?once=" + once)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "daily mission failed")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.w("MainActivity", "daily check ok")
                runOnUiThread { t("领取成功") }
            }
        })
    }

    private fun parseDailyOnce(string: String): String? {

        val body = Jsoup.parse(string).body()
        val onceElement = body.getElementsByAttributeValue("value", "领取 X 铜币").first() ?: return null
//        location.href = '/mission/daily/redeem?once=83270';
        val onceOriginal = onceElement.attr("onClick")
        return TimeUtil.getNum(onceOriginal)
    }

    override fun onResume() {
        super.onResume()
        XLog.tag(TAG).d("onResume")
    }

    override fun onPause() {
        super.onPause()
        XLog.tag(TAG).d("onPause")
    }

    override fun onRestart() {
        super.onRestart()
        XLog.tag(TAG).d("onRestart")
    }

    override fun onStop() {
        super.onStop()
        XLog.tag(TAG).d("onStop")

    }

    override fun onDestroy() {
        super.onDestroy()
        XLog.tag(TAG).d("onDestroy")
        if (MyApp.get().isLogin() && isOpenMessage && !isBackground) {
            stopService(intent)
        }
        mViewPager.clearOnPageChangeListeners()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private val isBackground: Boolean
        get() = sharedPreferences.getBoolean("pref_background_msg", false)

    private val isOpenMessage: Boolean
        get() = sharedPreferences.getBoolean("pref_msg", true)

    companion object {

        private val TAG = "MainActivity"
        private val LOG_IN = 0
    }

}

