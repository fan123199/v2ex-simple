@file:Suppress("DEPRECATION")

package im.fdx.v2ex.ui.main

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.*
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.DAILY_CHECK
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.ui.*
import im.fdx.v2ex.ui.favor.FavorActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.TAG_WORKER
import im.fdx.v2ex.utils.extensions.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.activity_main_nav_drawer.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jetbrains.anko.email
import org.jetbrains.anko.share
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

  private lateinit var mDrawer: DrawerLayout
  private lateinit var mViewPager: ViewPager
  private lateinit var fab: FloatingActionButton

  private lateinit var mAdapter: MyViewPagerAdapter
  private val shortcutId = "create_topic"
  private var shortcutManager: ShortcutManager? = null
  private val shortcutIds = listOf("create_topic")
  private var createTopicInfo: ShortcutInfo? = null
  private var isGetNotification: Boolean = false

  private var count: Int = -1


  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

      val action = intent.action
      logd("getAction: $action")
      when (action) {
        Keys.ACTION_LOGIN -> {
          invalidateOptionsMenu()
          val username = pref.getString(Keys.PREF_USERNAME, "")
          val avatar = pref.getString(Keys.PREF_AVATAR, "")
          setUserInfo(username, avatar)
          reloadTab()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.addDynamicShortcuts(listOfNotNull(createTopicInfo))
          }
        }
        Keys.ACTION_LOGOUT -> {
          invalidateOptionsMenu()
          setUserInfo(null, null)
          reloadTab()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.removeDynamicShortcuts(shortcutIds)
          }
        }
        Keys.ACTION_GET_NOTIFICATION -> {
          count = intent.getIntExtra(Keys.KEY_UNREAD_COUNT, -1)
          isGetNotification = true
          invalidateOptionsMenu()
        }

        Keys.ACTION_TAB_SETTING -> {
          reloadTab()
        }
        Keys.ACTION_TEXT_SIZE_CHANGE -> {
          recreate()
        }
      }
    }
  }

  fun reloadTab() {
    mAdapter.initFragment()
    mAdapter.notifyDataSetChanged()
    mViewPager.adapter = mAdapter
    sliding_tabs.setupWithViewPager(mViewPager)
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logd("onCreate")
    window.statusBarColor = Color.TRANSPARENT
    setContentView(R.layout.activity_main_nav_drawer)
    setSupportActionBar(toolbar)

    val intentFilter = IntentFilter().apply {
      addAction(Keys.ACTION_LOGIN)
      addAction(Keys.ACTION_LOGOUT)
      addAction(Keys.ACTION_GET_NOTIFICATION)
      addAction(Keys.ACTION_TAB_SETTING)
      addAction(Keys.ACTION_TEXT_SIZE_CHANGE)
    }

    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
    createShortCut()

    mDrawer = findViewById(R.id.drawer_layout)
    val mDrawToggle = ActionBarDrawerToggle(this, mDrawer,
        toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    mDrawer.addDrawerListener(mDrawToggle)
    mDrawer.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.status_bar_white))
    mDrawToggle.syncState()

    mDrawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
      override fun onDrawerClosed(drawerView: View) {
        val menu = nav_view.menu
        (0 until menu.size()).forEach { j -> menu.getItem(j).isChecked = false }
      }
    })

    nav_view.setNavigationItemSelectedListener(this)
    fab = findViewById(R.id.fab_main)
    fab.setOnClickListener {
      if(myApp.isLogin) {
        startActivity<NewTopicActivity>()
      } else {
        showLoginHint(it)
      }
    }
    val ivMode = nav_view.getHeaderView(0).findViewById<ImageView>(R.id.iv_night_mode)

    ivMode.setOnClickListener {
      if (pref.getBoolean("NIGHT_MODE", false)) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        pref.edit().putBoolean("NIGHT_MODE", false).apply()
        recreate()
      } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        pref.edit().putBoolean("NIGHT_MODE", true).apply()
        recreate()
      }
    }

    if (myApp.isLogin) {
      val username = pref.getString(Keys.PREF_USERNAME, "")
      val avatar = pref.getString(Keys.PREF_AVATAR, "")
      if (!username.isNullOrEmpty() && !avatar.isNullOrEmpty()) {
        setUserInfo(username, avatar)
      } else {
        updateUserInBackground()
      }
      dailyCheck(true)
    }

    mViewPager = findViewById(R.id.viewpager_main)
    mAdapter = MyViewPagerAdapter(supportFragmentManager, this@MainActivity)
    mViewPager.adapter = mAdapter


    //内部实现就是加入一堆的listener给viewpager，不用自己实现
    sliding_tabs.setupWithViewPager(mViewPager)

    sliding_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab) {}
      override fun onTabUnselected(tab: TabLayout.Tab) {}

      override fun onTabReselected(tab: TabLayout.Tab) {
        mAdapter.getItem(tab.position).scrollToTop()
      }
    })

    startGetNotification()
  }

  private fun updateUserInBackground() {
    vCall(NetManager.HTTPS_V2EX_BASE).start(object : Callback {
      override fun onFailure(call: Call, e: IOException) {

      }

      override fun onResponse(call: Call, response: Response) {
        val body = response.body()?.string()!!
        val myInfo = Parser(body).getMember()

        pref.edit {
          putString(Keys.PREF_USERNAME, myInfo.username)
          putString(Keys.PREF_AVATAR, myInfo.avatarLargeUrl)
        }
        runOnUiThread {
          setUserInfo(myInfo.username, myInfo.avatarLargeUrl)
        }
      }
    })
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
        MyApp.get().isLogin -> shortcutManager?.addDynamicShortcuts(listOf(createTopicInfo))
        else -> shortcutManager?.removeDynamicShortcuts(shortcutIds)
      }
    }
  }

  private fun startGetNotification() {
    if (MyApp.get().isLogin && isOpenMessage) {
      val timeSec = pref.getString("pref_msg_period", "900")!!.toLong()
      val compressionWork = PeriodicWorkRequestBuilder<GetMsgWorker>(timeSec, TimeUnit.SECONDS)
              .addTag(TAG_WORKER)
              .build()
      WorkManager.getInstance().enqueue(compressionWork)

    }
  }

  private fun stopGetNotification() {
    if (myApp.isLogin
            && isOpenMessage
            && !pref.getBoolean("pref_background_msg", false)) {
      WorkManager.getInstance().cancelAllWorkByTag(TAG_WORKER)
    }
  }

  private fun setUserInfo(username: String?, avatar: String?) {
    val tvMyName: TextView = nav_view.getHeaderView(0).findViewById(R.id.tv_my_username)
    val ivMyAvatar: CircleImageView = nav_view.getHeaderView(0).findViewById(R.id.iv_my_avatar)
    tvMyName.text = username
    ivMyAvatar.load(avatar)
    ivMyAvatar.setOnClickListener {
      username?.let { it2 -> startActivity<MemberActivity>(Keys.KEY_USERNAME to it2) }
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

    if (MyApp.get().isLogin) {
      menu.findItem(R.id.menu_login).isVisible = false
      menu.findItem(R.id.menu_notification).isVisible = true
    } else {
      menu.findItem(R.id.menu_login).isVisible = true
      menu.findItem(R.id.menu_notification).isVisible = false
    }

    if (isGetNotification) {
      menu.findItem(R.id.menu_notification).icon =
          ContextCompat.getDrawable(this, R.drawable.ic_notification_with_red_point)
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_login -> startActivity<LoginActivity>()
      R.id.menu_notification -> {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_notifications_primary_24dp)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(Keys.notifyID)
        val intent = Intent(this, NotificationActivity::class.java)
        when {
          count != -1 -> intent.putExtra(Keys.KEY_UNREAD_COUNT, count)
        }
        startActivity(intent)
        count = -1
      }
      R.id.menu_search -> startActivity<SearchActivity>()
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    @Suppress("DEPRECATION")
    when (item.itemId) {
      R.id.nav_daily ->
        if(myApp.isLogin) {
          dailyCheck(autoCheck = false)
        } else {
          showLoginHint(fab)
        }
      R.id.nav_node ->
        startActivity<AllNodesActivity>()
      R.id.nav_favor ->
        if(myApp.isLogin) {
          startActivity<FavorActivity>()
        } else {
          showLoginHint(fab)
        }
      R.id.nav_change_daylight -> startActivity<WebViewActivity>()
      R.id.nav_testNotify -> {}
      R.id.nav_share -> share("https://play.google.com/store/apps/details?id=$packageName")
      R.id.nav_feedback -> email(Keys.AUTHOR_EMAIL, getString(R.string.feedback_subject), getString(R.string.feedback_hint))
      R.id.nav_setting -> startActivity<SettingsActivity>()
    }
    mDrawer.closeDrawer(GravityCompat.START)
    return true
  }


  private fun dailyCheck(autoCheck : Boolean ) {
    vCall(DAILY_CHECK).start(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        Log.e("MainActivity", "daily mission failed")
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {

        if (response.code() == 302) {
          runOnUiThread {
            setLogin(false)
            toast("登录信息失效，请先登录") }
          return
        }

        val body = response.body()!!.string()

        if (body.contains("每日登录奖励已领取")) {
          logi("已领取")
          if(!autoCheck) {
            runOnUiThread { toast("已领取, 明日再来") }
          }
          return
        }

        val once = Parser(body).parseDailyOnce()

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
            loge("daily mission failed")
          }

          @Throws(IOException::class)
          override fun onResponse(call: Call, response: Response) {
            logw("daily check ok")
            runOnUiThread { toast("每日登录奖励领取成功") }
          }
        })
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

  private val isOpenMessage: Boolean
    get() = pref.getBoolean("pref_msg", true)
}

