package im.fdx.v2ex.ui.favor

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.favor.FavorViewPagerAdapter.Companion.titles
import im.fdx.v2ex.ui.tabTitles
import im.fdx.v2ex.utils.extensions.setUpToolbar

class FavorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_activity)

        setUpToolbar(getString(R.string.my_follow))

      val tabLayout: TabLayout = findViewById(R.id.tl_favor)
      val viewPager: ViewPager2 = findViewById(R.id.viewpager_follow)
        viewPager.offscreenPageLimit = titles.size
        viewPager.adapter = FavorViewPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}
