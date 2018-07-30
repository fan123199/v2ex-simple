package im.fdx.v2ex.ui.main

import android.support.v7.util.DiffUtil
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.ui.details.Reply

/**
 * Created by fdx on 2017/7/11.
 * fdx will maintain it
 */
class MyDiffCallback(private val oldList: List<BaseModel>, private val newList: List<BaseModel>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        if (oldList[oldItemPosition] is Topic && newList[newItemPosition] is Topic) {
            return (oldList[oldItemPosition] as Topic).id == (newList[newItemPosition] as Topic).id
        } else if (oldList[oldItemPosition] is Reply && newList[newItemPosition] is Reply)
            return (oldList[oldItemPosition] as Reply).id == (newList[newItemPosition] as Reply).id
        else return false
    }

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size


    /**
     * 有点问题，GoodText不加载图片
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList[oldItemPosition] is Topic && newList[newItemPosition] is Topic) {
            return (oldList[oldItemPosition] as Topic).replies == (newList[newItemPosition] as Topic).replies &&
                    (oldList[oldItemPosition] as Topic).content_rendered == (newList[newItemPosition] as Topic).content_rendered
        } else if (oldList[oldItemPosition] is Reply && newList[newItemPosition] is Reply)
            return (oldList[oldItemPosition] as Reply).thanks == (newList[newItemPosition] as Reply).thanks &&
                    (oldList[oldItemPosition] as Reply).isThanked == (newList[newItemPosition] as Reply).isThanked
        else return false
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}