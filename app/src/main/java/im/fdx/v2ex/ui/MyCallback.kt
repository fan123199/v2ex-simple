package im.fdx.v2ex.ui

import android.support.v7.util.ListUpdateCallback
import im.fdx.v2ex.ui.main.TopicsRVAdapter


/**
 * Created by fdx on 2017/7/25.
 *
 */
internal class MyCallback : ListUpdateCallback {
    var firstInsert = -1
    var adapter: TopicsRVAdapter? = null

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter?.notifyItemRangeChanged(position, count, payload)
    }

    override fun onInserted(position: Int, count: Int) {
        if (firstInsert == -1 || firstInsert > position) {
            firstInsert = position
        }
        adapter?.notifyItemRangeInserted(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter?.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter?.notifyItemRangeRemoved(position, count)
    }
}