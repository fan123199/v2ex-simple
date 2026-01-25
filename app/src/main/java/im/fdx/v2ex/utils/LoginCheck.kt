package im.fdx.v2ex.utils

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Utility to verify login state and show a Snackbar with a "Login" action if the user is not logged in.
 *
 * @param context The context for localized strings.
 * @param snackbarHostState The SnackbarHostState to show the reminder.
 * @param scope The CoroutineScope to launch the snackbar show action.
 * @param onLoginClick Callback when the user clicks the "Login" action in the Snackbar.
 * @return True if logged in, false otherwise.
 */
fun verifyLogin(
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    actionLabel: String,
    onLoginClick: () -> Unit
): Boolean {
    return if (myApp.isLogin) {
        true
    } else {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.not_login_tips),
                actionLabel = actionLabel,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                onLoginClick()
            }
        }
        false
    }
}
