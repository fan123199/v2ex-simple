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
import androidx.core.view.forEach
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ItemReplyViewBinding
import im.fdx.v2ex.ui.topic.ItemViewHolder
import im.fdx.v2ex.ui.topic.Reply

/**
 * Created by fdx on 2017/7/14.
 * fdx will maintain it
 */

class Popup(mActivity: Context) {
    private val popupWindow: PopupWindow
    private var binding: ItemReplyViewBinding = ItemReplyViewBinding.inflate(LayoutInflater.from(mActivity), null, false)

    init {
        popupWindow = PopupWindow(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 16f
        popupWindow.isFocusable = true
        val colorBackground = ContextCompat.getColor(mActivity, R.color.item_background)
        popupWindow.setBackgroundDrawable(ColorDrawable(colorBackground))
    }


    @SuppressLint("SetTextI18n")
    fun show(v: View, data: Reply, rowNum: Int, clickListener: (Int) -> Unit) {
        val hd = ItemViewHolder(binding)
        hd.bind(data)
        hd.binding.tvReplyContent.maxLines = 4
        hd.binding.tvReplyContent.isVerticalScrollBarEnabled = true
        hd.binding.tvReplyContent.disableTouch()
        hd.binding.tvReplyRow.text = "#$rowNum"
        hd.binding.ivThanks.visibility = View.INVISIBLE
        hd.binding.tvThanks.visibility = View.INVISIBLE
        hd.binding.root.forEach {
            it.isClickable = false
        }
        hd.binding.ivReply.visibility = View.INVISIBLE
        hd.binding.divider.visibility = View.INVISIBLE
        hd.binding.root.setOnClickListener {
            popupWindow.dismiss()
            clickListener(rowNum - 1)
        }

        popupWindow.width = v.width
        popupWindow.showAsDropDown(v, 0, -v.height)
    }


    interface PopupListener {
        fun onClick(v: View, url: String)
    }
}
