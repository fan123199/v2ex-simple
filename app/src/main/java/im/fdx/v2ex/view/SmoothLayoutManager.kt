package im.fdx.v2ex.view

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView

class SmoothLayoutManager : LinearLayoutManager {

    constructor(context: Context) : super(context, LinearLayoutManager.VERTICAL, false)

    @Suppress("unused")
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?,
                                        position: Int) {
        val smoothScroller = TopSnappedSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private inner class TopSnappedSmoothScroller(context: Context) : LinearSmoothScroller(context) {

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? = this@SmoothLayoutManager
                .computeScrollVectorForPosition(targetPosition)

        override fun getVerticalSnapPreference() = LinearSmoothScroller.SNAP_TO_START
    }
}