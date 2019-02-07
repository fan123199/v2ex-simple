package im.fdx.v2ex.ui.favor

import android.os.Bundle
import androidx.core.os.bundleOf
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys

/**
 * Created by fdx on 2017/4/13.
 */
internal class FavorViewPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = when (position) {
        1 -> NodeFavorFragment()
        else -> {
            TopicsFragment().apply {
                arguments = bundleOf(Keys.FAVOR_FRAGMENT_TYPE to position)
            }
        }
    }

    override fun getCount() = titles.size
    override fun getPageTitle(position: Int) = titles[position]

    companion object {
        val titles = arrayOf( "主题收藏", "节点收藏","特别关注")
    }
}
