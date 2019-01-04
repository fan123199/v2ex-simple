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
import androidx.core.content.ContextCompat
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.details.Reply
import im.fdx.v2ex.ui.details.TopicDetailAdapter
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
        val colorBackground = ContextCompat.getColor(mActivity, R.color.item_background)
        popupWindow.setBackgroundDrawable(ColorDrawable(colorBackground))
    }


    @SuppressLint("SetTextI18n")
    fun show(v: View, data: Reply, position: Int, clickListener: (Int, Int) -> Unit) {

        popupWindow.width = v.width
        val hd = TopicDetailAdapter.ItemViewHolder(contentView)
        hd.bind(data)
        hd.tv_reply_content.movementMethod = ScrollingMovementMethod.getInstance();
        hd.tv_reply_content.maxLines = 4
        hd.tv_reply_content.isVerticalScrollBarEnabled = true
        hd.tv_reply_row.text = "#$position"
        hd.iv_thanks.visibility = View.INVISIBLE
        hd.iv_reply.visibility = View.INVISIBLE
        contentView.setOnClickListener {
            clickListener(-1, position)
        }

        popupWindow.showAsDropDown(v, 0, -v.height)
    }


    interface PopupListener {
        fun onClick(v: View, url: String)
    }
}
