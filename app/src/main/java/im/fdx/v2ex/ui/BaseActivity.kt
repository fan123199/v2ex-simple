package im.fdx.v2ex.ui

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys

const val MODE_SYSTEM = 0 //跟随系统，采用SP方式
const val MODE_SMALL = 1
const val MODE_BIG2 = 2
const val MODE_BIG3 = 3
const val MODE_BIG4 = 4

abstract class BaseActivity : AppCompatActivity() {

    private val isSystemFont by lazy { pref.getString(Keys.PREF_TEXT_SIZE, MODE_SYSTEM.toString())!!.toInt() == MODE_SYSTEM }

    override fun onCreate(savedInstanceState: Bundle?) {
        val textSizeMode = pref.getString(Keys.PREF_TEXT_SIZE, MODE_SYSTEM.toString())!!.toInt()
        when (textSizeMode) {
            MODE_SYSTEM, MODE_SMALL -> {
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (!isSystemFont) {
            if (newConfig.fontScale != 1f) //非默认值
                resources
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources? {
        val res: Resources = super.getResources()
        if (!isSystemFont) {
            if (res.configuration.fontScale != 1f) { //非默认值
                val newConfig = Configuration()
                newConfig.setToDefaults() //设置默认
                res.updateConfiguration(newConfig, res.displayMetrics)
            }
        }
        return res
    }
}
