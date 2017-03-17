package im.fdx.v2ex.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.ui.LoginActivity;
import im.fdx.v2ex.ui.SettingsActivity;
import im.fdx.v2ex.ui.node.AllNodesActivity;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static im.fdx.v2ex.network.JsonManager.DAILY_CHECK;
import static im.fdx.v2ex.network.JsonManager.HTTPS_V2EX_BASE;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int LOG_IN_SUCCEED = 1;
    private static final int LOG_IN = 0;
    private static final int ID_ITEM_CHECK = 33;
    DrawerLayout mDrawer;
    private Notification mNotificationCompat;
    private NotificationManager mNotificationManager;
    private ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;
    private NavigationView navigationView;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("im.fdx.v2ex.preference")) {
                switchFragment();
            } else if (intent.getAction().equals("im.fdx.v2ex.event.login")) {
                changeUIWhenLogin();
            }

        }
    };
    private final IntentFilter intentFilter = new IntentFilter("im.fdx.v2ex.preference");
    private int i = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawToggle = new ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mDrawToggle);
        mDrawToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new MyViewPagerAdapter(getFragmentManager(), MainActivity.this);
        mViewPager.setAdapter(mAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        //这句话可以省略，主要用于如果在其他地方对tablayout自定义title的话，
        // 忽略自定义，只从pageAdapter中获取title
//        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        assert mTabLayout != null;
        mTabLayout.setupWithViewPager(mViewPager);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOG_IN);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_testMenu:
                break;
            case R.id.nav_testMenu2:

//                changeUIWhenLogin();
                break;
            case R.id.nav_node:
                startActivity(new Intent(this, AllNodesActivity.class));

                break;
            case R.id.nav_testNotify:
                Intent itNoti = new Intent();
                PendingIntent pdit = PendingIntent.getActivity(this, 0, itNoti, 0);

                Intent resultIntent = new Intent(MainActivity.this, SettingsActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(SettingsActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                int notifyID = 1;
                int color = Color.argb(127, 255, 0, 255);
                int c;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    c = getResources().getColor(R.color.primary, getTheme());
                } else {
                    c = color;
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle("fdx")
                        .setContentText("shinishinijiushi")
                        .setSubText("subtext")
                        .setTicker("this is from others")
                        .setWhen(System.currentTimeMillis() / 1000)
                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setLargeIcon(R.drawable.logo2x)
                        .setLights(c, 2000, 1000)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent);
                mNotificationCompat = mBuilder.build();
                mNotificationManager.notify(notifyID, mNotificationCompat);
                break;
            case R.id.nav_share:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_TEXT, "这是真正的内容");
//                intentShare.putExtra(Intent.EXTRA_TITLE, "这是Title");
                startActivity(Intent.createChooser(intentShare, getString(R.string.share_to)));
                break;
            case R.id.nav_feedback:
                Intent intentData = new Intent(Intent.ACTION_SEND);
                intentData.setType("message/rfc822");
                intentData.putExtra(Intent.EXTRA_EMAIL, new String[]{Keys.AUTHOR_EMAIL});
                intentData.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                intentData.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_hint) + "\n");
                try {
                    startActivity(Intent.createChooser(intentData, getString(R.string.send_email)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
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
//        mAdapter.initFragment();
//        mAdapter.notifyDataSetChanged();

    }


    private void changeUIWhenLogin() {
        MenuItem item2;
        if (navigationView.getMenu().findItem(ID_ITEM_CHECK) == null) {

            item2 = navigationView.getMenu().add(R.id.group_nav_main, ID_ITEM_CHECK, 88, R.string.daily_check);

            item2.setIcon(android.R.drawable.ic_menu_myplaces);
            item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    dailyCheck();
                    return true;
                }
            });
        }

        this.invalidateOptionsMenu();

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (MyApp.getInstance().isLogin()) {
            menu.findItem(R.id.menu_login).setVisible(false);
            XLog.tag(TAG).d("INvisible");
        } else {
            menu.findItem(R.id.menu_login).setVisible(true);
            XLog.tag(TAG).d("visible");
        }
        return super.onPrepareOptionsMenu(menu);


    }

    private void dailyCheck() {
        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(HttpHelper.baseHeaders)
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
        HttpHelper.OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(HTTPS_V2EX_BASE + "/mission/daily/redeem?once=" + once)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "daily mission failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.w("MainActivity", "good");
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
        return TimeHelper.getNum(onceOriginal);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        XLog.tag(TAG).d("onRestart");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        XLog.tag(TAG).d("onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XLog.tag(TAG).d("onDestroy");
    }
}
