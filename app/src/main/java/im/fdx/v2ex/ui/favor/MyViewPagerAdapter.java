package im.fdx.v2ex.ui.favor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

import im.fdx.v2ex.ui.main.TopicsFragment;

/**
 * Created by fdx on 2017/4/13.
 */
class MyViewPagerAdapter extends FragmentPagerAdapter {

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    String[] titles = new String[]{"节点收藏", "主题收藏", "特别关注"};

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            NodeFavorFragment nodeFavorFragment = new NodeFavorFragment();
            return nodeFavorFragment;
        } else {
            TopicsFragment topicFavorFragment = new TopicsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type", position);
            topicFavorFragment.setArguments(bundle);
            return topicFavorFragment;
        }
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
