package im.fdx.v2ex.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.utils.extensions.logd

abstract class EndlessOnScrollListener(val rvReply: RecyclerView, val mLinearLayoutManager:LinearLayoutManager) : RecyclerView.OnScrollListener() {

  private val visibleThreshold = 2

  private var pageToLoad = 1

  var loading = false
  var totalPage = 0

  private var pageAfterLoaded = pageToLoad

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    XLog.d("onScrolled + $dy")
    if (dy <= 0) {
      return
    }
    val visibleItemCount = recyclerView.childCount
    val totalItemCount = mLinearLayoutManager.itemCount
    val firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()
    val lastVisibleItem = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()

    synchronized(this) {

      logd("$totalItemCount , $lastVisibleItem , $pageToLoad")
      if (lastVisibleItem == totalItemCount - 1) {
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

  fun restart() {
    pageToLoad = 1
  }

  fun isRestart() :Boolean {
    return pageToLoad == 1
  }

  fun success() {
    pageAfterLoaded = pageToLoad
  }

  abstract fun onLoadMore(currentPage: Int)
  abstract fun onCompleted()

}