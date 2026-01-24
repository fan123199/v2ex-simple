package im.fdx.v2ex.ui.notification

import im.fdx.v2ex.data.model.Topic
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import im.fdx.v2ex.R
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import im.fdx.v2ex.data.model.NotificationModel
import im.fdx.v2ex.ui.main.TopicListScreen
import im.fdx.v2ex.utils.Keys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onMemberClick: (String) -> Unit
) {
    val viewModel: NotificationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    // val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
               items(uiState.notifications) { notification ->
                   NotificationItem(
                       notification, 
                       onMemberClick = onMemberClick
                   ) {
                       // Handle click
                        if (notification.topic?.id?.isNotEmpty() == true) {
                            onTopicClick(notification.topic!!.id)
                        } else if (notification.member?.username?.isNotEmpty() == true) {
                            onMemberClick(notification.member!!.username)
                        }
                   }
                   HorizontalDivider()
               }
            }
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationModel,
    onMemberClick: (String) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
         AsyncImage(
            model = notification.member?.avatar_normal,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    notification.member?.username?.let {
                        onMemberClick(it)
                    }
                }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
             Row {
                 Text(text = notification.member?.username ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                 Spacer(modifier = Modifier.width(4.dp))
                 Text(text = notification.time ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
             }
             Spacer(modifier = Modifier.height(4.dp))
             Text(text = "${notification.type ?: ""} ${notification.topic?.title ?: ""}", style = MaterialTheme.typography.bodyMedium)
             if(notification.content?.isNotEmpty() == true) {
                 Spacer(modifier = Modifier.height(4.dp))
                 Text(text = notification.content!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
             }
        }
    }
}





