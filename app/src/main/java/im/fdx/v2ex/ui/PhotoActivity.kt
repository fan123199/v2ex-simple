package im.fdx.v2ex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys

class PhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val list = intent.getStringArrayListExtra(Keys.KEY_PHOTO)
        val position = intent.getIntExtra(Keys.KEY_POSITION, 0)
        
        if (list == null) {
            finish()
            return
        }

        setContent {
            V2ExTheme {
                PhotoScreen(
                    photos = list,
                    initialPage = position,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
