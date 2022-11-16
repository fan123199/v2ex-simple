package im.fdx.v2ex.ui.favor

import android.os.Bundle
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.favor.FavorViewPagerAdapter.Companion.titles
import im.fdx.v2ex.ui.tabTitles
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlin.math.abs

class FavorActivity : BaseActivity() {
     lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_activity)

        setUpToolbar(getString(R.string.my_follow))

      val tabLayout: TabLayout = findViewById(R.id.tl_favor)
        viewPager = findViewById(R.id.viewpager_follow)
        viewPager.offscreenPageLimit = titles.size
        viewPager.adapter = FavorViewPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }


    private var initialXValue = 0f
    private var initialYValue = 0f
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if (ev.action == MotionEvent.ACTION_DOWN) {
            initialXValue = ev.x
            initialYValue = ev.y
        }
        if (ev.action == MotionEvent.ACTION_MOVE) {
            val diffX: Float = ev.x - initialXValue
            val diffY: Float = ev.y - initialYValue
            if (abs(diffY) > 1.4 * abs(diffX)) {
                viewPager.isUserInputEnabled = false
            }
        }
        if (ev.action == MotionEvent.ACTION_UP) {
            initialXValue = 0f
            initialYValue = 0f
            viewPager.isUserInputEnabled = true
        }
        return super.dispatchTouchEvent(ev)
    }

}
