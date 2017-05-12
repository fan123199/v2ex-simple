package im.fdx.v2ex.ui.favor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v13.app.FragmentPagerAdapter;

import im.fdx.v2ex.ui.main.TopicsFragment;
import im.fdx.v2ex.utils.Keys;

/**
 * Created by fdx on 2017/4/13.
 */
class FavorViewPagerAdapter extends FragmentPagerAdapter {

    FavorViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    static final String[] titles = new String[]{"节点收藏", "主题收藏", "特别关注"};

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return new NodeFavorFragment();
        } else {
            TopicsFragment topicFavorFragment = new TopicsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Keys.FAVOR_FRAGMENT_TYPE, position);
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
