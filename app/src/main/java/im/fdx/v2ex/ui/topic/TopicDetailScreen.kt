package im.fdx.v2ex.ui.topic

import androidx.compose.foundation.clickable
import im.fdx.v2ex.data.model.Reply
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.data.model.Topic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.fromHtml
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.main.TopicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    initialTopic: Topic?,
    viewModel: TopicDetailViewModel = viewModel(),
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var selectedReply by remember { mutableStateOf<Reply?>(null) }
    var showReplySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val clipboardManager = LocalClipboardManager.current
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
                text = replyText,
                onTextChange = { replyText = it },
                onSend = { content ->
                    viewModel.postReply(content)
                }
            )
        }
    ) { innerPadding ->
        
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
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
                             SelectionContainer {
                                 Text(
                                     text = AnnotatedString.fromHtml(topic.content_rendered ?: ""),
                                     style = MaterialTheme.typography.bodyLarge
                                 )
                             }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                         }
                    }
                }

                // Replies
                items(uiState.replies) { reply ->
                    ReplyItem(
                        reply = reply,
                        onMemberClick = { onMemberClick(it ?: "") },
                        onReplyClick = {
                            replyText = "@${it.member?.username} "
                        },
                        onThankClick = { /* TODO */ },
                        onLongClick = {
                            selectedReply = it
                            showReplySheet = true
                        }
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

        if (showReplySheet) {
            ModalBottomSheet(
                onDismissRequest = { showReplySheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Reply") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                 replyText = "@${it.member?.username} "
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Copy") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                clipboardManager.setText(AnnotatedString(it.content))
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Thank") },
                        leadingContent = { Icon(painterResource(id = R.drawable.ic_thank), contentDescription = null) },
                        modifier = Modifier.clickable {
                            selectedReply?.let { it ->
                                viewModel.thankReply(it.id)
                            }
                            showReplySheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomReplyInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
             TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Post a reply...") },
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if (text.isNotBlank()) {
                        onSend(text)
                        onTextChange("")
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}





