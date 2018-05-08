package im.fdx.v2ex.ui.favor

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.setUpToolbar

class FavorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_activity)

        setUpToolbar("我的收藏")

        val tabLayout: TabLayout = findViewById(R.id.tl_favor)
        val viewPager: ViewPager = findViewById(R.id.viewpager_follow)
        viewPager.offscreenPageLimit = FavorViewPagerAdapter.titles.size
        viewPager.adapter = FavorViewPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }
}
