package im.fdx.v2ex.ui.member

import android.app.FragmentManager
import android.support.v13.app.FragmentPagerAdapter
import im.fdx.v2ex.ui.main.TopicsFragment
import im.fdx.v2ex.utils.Keys
import org.jetbrains.anko.bundleOf

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 */
class MemberViewpagerAdapter(fm: FragmentManager?, var username: String) : FragmentPagerAdapter(fm) {

    val titles = arrayOf("主题", "评论")

    override fun getItem(position: Int) = when (position) {
        0 -> TopicsFragment().apply { arguments = bundleOf(Keys.KEY_USERNAME to username) }
        else -> ReplyFragment().apply { arguments = bundleOf(Keys.KEY_USERNAME to username) }
    }

    override fun getCount() = 2
    override fun getPageTitle(position: Int) = titles[position]

}