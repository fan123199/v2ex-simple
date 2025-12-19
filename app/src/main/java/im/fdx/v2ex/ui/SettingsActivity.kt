package im.fdx.v2ex.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.settings.SettingsScreen
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys.PREF_NIGHT_MODE

val isUsePageNum get() = pref.getBoolean("pref_page_num", false)

class SettingsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        V2ExTheme {
            SettingsScreen(
                onBackClick = { finish() },
                onTabSettingClick = { startActivity(android.content.Intent(this@SettingsActivity, TabSettingActivity::class.java)) }
            )
        }
    }
  }
}

