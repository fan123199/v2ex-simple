package im.fdx.v2ex.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import im.fdx.v2ex.R
import org.jetbrains.anko.defaultSharedPreferences


const val MODE_SMALL = 0
const val MODE_BIGGER = 1
const val MODE_BIGGEST = 2

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var textSizeMode = defaultSharedPreferences.getInt("text_size_mode", MODE_SMALL)
        textSizeMode = 1
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
