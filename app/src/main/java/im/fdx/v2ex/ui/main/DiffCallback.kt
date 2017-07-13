package im.fdx.v2ex.ui.main

import android.support.v7.util.DiffUtil
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.ui.details.ReplyModel

/**
 * Created by fdx on 2017/7/11.
 * fdx will maintain it
 */
class DiffCallback(val oldList: List<BaseModel>, val newList: List<BaseModel>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        if (oldList[oldItemPosition] is TopicModel && newList[newItemPosition] is TopicModel) {
            return (oldList[oldItemPosition] as TopicModel).id == (newList[newItemPosition] as TopicModel).id
        } else if (oldList[oldItemPosition] is ReplyModel && newList[newItemPosition] is ReplyModel)
            return (oldList[oldItemPosition] as ReplyModel).id == (newList[newItemPosition] as ReplyModel).id
        else return false
    }

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList[oldItemPosition] is TopicModel && newList[newItemPosition] is TopicModel) {
            return (oldList[oldItemPosition] as TopicModel).replies == (newList[newItemPosition] as TopicModel).replies &&
                    (oldList[oldItemPosition] as TopicModel).content_rendered == (newList[newItemPosition] as TopicModel).content_rendered
        } else if (oldList[oldItemPosition] is ReplyModel && newList[newItemPosition] is ReplyModel)
            return (oldList[oldItemPosition] as ReplyModel).thanks == (newList[newItemPosition] as ReplyModel).thanks &&
                    (oldList[oldItemPosition] as ReplyModel).isThanked == (newList[newItemPosition] as ReplyModel).isThanked
        else return false
    }
}