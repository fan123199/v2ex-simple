package im.fdx.v2ex.utils.extensions

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */


fun Context.t(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.T(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Any.d(message: String) = Log.d(this.javaClass.simpleName, message)