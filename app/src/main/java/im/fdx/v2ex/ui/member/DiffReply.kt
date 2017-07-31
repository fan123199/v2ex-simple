package im.fdx.v2ex.ui.member

import android.support.v7.util.DiffUtil

/**
 * Created by fdx on 2017/7/31.
 *
 */
class DiffReply(val oldList: List<MemberReplyModel>, val newList: List<MemberReplyModel>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int)
            = oldList[oldItemPosition].content == newList[newItemPosition].content

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    /**
     * 有点问题，GoodText不加载图片
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int)
            = oldList[oldItemPosition].content == newList[newItemPosition].content

    /*
    // TODO: 2017/7/18 高级用法，可用bundle 然后在onBindViewHolder（xx,xx, payload）中，复写，太多了~~
     */
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}