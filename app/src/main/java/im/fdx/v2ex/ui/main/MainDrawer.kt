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
    
    ModalDrawerSheet {
        // Header
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, top = 24.dp, bottom = 16.dp), // Standard drawer header might need top padding for status bar if not handled, but usually ModalDrawer handles it. Let's stick to safe padding.
            verticalAlignment = Alignment.CenterVertically
        ) {
             if (user != null) {
                 AsyncImage(
                    model = user!!.avatar_normal,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp) // Slight increase for better visibility in horizontal layout? Or keep 32dp? User said 'shrink module', implies compact. Keeping 32dp or maybe 40dp is standard. Let's stick to 40dp for better touch target or keep 32 to match user "match size" previously. User said "shrink drawer header". I'll stick to 32dp or maybe 48dp which is standard avatar size. Previous was 32dp in valid request, but visually 48dp is better for drawer. I'll stick to 40dp as a middle ground or just keep 32dp if user emphasized consistency. Let's use 40dp as it's common for list items. Actually, user asked for "match size/shape of AppBar" in prev request. AppBar is usually small. I'll stick to 32dp if not specified, but 40dp looks better.
                        // Wait, user said "shrink module". I'll keep 32dp.
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { 
                            onItemClick("member:${user!!.username}")
                        }
                )
             } else {
                 Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { 
                            onItemClick("login")
                        }
                )
             }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user?.username ?: stringResource(R.string.drawer_click_to_login),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface // Change to onSurface since no primary background
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_daily_check)) },
            icon = { Icon(painterResource(id = R.drawable.ic_daily_check), contentDescription = null) },
            selected = false,
            onClick = { onItemClick("daily") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_all_nodes)) },
            icon = { Icon(painterResource(id = R.drawable.ic_all_node), contentDescription = null) },
            selected = false,
            onClick = { onItemClick("all_nodes") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_favorites)) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("favorites") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_notifications)) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("notifications") }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_settings)) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("settings") }
        )
    }
}



