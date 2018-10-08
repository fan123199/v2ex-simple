package im.fdx.v2ex.ui.favor

import android.os.Bundle
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.utils.extensions.setUpToolbar

class FavorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_activity)

        setUpToolbar(getString(R.string.my_follow))

      val tabLayout: com.google.android.material.tabs.TabLayout = findViewById(R.id.tl_favor)
      val viewPager: androidx.viewpager.widget.ViewPager = findViewById(R.id.viewpager_follow)
        viewPager.offscreenPageLimit = FavorViewPagerAdapter.titles.size
        viewPager.adapter = FavorViewPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }
}
