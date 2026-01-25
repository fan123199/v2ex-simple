package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Res
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
// import im.fdx.v2ex.ui.daily.DailyActivity

import im.fdx.v2ex.utils.Keys
import androidx.core.content.ContextCompat.startActivity
import im.fdx.v2ex.utils.UserStore.user

@Composable
fun MainDrawer(
    onItemClick: (String) -> Unit
) {
    val context = LocalContext.current
    val user by user.collectAsState()
    
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f)
    ) {
        // Header
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (user != null) {
                        onItemClick("member:${user!!.username}")
                    } else {
                        onItemClick("login")
                    }
                }
                .padding(16.dp, top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             if (user != null) {
                 AsyncImage(
                    model = user!!.avatar_normal,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
             } else {
                 Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
             }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user?.username ?: stringResource(R.string.drawer_click_to_login),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_daily_check)) },
            icon = { Icon(painterResource(id = R.drawable.ic_daily_check), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { onItemClick("daily") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_all_nodes)) },
            icon = { Icon(painterResource(id = R.drawable.ic_all_node), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { onItemClick("all_nodes") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_favorites)) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { onItemClick("favorites") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_notifications)) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { onItemClick("notifications") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_settings)) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { onItemClick("settings") }
        )
    }
}



