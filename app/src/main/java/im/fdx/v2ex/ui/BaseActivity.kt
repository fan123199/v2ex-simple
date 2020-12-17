package im.fdx.v2ex.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys


const val MODE_SMALL = 0
const val MODE_BIG2 = 1
const val MODE_BIG3 = 2
const val MODE_BIG4 = 3

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val textSizeMode = pref.getString(Keys.PREF_TEXT_SIZE, MODE_SMALL.toString())!!.toInt()
        when (textSizeMode) {
            MODE_SMALL -> {
                setTheme(R.style.Theme_V2ex)
            }
            MODE_BIG2 -> {
                setTheme(R.style.Theme_V2ex_big2)
            }
            MODE_BIG3 -> {
                setTheme(R.style.Theme_V2ex_big3)
            }
            MODE_BIG4 -> {
                setTheme(R.style.Theme_V2ex_big4)
            }
        }
        super.onCreate(savedInstanceState)
    }
}
