package im.fdx.v2ex.ui;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.fragment.AboutFragment;
import im.fdx.v2ex.ui.fragment.NewArticleFragment;
import im.fdx.v2ex.ui.fragment.TopArticleFragment;
import im.fdx.v2ex.utils.L;


public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.ViewPager);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getFragmentManager());
        viewPager.setAdapter(mAdapter);


    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 3;
        public String tabTitles[] = new String[]{"最新","热门","关于"};

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
                case 2:
                    return new AboutFragment();

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
                L.t(this.getApplicationContext(), "choose Refresh");
                break;
            case R.id.menu_settings:
                L.t(this.getApplicationContext(), "choose Settings");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
