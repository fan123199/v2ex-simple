package im.fdx.v2ex.ui.favor

import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys

/**
 * Created by fdx on 2017/4/13.
 */
internal class FavorViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = when (position) {
        0 -> NodeFavorFragment()
        else -> {
            val topicFavorFragment = TopicsFragment()
            val bundle = Bundle()
            bundle.putInt(Keys.FAVOR_FRAGMENT_TYPE, position)
            topicFavorFragment.arguments = bundle
            topicFavorFragment
        }
    }

    override fun getCount() = titles.size
    override fun getPageTitle(position: Int): CharSequence = titles[position]

    companion object {
        val titles = arrayOf("节点收藏", "主题收藏", "特别关注")
    }
}
