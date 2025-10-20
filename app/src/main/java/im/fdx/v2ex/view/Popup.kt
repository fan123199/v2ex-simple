package im.fdx.v2ex.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
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
import androidx.core.graphics.drawable.toDrawable
import im.fdx.v2ex.utils.extensions.getColorFromAttr


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
        popupWindow.setBackgroundDrawable(mActivity.getColorFromAttr(R.attr.toolbar_background).toDrawable())
    }


    @SuppressLint("SetTextI18n")
    fun show(v: View, data: Reply, rowNum: Int, clickListener: (Int) -> Unit) {
        val hd = ItemViewHolder(binding)
        hd.bind(data)
        hd.binding.tvReplyContent.maxLines = 5
        hd.binding.tvReplyContent.isVerticalScrollBarEnabled = true
        hd.binding.tvReplyRow.text = "#$rowNum"
        hd.binding.ivThanks.visibility = View.GONE
        hd.binding.tvThanks.visibility = View.GONE
        hd.binding.root.forEach {
            it.isClickable = false
        }
        hd.binding.ivReply.visibility = View.GONE
        hd.binding.divider.visibility = View.GONE
        hd.binding.root.setOnClickListener {
            popupWindow.dismiss()
            clickListener(rowNum) //加上正文， index = rowNum
        }

        popupWindow.width = v.width
        popupWindow.showAsDropDown(v, 0, -v.height)
    }


    interface PopupListener {
        fun onClick(v: View, url: String)
    }
}
