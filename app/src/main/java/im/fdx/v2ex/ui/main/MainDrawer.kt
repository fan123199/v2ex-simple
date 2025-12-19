package im.fdx.v2ex.ui.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.bumptech.glide.Glide
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
// import im.fdx.v2ex.ui.daily.DailyActivity
import im.fdx.v2ex.ui.node.AllNodesActivity
import im.fdx.v2ex.ui.SettingsActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.utils.Keys
import androidx.core.content.ContextCompat.startActivity

@Composable
fun MainDrawer(
    onItemClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isLoggedIn = myApp.isLogin
    // Placeholder for user info - would typically come from a UserViewModel or globally accessible user object
    
    ModalDrawerSheet {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.primaryContainer), // Or custom image
            contentAlignment = Alignment.CenterStart
        ) {
             // Background Image if needed
             
             Column(modifier = Modifier.padding(16.dp)) {
                 AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        // Typically load avatar here if logged in
                        // Glide.with(imageView).load(avatarUrl).into(imageView)
                        imageView.setImageResource(R.drawable.ic_profile) 
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { 
                            if (isLoggedIn) {
                                // Navigate to profile
                            } else {
                                // Navigate to Login
                            }
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if(isLoggedIn) "User" else "Click to Login", // placeholder
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
             }
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Daily Check") },
            icon = { Icon(painterResource(id = R.drawable.ic_daily_check), contentDescription = null) },
            selected = false,
            onClick = { onItemClick("daily") }
        )
        NavigationDrawerItem(
            label = { Text("All Nodes") },
            icon = { Icon(painterResource(id = R.drawable.ic_all_node), contentDescription = null) },
            selected = false,
            onClick = { onItemClick("all_nodes") }
        )
        NavigationDrawerItem(
            label = { Text("Favorites") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("favorites") }
        )
        NavigationDrawerItem(
            label = { Text("Notifications") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("notifications") }
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick = { onItemClick("settings") }
        )
    }
}
