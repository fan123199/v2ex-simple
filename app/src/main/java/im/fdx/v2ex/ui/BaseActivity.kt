package im.fdx.v2ex.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys


const val MODE_SMALL = 0
const val MODE_BIGGER = 1
const val MODE_BIGGEST = 2

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textSizeMode = pref.getString(Keys.PREF_TEXT_SIZE, "0")!!.toInt()
        when (textSizeMode) {
            MODE_SMALL -> {
                setTheme(R.style.Theme_V2ex)
            }
            MODE_BIGGER -> {
                setTheme(R.style.Theme_V2ex_bigger)
            }
            MODE_BIGGEST -> {
                setTheme(R.style.Theme_V2ex_biggest)
            }
        }
    }

}
