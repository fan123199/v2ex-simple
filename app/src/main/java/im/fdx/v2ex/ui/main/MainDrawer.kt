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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val userData = user.collectAsState(initial = null).value
    
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f)
    ) {
        // Header
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (userData != null) {
                        onItemClick("member:${userData.username}")
                    } else {
                        onItemClick("login")
                    }
                }
                .padding(16.dp, top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             if (userData != null) {
                 AsyncImage(
                    model = userData.avatar_normal,
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
                text = userData?.username ?: stringResource(R.string.drawer_click_to_login),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val themeModeStr = im.fdx.v2ex.pref.getString(Keys.PREF_THEME_MODE, "0") ?: "0"
            val themeMode = themeModeStr.toInt()
            var showMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val currentIcon = when (themeMode) {
                    1 -> Icons.Default.LightMode
                    2 -> Icons.Default.DarkMode
                    else -> Icons.Default.BrightnessAuto
                }
                
                val currentText = when (themeMode) {
                    1 -> stringResource(R.string.light_mode)
                    2 -> stringResource(R.string.dark_mode)
                    else -> stringResource(R.string.use_device_setting)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMenu = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = currentText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.light_mode)) },
                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                        onClick = {
                            im.fdx.v2ex.pref.edit().putString(Keys.PREF_THEME_MODE, "1").apply()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dark_mode)) },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        onClick = {
                            im.fdx.v2ex.pref.edit().putString(Keys.PREF_THEME_MODE, "2").apply()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.use_device_setting)) },
                        leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) },
                        onClick = {
                            im.fdx.v2ex.pref.edit().putString(Keys.PREF_THEME_MODE, "0").apply()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}



