package im.fdx.v2ex.utils

import com.elvishew.xlog.XLog
import im.fdx.v2ex.utils.extensions.logd

abstract class EndlessOnScrollListener(val rvReply: androidx.recyclerview.widget.RecyclerView) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

  private val mLinearLayoutManager = rvReply.layoutManager as androidx.recyclerview.widget.LinearLayoutManager

    var loading = false // True if we are still waiting for the last set of data to load.
    private val visibleThreshold = 2 // The minimum amount of items to have below your current scroll position before loading more.
    internal var firstVisibleItem: Int = 0
    internal var visibleItemCount: Int = 0
    internal var totalItemCount: Int = 0
    internal var lastVisibleItem: Int = 0
    internal var pageToLoad = 1
    var totalPage = 0
    internal var pageAfterLoaded = pageToLoad

  override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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

            logd("$totalItem , $lastVisibleItem , $pageToLoad")
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

}