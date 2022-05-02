package im.fdx.v2ex.ui.main

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.Tab
import im.fdx.v2ex.ui.tabPaths
import im.fdx.v2ex.ui.tabTitles
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.PREF_TAB
import org.jetbrains.anko.collections.forEachWithIndex
import java.util.*


/**
 * Created by fdx on 2015/10/15.
 * 从MainActivity分离出来. 用了FragmentStatePagerAdapter 替代FragmentPagerAdapter，才可以动态切换Fragment
 * 弃用了Volley 和 模拟web + okhttp
 *
 * todo pageadapter有更新，明天需要完成
 */
internal class MyViewPagerAdapter(
        fm: FragmentManager,
        private val mContext: Context) : FragmentStatePagerAdapter(fm, BEHAVIOR_SET_USER_VISIBLE_HINT ) {

    private val mFragments = ArrayList<TopicsFragment>()
    private val tabList = mutableListOf<Tab>()
    init {
        initFragment()
    }

    fun initFragment() {
        tabList.clear()
        mFragments.clear()

        var jsonData = pref.getString(PREF_TAB, null)
        if (jsonData == null) {
            val list = MutableList(tabTitles.size) { index: Int ->
                Tab(tabTitles[index], tabPaths[index])
            }

            jsonData = Gson().toJson(list)
        }

        val turnsType = object : TypeToken<List<Tab>>() {}.type
        val list = Gson().fromJson<List<Tab>>(jsonData, turnsType)

        for (it in list) {
            if (!myApp.isLogin && it.path == "recent") {
                continue
            }
            mFragments.add(TopicsFragment().apply { arguments = bundleOf(Keys.KEY_TAB to it.path, Keys.KEY_TYPE to it.type) })
            tabList.add(it)
        }
    }

    override fun getItem(position: Int) = mFragments[position]
    override fun getCount() = tabList.size
    override fun getPageTitle(position: Int) = tabList[position].title

}
