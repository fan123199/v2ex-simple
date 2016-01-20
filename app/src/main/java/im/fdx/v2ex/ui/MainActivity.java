package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.adapter.ViewPagerAdapter;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);


        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawToggle = new ActionBarDrawerToggle(this,mDrawer,
                mToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(mDrawToggle);
        mDrawToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //这句话不懂什么意思，来自as1.40drawer模板
//        findViewById(R.id.design_menu_item_text).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getFragmentManager(),MainActivity.this);
        viewPager.setAdapter(mAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        //这句话可以省略，主要用于如果在其他地方对tablayout自定义title的话，
        // 忽略自定义，只从pageAdapter中获取title
//        mTabLayout.setTabsFromPagerAdapter(mAdapter);
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
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
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
//            case R.id.nav_slideshow:
//
//                break;
            case R.id.nav_manage:

                break;
            case R.id.nav_share:

                break;
            case R.id.nav_send:
                Intent data = new Intent(Intent.ACTION_SENDTO);
                data.putExtra(Intent.EXTRA_EMAIL, new String[]{"fan123199@qq.com"});
                data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
                data.putExtra(Intent.EXTRA_TEXT, "这是内容");
                try {
                    startActivity(Intent.createChooser(data, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.nav_setting:
                Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intentSetting);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
