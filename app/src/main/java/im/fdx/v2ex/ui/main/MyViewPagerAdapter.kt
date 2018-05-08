package im.fdx.v2ex.ui.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.Keys
import java.util.*


/**
 * Created by fdx on 2015/10/15.
 * 从MainActivity分离出来. 用了FragmentStatePagerAdapter 替代FragmentPagerAdapter，才可以动态切换Fragment
 * 弃用了Volley 和 模拟web + okhttp
 */
internal class MyViewPagerAdapter(fm: FragmentManager, private val mContext: Context) : FragmentStatePagerAdapter(fm) {

    private val mFragments = ArrayList<TopicsFragment>()
    private val mTabTitles = ArrayList<String>()

    init {
        initFragment()
    }

    @Synchronized fun initFragment() {
        mTabTitles.clear()
        mFragments.clear()
        val tabTitles = mContext.resources.getStringArray(R.array.v2ex_favorite_tab_titles)
        val tabPaths = mContext.resources.getStringArray(R.array.v2ex_favorite_tab_paths)
        for (i in tabPaths.indices) {

            if (!MyApp.get().isLogin && tabPaths[i] == "recent") {
                continue
            }

            val fragment = TopicsFragment()
            val bundle = Bundle()
            bundle.putString(Keys.KEY_TAB, tabPaths[i])
            fragment.arguments = bundle
            mFragments.add(fragment)
            mTabTitles.add(tabTitles[i])
        }
    }

    override fun getItem(position: Int) = mFragments[position]
    override fun getCount() = mTabTitles.size
    override fun getPageTitle(position: Int) = mTabTitles[position]

}
