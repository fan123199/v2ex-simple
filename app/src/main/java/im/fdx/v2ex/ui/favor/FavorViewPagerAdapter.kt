package im.fdx.v2ex.ui.favor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys

/**
 * Created by fdx on 2017/4/13.
 */
internal class FavorViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    companion object {
        val titles = arrayOf( "主题收藏", "节点收藏","特别关注")
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NodeFavorFragment()
            else -> {
                TopicsFragment().apply {
                    arguments = bundleOf(Keys.FAVOR_FRAGMENT_TYPE to position)
                }
            }
        }
    }
}
