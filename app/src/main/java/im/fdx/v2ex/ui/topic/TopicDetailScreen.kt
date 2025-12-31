package im.fdx.v2ex.ui.topic

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import im.fdx.v2ex.data.model.Reply
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import im.fdx.v2ex.data.network.NetManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


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
    val clipboardManager = LocalClipboard.current
    LaunchedEffect(topicId) {
        viewModel.init(topicId, initialTopic)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showMoreActions by remember { mutableStateOf(false) }

    val topicUrl = "${NetManager.HTTPS_V2EX_BASE}/t/$topicId"

    val showTitleInBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || (listState.firstVisibleItemScrollOffset > 100)
        }
    }

    var isInputFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Infinite Scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                index != null && index >= uiState.replies.size - 2 && !uiState.isLoading && !uiState.isEnd && uiState.replies.isNotEmpty()
            }
            .collect {
                viewModel.loadMore()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showTitleInBar) {
                        Text(
                            text = uiState.topic?.title ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreActions = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMoreActions,
                        onDismissRequest = { showMoreActions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (uiState.isFavored) "取消收藏" else "收藏") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.isFavored) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isFavored) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                viewModel.favorTopic()
                                showMoreActions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("感谢") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.isThanked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isThanked) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                viewModel.thankTopic()
                                showMoreActions = false
                            },
                            enabled = !uiState.isThanked
                        )
                        DropdownMenuItem(
                            text = { Text("忽略") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                viewModel.ignoreTopic()
                                showMoreActions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, topicUrl)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share topic"))
                                showMoreActions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("在浏览器打开") },
                            leadingIcon = { Icon(painterResource(id = R.drawable.ic_website), contentDescription = null) },
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(topicUrl)))
                                showMoreActions = false
                            }
                        )
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
                },
                focusRequester = focusRequester,
                onFocusChanged = { isInputFocused = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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

            // Dimming Overlay
            if (isInputFocused) {
               Box(
                   modifier = Modifier
                       .fillMaxSize()
                       .background(Color.Black.copy(alpha = 0.4f))
                       .clickable(
                           interactionSource = remember { MutableInteractionSource() },
                           indication = null
                       ) {
                           focusManager.clearFocus()
                       }
               )
            }
        }

        if (showReplySheet) {
            ModalBottomSheet(
                onDismissRequest = { showReplySheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    val scope = rememberCoroutineScope()
                    ListItem(
                        headlineContent = { Text("Reply") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                 replyText = "@${it.member?.username} "
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Copy content") },
                        leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                scope.launch {
                                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("reply", AnnotatedString(it.content))))
                                }
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Thank") },
                        leadingContent = {
                            Icon(
                                imageVector = if (selectedReply?.isThanked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (selectedReply?.isThanked == true) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedReply?.let { it ->
                                if (!it.isThanked) {
                                    viewModel.thankReply(it.id)
                                }
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
    onSend: (String) -> Unit,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    placeholder = { Text("Post a reply...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}






