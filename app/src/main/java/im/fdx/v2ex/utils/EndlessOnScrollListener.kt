package im.fdx.v2ex.utils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.elvishew.xlog.XLog

abstract class EndlessOnScrollListener(private val mLinearLayoutManager: LinearLayoutManager, val rvReply: RecyclerView) : RecyclerView.OnScrollListener() {

    var loading = false // True if we are still waiting for the last set of data to load.
    private val visibleThreshold = 2 // The minimum amount of items to have below your current scroll position before loading more.
    internal var firstVisibleItem: Int = 0
    internal var visibleItemCount: Int = 0
    internal var totalItemCount: Int = 0
    internal var lastVisibleItem: Int = 0
    internal var pageToLoad = 1
    var totalPage = 0
    internal var pageAfterLoaded = pageToLoad

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        XLog.d("onScrolled + $dy")

        if (dy < 0) {
            return
        }
        // check for scroll down only
        visibleItemCount = recyclerView.childCount
        totalItemCount = mLinearLayoutManager.itemCount
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()
        lastVisibleItem = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()

        // to make sure only one onLoadMore is triggered
        synchronized(this) {
            val totalItem = totalItemCount

            Log.d(TAG, "$totalItem , $lastVisibleItem , $pageToLoad")
            if (lastVisibleItem == totalItem - 1) {
                rvReply.stopScroll()
                if (pageAfterLoaded == totalPage) {
                    onCompleted()
                }
            }

            if (pageToLoad < totalPage && !loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
                // End has been reached, Do something
                pageToLoad++
                onLoadMore(pageToLoad)
                loading = true
            }
        }
    }

    abstract fun onLoadMore(current_page: Int)

    abstract fun onCompleted()

    companion object {
        var TAG = EndlessOnScrollListener::class.java.simpleName
    }

}