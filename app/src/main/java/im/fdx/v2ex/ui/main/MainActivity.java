package im.fdx.v2ex.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.LoginActivity;
import im.fdx.v2ex.ui.SettingsActivity;
import im.fdx.v2ex.ui.node.AllNodesActivity;
import im.fdx.v2ex.utils.Keys;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawer;
    private Notification mNotificationCompat;
    private NotificationManager mNotificationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawToggle = new ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mDrawToggle);
        mDrawToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        MyViewPagerAdapter mAdapter = new MyViewPagerAdapter(getFragmentManager(), MainActivity.this);
        viewPager.setAdapter(mAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        //这句话可以省略，主要用于如果在其他地方对tablayout自定义title的话，
        // 忽略自定义，只从pageAdapter中获取title
//        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        assert mTabLayout != null;
        mTabLayout.setupWithViewPager(viewPager);
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
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
//            case R.id.nav_camera:
//                // Handle the camera action
//                break;
//            case R.id.nav_gallery:
//
//                break;
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
}
