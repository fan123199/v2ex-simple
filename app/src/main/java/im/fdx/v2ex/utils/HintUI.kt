package im.fdx.v2ex.utils

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

/**
 * Created by fdx on 2015/8/18.
 * Toast或Snackbar的封装
 */
object HintUI {

    fun t(context: Context, message: String) = Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show()
    fun T(context: Context, message: String) = Toast.makeText(context, "" + message, Toast.LENGTH_LONG).show()
    fun s(view: View, message: String) = Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    fun S(view: View, message: String) = Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()

}
