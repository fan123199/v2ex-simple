package im.fdx.v2ex.view

import android.view.MotionEvent
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


//防止 下滑时 不小心给 左右滑了
class ViewPagerHelper(val viewPager2: ViewPager2) {

    private var initialXValue = 0f
    private var initialYValue = 0f
    fun dispatchTouchEvent(ev: MotionEvent) {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            initialXValue = ev.x
            initialYValue = ev.y
        }
        if (ev.action == MotionEvent.ACTION_MOVE) {
            val diffX: Float = ev.x - initialXValue
            val diffY: Float = ev.y - initialYValue
            if (abs(diffY) > 1.4 * abs(diffX)) {
                if (viewPager2.scrollState != ViewPager2.SCROLL_STATE_DRAGGING) {
                    viewPager2.isUserInputEnabled = false
                }
            }
        }
        if (ev.action == MotionEvent.ACTION_UP) {
            initialXValue = 0f
            initialYValue = 0f
            viewPager2.isUserInputEnabled = true
        }
    }


}