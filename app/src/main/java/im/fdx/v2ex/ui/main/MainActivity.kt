package im.fdx.v2ex.ui.main

import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = androidx.navigation.compose.rememberNavController()
            val intentState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(intent) }

            androidx.compose.runtime.DisposableEffect(Unit) {
                val listener = androidx.core.util.Consumer<android.content.Intent> { newIntent ->
                    intentState.value = newIntent
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            V2ExTheme {
                im.fdx.v2ex.ui.AppNavigation(navController, intentState.value)
            }
        }
    }
}
