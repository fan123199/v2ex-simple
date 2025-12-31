package im.fdx.v2ex.ui.node

import im.fdx.v2ex.data.model.Node
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import im.fdx.v2ex.ui.main.TopicListScreen
import im.fdx.v2ex.ui.main.TopicListViewModel
import im.fdx.v2ex.data.model.Topic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(
    nodeName: String,
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit
) {
    val viewModel: TopicListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    // val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.node?.title ?: nodeName) },
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
        Box(modifier = Modifier.padding(innerPadding)) {
            TopicListScreen(
                nodeName = nodeName,
                viewModel = viewModel,
                onTopicClick = onTopicClick,
                onMemberClick = { /* Add onMemberClick if needed, or ignore for Node view */ }, 
                onNodeClick = { /* Already on Node */ },
                header = {
                    uiState.node?.let { node ->
                        NodeHeader(node)
                    }
                }
            )
        }
    }
}

@Composable
fun NodeHeader(node: Node) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = node.avatarLargeUrl,
                contentDescription = "Node Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Topics: ${node.topics}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!node.header.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = node.header ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}






