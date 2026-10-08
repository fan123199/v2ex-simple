package im.fdx.v2ex.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/** Accept clicks only from the current, fully visible source screen. */
internal fun NavController.openTopic(source: NavBackStackEntry, topicId: String) {
    // The outgoing screen can still receive taps during its exit transition.
    // Check the captured source, not the newly pushed destination's lifecycle.
    if (currentBackStackEntry != source ||
        source.lifecycle.currentState != Lifecycle.State.RESUMED ||
        topicId.isBlank()
    ) {
        return
    }

    navigate(Screen.Topic.createRoute(topicId)) {
        launchSingleTop = true
    }
}
