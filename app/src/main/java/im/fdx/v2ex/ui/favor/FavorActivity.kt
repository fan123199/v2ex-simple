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
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "我的收藏"

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        val tabLayout: TabLayout = findViewById(R.id.tl_favor)
        val viewPager: ViewPager = findViewById(R.id.viewpager_follow)
        viewPager.offscreenPageLimit = FavorViewPagerAdapter.titles.size
        viewPager.adapter = FavorViewPagerAdapter(fragmentManager)
        tabLayout.setupWithViewPager(viewPager)


    }
}
