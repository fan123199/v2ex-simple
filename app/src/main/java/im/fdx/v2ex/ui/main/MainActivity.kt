package im.fdx.v2ex.ui.main

import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.crashlytics.FirebaseCrashlytics
import im.fdx.v2ex.ui.BaseActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import android.content.Intent
import im.fdx.v2ex.ui.navigation.AppNavigation

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = rememberNavController()
            val intentState = remember { mutableStateOf(intent) }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    intentState.value = newIntent
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            V2ExTheme {
                AppNavigation(navController, intentState.value)
            }
        }
    }
}


