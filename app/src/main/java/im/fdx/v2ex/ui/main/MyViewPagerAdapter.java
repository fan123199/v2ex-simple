package im.fdx.v2ex.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.main.TopicsFragment;

/**
 * Created by fdx on 2015/10/15.
 * 从MainActivity分离出来
 */
public class MyViewPagerAdapter extends FragmentStatePagerAdapter {

//        private static final int PAGE_COUNT = 2;
//        public String tabTitles[] = new String[]{"最新","热门"};

    private List<TopicsFragment> mFragments = new ArrayList<>();
    private List<String> mTabTitles = new ArrayList<>();

    private Context mCtx;

    public MyViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mCtx = context;
        initFragment();

    }

    private void initFragment() {
        //Latest topic
        TopicsFragment latestTopicsFragment = new TopicsFragment();
        Bundle bundle_latest = new Bundle();
        bundle_latest.putInt("column_id", TopicsFragment.LATEST_TOPICS);
        latestTopicsFragment.setArguments(bundle_latest);
        mFragments.add(latestTopicsFragment);
        mTabTitles.add(mCtx.getString(R.string.tab_title_latest));

        //Top10 topic
        TopicsFragment Top10TopicsFragment = new TopicsFragment();
        Bundle bundle_top10 = new Bundle();
        bundle_top10.putInt("column_id", TopicsFragment.TOP_10_TOPICS);

        Top10TopicsFragment.setArguments(bundle_top10);
        mFragments.add(Top10TopicsFragment);
        mTabTitles.add(mCtx.getString(R.string.tab_title_top10));

        //web homepage tab topic
//        String[] tabTitles = mCtx.getResources().getStringArray(R.array.v2ex_favorite_tab_titles);
//        String[] TabPaths = mCtx.getResources().getStringArray(R.array.v2ex_favorite_tab_paths);
//        for (int i = 0; i < mTabTitles.size(); i++) {
//            TopicsFragment fragment = new TopicsFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString("tab", TabPaths[i]);
//            fragment.setArguments(bundle);
//            mTabTitles.add(tabTitles[i]);
//        }
    }

    @Override
    public Fragment getItem(int position) {

        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles.get(position);
    }
}
