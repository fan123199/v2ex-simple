package im.fdx.v2ex.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Removed legacy XML inflation
        // setContentView(binding.root)
        
        setContent {
            V2ExTheme {
                im.fdx.v2ex.ui.AppNavigation()
            }
        }
    }
}
