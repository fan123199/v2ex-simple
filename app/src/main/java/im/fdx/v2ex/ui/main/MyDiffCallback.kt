package im.fdx.v2ex.ui.main

import androidx.recyclerview.widget.DiffUtil

/**
 * Created by fdx on 2017/7/11.
 * fdx will maintain it
 */
class MyDiffCallback(private val oldList: List<Topic>, private val newList: List<Topic>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    /**
     * 有点问题，GoodText不加载图片
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].replies == newList[newItemPosition].replies &&
                oldList[oldItemPosition].content_rendered == newList[newItemPosition].content_rendered
    }

}