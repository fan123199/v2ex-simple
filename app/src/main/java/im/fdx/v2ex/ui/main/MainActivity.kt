package im.fdx.v2ex.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = androidx.navigation.compose.rememberNavController()
            
            // Handle new intents
            androidx.compose.runtime.DisposableEffect(Unit) {
                val listener = androidx.core.util.Consumer<android.content.Intent> { intent ->
                     handleIntent(intent, navController)
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
            
            // Handle initial intent
             androidx.compose.runtime.LaunchedEffect(Unit) {
                 handleIntent(intent, navController)
             }

            V2ExTheme {
                im.fdx.v2ex.ui.AppNavigation(navController)
            }
        }
    }

    private fun handleIntent(intent: android.content.Intent, navController: androidx.navigation.NavController) {
         if (intent.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
             val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
             val title = intent.getStringExtra(android.content.Intent.EXTRA_TITLE)
             if (sharedText != null) {
                 navController.navigate(im.fdx.v2ex.ui.Screen.NewTopic.createRoute(title = title, content = sharedText))
             }
         } else {
             navController.handleDeepLink(intent)
         }
    }
}
