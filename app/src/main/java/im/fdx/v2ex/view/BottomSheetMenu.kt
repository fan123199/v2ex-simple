package im.fdx.v2ex.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import im.fdx.v2ex.R

/**
 * Created by fdx on 2017/11/25.
 *
 * 基础底部弹出组件
 *
 */
class BottomSheetMenu(private val activity: Activity) {

    private val bottomSheet: BottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetDialogStyle)
    private var container: LinearLayout

    init {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_bottom, null, false)
        container = contentView.findViewById(R.id.linear)
        bottomSheet.setContentView(contentView)
    }


    fun addItem(title: String, action: ()->Unit): BottomSheetMenu {
        val view = LayoutInflater.from(activity).inflate(R.layout.simple_list_item_center, null, false) as TextView
        view.text =  title
        view.setOnClickListener{
            action()
            bottomSheet.dismiss()
        }
        val p = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        container.addView(view, p)
        return this
    }


    fun show() {
        if(!activity.isDestroyed) {
            bottomSheet.show()
        }
    }
}