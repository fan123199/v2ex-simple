package im.fdx.v2ex.ui.member

import im.fdx.v2ex.data.model.Member
import androidx.recyclerview.widget.DiffUtil
import im.fdx.v2ex.data.model.MemberReplyModel

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

}


