package im.fdx.v2ex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.fragment.NewArticleFragment;
import im.fdx.v2ex.ui.fragment.TopArticleFragment;


public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setActionBar(toolbar);



        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getFragmentManager());
        viewPager.setAdapter(mAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
//        tabLayout.setBackgroundResource(R.color.primary);
        tabLayout.setupWithViewPager(viewPager);
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;
        public String tabTitles[] = new String[]{"最新","热门"};

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NewArticleFragment();
                case 1:
                    return new TopArticleFragment();
//                case 2:
//                    return new AboutFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
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
            case R.id.menu_refresh:
//                L.t(this.getApplicationContext(), "choose Refresh");
                break;
            case R.id.menu_settings:
//                L.t(this.getApplicationContext(), "choose Settings");
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;
            case R.id.menu_login:
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
