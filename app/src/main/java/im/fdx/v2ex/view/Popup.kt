package im.fdx.v2ex.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.details.ItemViewHolder
import im.fdx.v2ex.ui.details.Reply
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
        popupWindow.isFocusable = true
        val colorBackground = ContextCompat.getColor(mActivity, R.color.item_background)
        popupWindow.setBackgroundDrawable(ColorDrawable(colorBackground))
    }


    @SuppressLint("SetTextI18n")
    fun show(v: View, data: Reply, index: Int, clickListener: (Int) -> Unit) {

        val position  = index + 1 //这是对应关系
        val hd = ItemViewHolder(contentView)
        hd.bind(data)
        hd.tv_reply_content.movementMethod = ScrollingMovementMethod.getInstance()
        hd.tv_reply_content.maxLines = 4
        hd.tv_reply_content.isVerticalScrollBarEnabled = true
        hd.tv_reply_row.text = "#$position"
        hd.iv_thanks.visibility = View.INVISIBLE
        hd.iv_reply.visibility = View.INVISIBLE
        hd.divider.visibility = View.INVISIBLE
        contentView.setOnClickListener {
            clickListener(position)
        }

        popupWindow.width = v.width
        popupWindow.showAsDropDown(v, 0, -v.height)
    }


    interface PopupListener {
        fun onClick(v: View, url: String)
    }
}
