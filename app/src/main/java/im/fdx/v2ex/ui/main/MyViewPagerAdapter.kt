package im.fdx.v2ex.ui.main

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.myApp
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.MyTab
import im.fdx.v2ex.ui.tabPaths
import im.fdx.v2ex.ui.tabTitles
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.Keys.PREF_TAB
import java.util.*


/**
 * Created by fdx on 2015/10/15.
 * 从MainActivity分离出来. 用了FragmentStatePagerAdapter 替代FragmentPagerAdapter，才可以动态切换Fragment
 * 弃用了Volley 和 模拟web + okhttp
 *
 * todo pageadapter有更新，明天需要完成
 */
internal class MyViewPagerAdapter(
        fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val mFragments = ArrayList<TopicsFragment>()
    val myTabList = mutableListOf<MyTab>()
    init {
        initFragment()
    }

    fun initFragment() {
        myTabList.clear()
        mFragments.clear()

        var jsonData = pref.getString(PREF_TAB, null)
        if (jsonData == null) {
            val list = MutableList(tabTitles.size) { index: Int ->
                MyTab(tabTitles[index], tabPaths[index])
            }

            jsonData = Gson().toJson(list)
        }

        val turnsType = object : TypeToken<List<MyTab>>() {}.type
        val list = Gson().fromJson<List<MyTab>>(jsonData, turnsType)

        if(list.isNullOrEmpty()) { //可能的一些奇怪的问题
            MutableList(tabTitles.size) { index: Int ->
                MyTab(tabTitles[index], tabPaths[index])
            }
        }

        for (it in list) {
            if (!myApp.isLogin && it.path == "recent") {
                continue
            }
            mFragments.add(TopicsFragment().apply { arguments = bundleOf(Keys.KEY_TAB to it.path, Keys.KEY_TYPE to it.type) })
            myTabList.add(it)
        }
    }

    override fun getItemCount(): Int {
        return  myTabList.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        return if (position < itemCount -1)
            mFragments[position]
        else
            Fragment()
    }

}
