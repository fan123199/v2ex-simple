package im.fdx.v2ex.ui.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.ui.main.Topic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import im.fdx.v2ex.ui.main.TopicItem
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    initialTopic: Topic?,
    viewModel: TopicDetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(topicId) {
        viewModel.init(topicId, initialTopic)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Topic Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomReplyInput(
                onSend = { content ->
                     viewModel.postReply(content)
                }
            )
        }
    ) { innerPadding ->
        
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(innerPadding)
        ) {
              LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Topic Content)
                item {
                    uiState.topic?.let { topic ->
                         Column(modifier = Modifier.padding(16.dp)) {
                             Text(text = topic.title, style = MaterialTheme.typography.titleLarge)
                             Spacer(modifier = Modifier.height(8.dp))
                             // Author info row ... (Can reuse parts of TopicItem or make new Header Composable)
                             Text(text = "By ${topic.member?.username} at ${topic.showCreated()}", style = MaterialTheme.typography.labelMedium)
                             Spacer(modifier = Modifier.height(16.dp))
                             
                             // Topic Content
                             AndroidView(
                                factory = { context ->
                                    TextView(context).apply {
                                       setTextIsSelectable(true)
                                       textSize = 16f
                                       setTextColor(android.graphics.Color.BLACK) 
                                    }
                                },
                                update = { textView ->
                                     topic.content_rendered?.let {
                                         textView.text = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                                     }
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                         }
                    }
                }

                // Replies
                items(uiState.replies) { reply ->
                    ReplyItem(
                        reply = reply,
                        onMemberClick = {},
                        onReplyClick = {},
                        onThankClick = {}
                    )
                }
                
                if (uiState.isLoading && uiState.page > 1) {
                     item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomReplyInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
             TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Post a reply...") },
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
