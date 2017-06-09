package im.fdx.v2ex.ui.main;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import im.fdx.v2ex.BuildConfig;
import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.UpdateService;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.ui.LoginActivity;
import im.fdx.v2ex.ui.MemberActivity;
import im.fdx.v2ex.ui.NotificationActivity;
import im.fdx.v2ex.ui.SettingsActivity;
import im.fdx.v2ex.ui.WebViewActivity;
import im.fdx.v2ex.ui.favor.FavorActivity;
import im.fdx.v2ex.ui.node.AllNodesActivity;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static im.fdx.v2ex.R.id.nav_testNotify;
import static im.fdx.v2ex.network.NetManager.DAILY_CHECK;
import static im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE;
import static im.fdx.v2ex.utils.Keys.ACTION_GET_NOTIFICATION;
import static im.fdx.v2ex.utils.Keys.ACTION_LOGIN;
import static im.fdx.v2ex.utils.Keys.ACTION_LOGOUT;
import static im.fdx.v2ex.utils.Keys.KEY_AVATAR;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int LOG_IN_SUCCEED = 1;
    private static final int LOG_IN = 0;
    private static final int ID_ITEM_CHECK = 33;
    DrawerLayout mDrawer;

    private ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;
    private NavigationView navigationView;
    private final String shortcutId = "create_topic";
    private Intent intent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            XLog.tag(TAG).d("getAction: " + action);
            if (action.equals(ACTION_LOGIN)) {
                showIcon(true);
                String username = intent.getStringExtra(Keys.KEY_USERNAME);
                String avatar = intent.getStringExtra(KEY_AVATAR);
                setUserInfo(username, avatar);
                fab.show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcutManager.addDynamicShortcuts(Collections.singletonList(createTopicInfo));
                }
            } else if (action.equals(ACTION_LOGOUT)) {
                showIcon(false);
                removeUserInfo();
                fab.hide();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcutManager.removeDynamicShortcuts(shortcutIds);
                }
            } else if (action.equals(ACTION_GET_NOTIFICATION)) {
                isGetNotification = true;
                invalidateOptionsMenu();
            }
        }
    };
    private ViewPager.OnPageChangeListener listener;
    private FloatingActionButton fab;
    private ShortcutManager shortcutManager;
    private List<String> shortcutIds = Collections.singletonList("create_topic");
    private ShortcutInfo createTopicInfo;
    private SharedPreferences sharedPreferences;
    private boolean isGetNotification;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav_drawer);
        XLog.tag(TAG).d("onCreate");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOGIN);
        intentFilter.addAction(ACTION_LOGOUT);
        intentFilter.addAction(ACTION_GET_NOTIFICATION);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            shortcutManager = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            Intent intent = new Intent(this, NewTopicActivity.class);
            intent.setAction("android.intent.action.MAIN");
            createTopicInfo = new ShortcutInfo.Builder(this, shortcutId)
                    .setActivity(getComponentName())
                    .setShortLabel(getString(R.string.create_topic))
                    .setLongLabel(getString(R.string.create_topic))
                    .setIntent(intent)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_create))
                    .build();
            if (MyApp.Companion.get().isLogin()) {
                shortcutManager.addDynamicShortcuts(Collections.singletonList(createTopicInfo));

            } else {
                shortcutManager.removeDynamicShortcuts(shortcutIds);
            }
        }


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawToggle = new ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mDrawToggle);
        mDrawToggle.syncState();

        mDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                Menu menu = navigationView.getMenu();
                for (int j = 0; j < menu.size(); j++) {
                    menu.getItem(j).setChecked(false);
                }
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NewTopicActivity.class)));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (MyApp.Companion.get().isLogin()) {
            showIcon(true);

            String username = sharedPreferences.getString(Keys.KEY_USERNAME, "");
            String avatar = sharedPreferences.getString(KEY_AVATAR, "");
            XLog.tag(TAG).d(username + "//// " + avatar);
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(avatar)) {
                setUserInfo(username, avatar);
            }

            shrinkFab();

        } else {

            showIcon(false);
            fab.hide();
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager_main);
        mAdapter = new MyViewPagerAdapter(getFragmentManager(), MainActivity.this);
        mViewPager.setAdapter(mAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);


        //这句话可以省略，主要用于如果在其他地方对tablayout自定义title的话，
        // 忽略自定义，只从pageAdapter中获取title
//        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        if (mTabLayout != null) {
            //内部实现就是加入一堆的listener给viewpager，不用自己实现
            mTabLayout.setupWithViewPager(mViewPager);

            mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    ((TopicsFragment) mAdapter.getItem(tab.getPosition())).scrollToTop();
                }
            });

        }


        if (!BuildConfig.DEBUG) {
            navigationView.getMenu().removeItem(nav_testNotify);
            navigationView.getMenu().removeItem(R.id.nav_testMenu2);
        }

        intent = new Intent(MainActivity.this, UpdateService.class);
        intent.setAction(Keys.ACTION_START_NOTIFICATION);
        if (MyApp.Companion.get().isLogin() && isOpenMessage()) {
            startService(intent);
        }

    }

    public static
    @ColorInt
    int adjustColorForStatusBar(@ColorInt int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);

        // darken the color by 7.5%
        float lightness = hsl[2] * 0.925f;
        // constrain lightness to be within [0–1]
        lightness = Math.max(0f, Math.min(1f, lightness));
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    private void showIcon(boolean visible) {
        navigationView.getMenu().findItem(R.id.nav_daily).setVisible(visible);
        navigationView.getMenu().findItem(R.id.nav_favor).setVisible(visible);
        MainActivity.this.invalidateOptionsMenu();
    }


    private void shrinkFab() {
        fab.animate().rotation(360f)
                .setDuration(1000).start();
    }


    private void setUserInfo(final String username, String avatar) {
        TextView tvMyName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_my_username);
        tvMyName.setText(username);
        CircleImageView ivMyAvatar = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.iv_my_avatar);
