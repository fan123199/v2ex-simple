package im.fdx.v2ex.utils.extensions

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.Toast

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */


fun Context.t(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.T(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun View.S(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

fun View.s(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

fun Any.d(message: String) = Log.d(this.javaClass.simpleName, message)