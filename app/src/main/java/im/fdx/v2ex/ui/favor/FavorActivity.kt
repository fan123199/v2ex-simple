package im.fdx.v2ex.ui.favor

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import im.fdx.v2ex.R

class FavorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_activity)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = "我的收藏"

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        val tabLayout = findViewById(R.id.tl_favor) as TabLayout
        val viewPager = findViewById(R.id.viewpager_follow) as ViewPager
        viewPager.offscreenPageLimit = FavorViewPagerAdapter.titles.size
        viewPager.adapter = FavorViewPagerAdapter(fragmentManager)
        tabLayout.setupWithViewPager(viewPager)


    }
}
