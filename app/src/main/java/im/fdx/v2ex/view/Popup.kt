package im.fdx.v2ex.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.details.DetailsAdapter
import im.fdx.v2ex.ui.details.ReplyModel
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import kotlinx.android.synthetic.main.item_reply_view.*

/**
 * Created by fdx on 2017/7/14.
 * fdx will maintain it
 */

class Popup(mActivity: Context) {
    private val popupWindow: PopupWindow
    private var contentView: View = LayoutInflater.from(mActivity).inflate(R.layout.item_reply_view, null)

    init {
        popupWindow = PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 16f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    @SuppressLint("SetTextI18n")
    fun show(v: View, data: ReplyModel, position: Int, clickListener: View.OnClickListener) {

        popupWindow.width = v.width
        val hd = DetailsAdapter.ItemViewHolder(contentView)
        hd.tv_reply_content.movementMethod = ScrollingMovementMethod.getInstance();
        hd.tv_reply_content.maxLines = 4
        hd.tv_reply_content.isVerticalScrollBarEnabled = true
        hd.tv_reply_content.setGoodText(data.content_rendered)
        hd.tv_louzu.visibility = if (data.isLouzu) View.VISIBLE else View.GONE
        hd.tv_reply_row.text = "#$position"
        hd.tv_replier.text = data.member?.username
        hd.iv_thanks.visibility = View.INVISIBLE
        hd.iv_reply.visibility = View.INVISIBLE
        hd.iv_reply_avatar.load(data.member?.avatarLargeUrl)
        hd.tv_reply_time.text = TimeUtil.getRelativeTime(data.created)
        contentView.setOnClickListener(clickListener)

        popupWindow.showAsDropDown(v, 0, -v.height)
    }


    interface PopupListener {
        fun onClick(v: View, url: String)
    }
}