//        ivMyAvatar.setImageUrl(avatar, VolleyHelper.Companion.get()().getImageLoader());
        ivMyAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MemberActivity.class);
            intent.putExtra(Keys.KEY_USERNAME, username);
            startActivity(intent);
        });

        Picasso.with(MainActivity.this).load(avatar).into(ivMyAvatar);

    }

    private void removeUserInfo() {
        TextView tvMyName = (TextView) findViewById(R.id.tv_my_username);
        tvMyName.setText("");
        CircleImageView imageView = (CircleImageView) findViewById(R.id.iv_my_avatar);
        imageView.setImageDrawable(null);
        imageView.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (MyApp.Companion.get().isLogin()) {
            menu.findItem(R.id.menu_login).setVisible(false);
            menu.findItem(R.id.menu_notification).setVisible(true);
//            XLog.tag(TAG).d("invisible");
        } else {
            menu.findItem(R.id.menu_login).setVisible(true);
            menu.findItem(R.id.menu_notification).setVisible(false);
//            XLog.tag(TAG).d("visible");
        }

        if (isGetNotification) {
            menu.findItem(R.id.menu_notification).setIcon(getResources().getDrawable(R.drawable.ic_notification_with_red_point));
        }
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOG_IN);
                break;
            case R.id.menu_notification:
                item.setIcon(getResources().getDrawable(R.drawable.ic_notifications_white_24dp));
                startActivity(new Intent(this, NotificationActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_daily:
                dailyCheck();
                break;
            case R.id.nav_node:
                startActivity(new Intent(this, AllNodesActivity.class));
                break;
//            case R.id.nav_notification:
//                break;

            case R.id.nav_favor:
                Intent intentFavor = new Intent(this, FavorActivity.class);
                startActivity(intentFavor);
                break;
            case R.id.nav_testMenu2:

                startActivity(new Intent(this, WebViewActivity.class));
                break;
            case R.id.nav_testNotify:
//                UpdateService.putNotification(this, 199);
                break;
            case R.id.nav_share:
//                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_TEXT, "V2ex:" + "market://details?id=" + getPackageName());
                startActivity(Intent.createChooser(intentShare, getString(R.string.share_to)));
                break;
            case R.id.nav_feedback:


                Intent intentData = new Intent(Intent.ACTION_SEND);

                intentData.setType("message/rfc822");
                intentData.putExtra(Intent.EXTRA_EMAIL, new String[]{Keys.AUTHOR_EMAIL});
                intentData.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                intentData.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_hint) + "\n");
                try {
                    intentData.setPackage("com.google.android.apps.inbox");
                    startActivity(intentData);
                } catch (ActivityNotFoundException ex) {
                    intentData.setPackage(null);
                    intentData.setData(Uri.parse("mailto:" + Keys.AUTHOR_EMAIL));
                    startActivity(intentData);
//                    Toast.makeText(MainActivity.this, "There are no email clients installed.",
//                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.nav_setting:
                Intent intentSetting = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intentSetting);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void switchFragment() {

        mViewPager.removeAllViews();
        mViewPager.setAdapter(new MyViewPagerAdapter(getFragmentManager(), this));

    }


    private void dailyCheck() {
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                .headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(DAILY_CHECK).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "daily mission failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() == 302) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HintUI.t(MainActivity.this, " 还未登录，请先登录");
                        }
                    });
                    return;
                }

                String body = response.body().string();

                if (body.contains("每日登录奖励已领取")) {
                    XLog.tag("MainActivity").w("已领取");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HintUI.t(MainActivity.this, "已领取，明天再来");
                        }
                    });
                    return;
                }

                String once = parseDailyOnce(body);

                if (once == null) {
                    XLog.tag(TAG).e("null once");
                    return;
                }
                postDailyCheck(once);
            }
        });
    }

    private void postDailyCheck(String once) {
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(HTTPS_V2EX_BASE + "/mission/daily/redeem?once=" + once)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "daily mission failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.w("MainActivity", "daily check ok");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintUI.t(MainActivity.this, "领取成功");
                    }
                });
            }
        });
    }

    private String parseDailyOnce(String string) {

        Element body = Jsoup.parse(string).body();
        Element onceElement = body.getElementsByAttributeValue("value", "领取 X 铜币").first();
        if (onceElement == null) {
            return null;
        }
//        location.href = '/mission/daily/redeem?once=83270';
        String onceOriginal = onceElement.attr("onClick");
        return TimeUtil.getNum(onceOriginal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XLog.tag(TAG).d("onResume");

//        bindService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XLog.tag(TAG).d("onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        XLog.tag(TAG).d("onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        XLog.tag(TAG).d("onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyApp.Companion.get().isLogin() && isOpenMessage() && !isBackground()) {
            stopService(intent);
        }
        XLog.tag(TAG).d("onDestroy");
        mViewPager.clearOnPageChangeListeners();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private boolean isBackground() {
        return sharedPreferences.getBoolean("pref_background_msg", false);
    }

    private boolean isOpenMessage() {
        return sharedPreferences.getBoolean("pref_msg", true);
    }

}

