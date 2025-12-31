package im.fdx.v2ex.ui.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import im.fdx.v2ex.data.model.Reply
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
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
    var quotedReply by remember { mutableStateOf<Reply?>(null) }
    var showQuoteDialog by remember { mutableStateOf(false) }
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
        Column(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
        ) {
            if (uiState.isLoading && uiState.page == 1) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
              LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Topic Content) - styled like TopicItem
                item {
                    uiState.topic?.let { topic ->
                         Column(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                         ) {
                             // Title row with reply count (unlimited lines)
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.SpaceBetween,
                                 verticalAlignment = Alignment.Top
                             ) {
                                 Text(
                                     text = topic.title,
                                     style = MaterialTheme.typography.titleMedium,
                                     color = MaterialTheme.colorScheme.onSurface,
                                     modifier = Modifier.weight(1f).padding(end = 8.dp)
                                 )

                                 if ((topic.replies ?: 0) > 0) {
                                     Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 4.dp)
                                     ) {
                                         Icon(
                                             painter = painterResource(id = R.drawable.ic_message_black_24dp),
                                             contentDescription = null,
                                             modifier = Modifier.size(14.dp),
                                             tint = MaterialTheme.colorScheme.onSurfaceVariant
                                         )
                                         Spacer(modifier = Modifier.width(2.dp))
                                         Text(
                                             text = topic.replies.toString(),
                                             style = MaterialTheme.typography.labelSmall,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant
                                         )
                                     }
                                 }
                             }

                             Spacer(modifier = Modifier.height(8.dp))

                             // Author info row (avatar, username, time, node)
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 // Avatar
                                 AsyncImage(
                                     model = topic.member?.avatarNormalUrl,
                                     contentDescription = "Avatar",
                                     contentScale = ContentScale.Crop,
                                     modifier = Modifier
                                         .size(24.dp)
                                         .clip(CircleShape)
                                         .clickable { onMemberClick(topic.member?.username ?: "") }
                                 )

                                 Spacer(modifier = Modifier.width(8.dp))

                                 Text(
                                     text = topic.member?.username ?: "",
                                     style = MaterialTheme.typography.labelSmall,
                                     color = MaterialTheme.colorScheme.secondary,
                                     modifier = Modifier.clickable { onMemberClick(topic.member?.username ?: "") }
                                 )

                                 Spacer(modifier = Modifier.width(8.dp))

                                 Text(
                                     text = topic.showCreated(),
                                     style = MaterialTheme.typography.labelSmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )

                                 Spacer(modifier = Modifier.weight(1f))

                                 if (!topic.node?.title.isNullOrEmpty()) {
                                     Text(
                                         text = topic.node?.title ?: "",
                                         style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.tertiary,
                                         modifier = Modifier
                                             .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                             .padding(horizontal = 4.dp, vertical = 2.dp)
                                     )
                                 }
                             }

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
                            selectedReply = it
                            showReplySheet = true
                        },
                        onThankClick = { /* TODO */ },
                        onLongClick = {
                            selectedReply = it
                            showReplySheet = true
                        },
                        onQuoteClick = { username, replyNum ->
                            // Find the quoted reply by username and reply number
                            val foundReply = uiState.replies.find { r ->
                                r.member?.username == username && r.getRowNum() == replyNum
                            }
                            if (foundReply != null) {
                                quotedReply = foundReply
                                showQuoteDialog = true
                            }
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
        
        // Quote Dialog to show referenced reply
        if (showQuoteDialog && quotedReply != null) {
            AlertDialog(
                onDismissRequest = { showQuoteDialog = false },
                title = {
                    Text(text = "@${ quotedReply?.member?.username} #${quotedReply?.getRowNum()}")
                },
                text = {
                    SelectionContainer {
                        Text(
                            text = AnnotatedString.fromHtml(quotedReply?.content_rendered ?: ""),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQuoteDialog = false }) {
                        Text("关闭")
                    }
                }
            )
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






