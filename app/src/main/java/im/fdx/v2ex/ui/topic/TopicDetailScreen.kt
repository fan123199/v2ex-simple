package im.fdx.v2ex.ui.topic

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import im.fdx.v2ex.data.network.NetManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange



@Composable
fun TopicDetailScreen(
    topicId: String,
    initialTopic: Topic?,
    onBackClick: () -> Unit,
    onMemberClick: (String?) -> Unit,
    onReportClick: (String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    val enableSwipe = im.fdx.v2ex.pref.getBoolean("pref_viewpager", false)
    val topicList = TopicListStore.currentTopics
    val initialIndex = topicList.indexOfFirst { it.id == topicId }

    if (enableSwipe && initialIndex != -1) {
        val pagerState = rememberPagerState(initialPage = initialIndex) { topicList.size }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val topic = topicList[page]
            TopicDetailContent(
                topicId = topic.id,
                initialTopic = topic,
                onBackClick = onBackClick,
                onMemberClick = onMemberClick,
                onReportClick = onReportClick,
                onLoginClick = onLoginClick
            )
        }
    } else {
        TopicDetailContent(
            topicId = topicId,
            initialTopic = initialTopic,
            onBackClick = onBackClick,
            onMemberClick = onMemberClick,
            onReportClick = onReportClick,
            onLoginClick = onLoginClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailContent(
    topicId: String,
    initialTopic: Topic?,
    onBackClick: () -> Unit,
    onMemberClick: (String?) -> Unit,
    onReportClick: (String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    val viewModel: TopicDetailViewModel = viewModel(key = "topic_$topicId")
    var replyText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedReply by remember { mutableStateOf<Reply?>(null) }
    var showReplySheet by remember { mutableStateOf(false) }
    var showUserRepliesSheet by remember { mutableStateOf(false) }
    var quotedReply by remember { mutableStateOf<Reply?>(null) }
    var clickOffset by remember { mutableStateOf(Offset.Zero) }
    var showQuoteDialog by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }
    var showThankDialog by remember { mutableStateOf(false) }
    var replyToThank by remember { mutableStateOf<Reply?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val reportSheetState = rememberModalBottomSheetState()
    val clipboardManager = LocalClipboard.current
    LaunchedEffect(topicId) {
        viewModel.init(topicId, initialTopic)
    }

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val loginActionStr = stringResource(R.string.login)
    
    val toastPostReplySuccess = stringResource(R.string.toast_post_reply_success)
    val toastFavorSuccess = stringResource(R.string.toast_favor_success)
    val toastUnfavorSuccess = stringResource(R.string.toast_unfavor_success)
    val toastThankTopicSuccess = stringResource(R.string.toast_thank_topic_success)
    val toastIgnoreTopicSuccess = stringResource(R.string.toast_ignore_topic_success)
    val toastIgnoreReplySuccess = stringResource(R.string.toast_ignore_reply_success)
    val toastThankReplySuccess = stringResource(R.string.toast_thank_reply_success)
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                TopicDetailEvent.PostReplySuccess -> toastPostReplySuccess
                TopicDetailEvent.FavorTopicSuccess -> toastFavorSuccess
                TopicDetailEvent.UnfavorTopicSuccess -> toastUnfavorSuccess
                TopicDetailEvent.ThankTopicSuccess -> toastThankTopicSuccess
                TopicDetailEvent.IgnoreTopicSuccess -> toastIgnoreTopicSuccess
                TopicDetailEvent.IgnoreReplySuccess -> toastIgnoreReplySuccess
                TopicDetailEvent.ThankReplySuccess -> toastThankReplySuccess
                is TopicDetailEvent.RequestReportTopic -> null
                is TopicDetailEvent.RequestReportReply -> null
                is TopicDetailEvent.ShowErrorMessage -> event.error
            }
            if (message != null) {
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

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
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreActions = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreActions,
                        onDismissRequest = { showMoreActions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (uiState.isFavored) stringResource(R.string.unFavor) else stringResource(R.string.favor)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.isFavored) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isFavored) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                if (im.fdx.v2ex.utils.verifyLogin(
                                        context,
                                        snackbarHostState,
                                        scope,
                                        loginActionStr,
                                        onLoginClick = onLoginClick
                                    )
                                ) {
                                    viewModel.favorTopic()
                                }
                                showMoreActions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.thanks)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.isThanked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isThanked) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                if (im.fdx.v2ex.utils.verifyLogin(
                                        context,
                                        snackbarHostState,
                                        scope,
                                        loginActionStr,
                                        onLoginClick = onLoginClick
                                    )
                                ) {
                                    viewModel.thankTopic()
                                }
                                showMoreActions = false
                            },
                            enabled = !uiState.isThanked
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ignore)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                if (im.fdx.v2ex.utils.verifyLogin(
                                        context,
                                        snackbarHostState,
                                        scope,
                                        loginActionStr,
                                        onLoginClick = onLoginClick
                                    )
                                ) {
                                    viewModel.ignoreTopic()
                                }
                                showMoreActions = false
                            }
                        )

                        val shareToStr = stringResource(R.string.share_to)
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_share)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, topicUrl)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        shareToStr
                                    )
                                )
                                showMoreActions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_open_in_browser)) },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_website),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(topicUrl)))
                                showMoreActions = false
                            }
                        )
                        val str3 = stringResource(R.string.not_login_tips)
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.report_abuse)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMoreActions = false
                                if (!im.fdx.v2ex.myApp.isLogin) {
                                    android.widget.Toast.makeText(
                                        context,
                                        str3,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showReportSheet = true
                                }
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomReplyInput(
                value = replyText,
                onValueChange = { replyText = it },
                onSend = { content ->
                    if (im.fdx.v2ex.utils.verifyLogin(context, snackbarHostState, scope,
                            actionLabel = loginActionStr,
                            onLoginClick = onLoginClick)) {


                        viewModel.postReply(content)
                    }
                },
                focusRequester = focusRequester,
                onFocusChanged = { isInputFocused = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.filterType != FilterType.None) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.clearFilter() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.filterType == FilterType.User) Icons.Default.Person else Icons.Default.Forum,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (uiState.filterType) {
                                                FilterType.User -> stringResource(R.string.filtering_by_user, uiState.filterTarget ?: "")
                                                FilterType.Conversation -> stringResource(R.string.viewing_conversation)
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = stringResource(R.string.close),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
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
                                            contentDescription = stringResource(R.string.it_is_avatar),
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
                                            modifier = Modifier.clickable {
                                                onMemberClick(
                                                    topic.member?.username ?: ""
                                                )
                                            }
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
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        RoundedCornerShape(4.dp)
                                                    )
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
                                    val text = "@${it.member?.username} "
                                    replyText = TextFieldValue(
                                        text = text,
                                        selection = TextRange(text.length)
                                    )
                                    focusRequester.requestFocus()
                                },
                                onThankClick = {
                                    if (im.fdx.v2ex.utils.verifyLogin(
                                            context,
                                            snackbarHostState,
                                            scope,
                                            actionLabel = loginActionStr,
                                            onLoginClick = onLoginClick
                                        )
                                    ) {
                                        if (!it.isThanked) {
                                            replyToThank = it
                                            showThankDialog = true
                                        }
                                    }
                                },
                                onLongClick = {
                                    selectedReply = it
                                    showReplySheet = true
                                },
                                onQuoteClick = { username, replyNum, offset ->
                                    // Find the quoted reply by username and reply number
                                    var foundReply = viewModel.findReplyByFloor(username, replyNum)
                                    if (foundReply == null) {
                                        foundReply = viewModel.findRecentReplyByUsername(username, reply.id)
                                    }

                                    if (foundReply != null) {
                                        quotedReply = foundReply
                                        clickOffset = offset
                                        showQuoteDialog = true
                                    } else {
                                        onMemberClick(username)
                                    }
                                },
                                onMentionClick = { username, offset ->
                                    val foundReply = viewModel.findRecentReplyByUsername(username, reply.id)
                                    if (foundReply != null) {
                                        quotedReply = foundReply
                                        clickOffset = offset
                                        showQuoteDialog = true
                                    } else {
                                        onMemberClick(username)
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

            if (uiState.isLoading && uiState.page == 1) {
                LinearProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = innerPadding.calculateTopPadding())
                            .align(Alignment.TopCenter)
                )
            }

            // Dimming Overlay
            if (isImeVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
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
                    val reply = selectedReply
                    val reportReplyTitle = reply?.let { stringResource(R.string.report_reply_title, it.content?.take(20) ?: "") } ?: ""
                    val reportReplyContent = reply?.let { stringResource(R.string.report_reply_content, it.id, it.member?.username ?: "", topicUrl) } ?: ""
                    
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.reply)) },
                        leadingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                val text = "@${it.member?.username} "
                                replyText = TextFieldValue(
                                    text = text,
                                    selection = TextRange(text.length)
                                )
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.copy_content)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedReply?.let {
                                scope.launch {
                                    clipboardManager.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                "reply",
                                                AnnotatedString(it.content)
                                            )
                                        )
                                    )
                                }
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.thanks)) },
                        leadingContent = {
                            Icon(
                                imageVector = if (selectedReply?.isThanked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (selectedReply?.isThanked == true) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            if (im.fdx.v2ex.utils.verifyLogin(
                                    context,
                                    snackbarHostState,
                                    scope,
                                    actionLabel = loginActionStr,
                                    onLoginClick = onLoginClick
                                )
                            ) {
                                selectedReply?.let { it ->
                                    if (!it.isThanked) {
                                        viewModel.thankReply(it.id)
                                    }
                                }
                            }
                            showReplySheet = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.hide_reply)) },
                        leadingContent = {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                if (im.fdx.v2ex.utils.verifyLogin(
                                        context,
                                        snackbarHostState,
                                        scope,
                                        actionLabel = loginActionStr,
                                        onLoginClick = onLoginClick
                                    )
                                ) {
                                    selectedReply?.let { viewModel.ignoreReply(it.id) }
                                }
                                showReplySheet = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.show_user_all_reply)) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedReply?.let {
                                    viewModel.filterByUser(it.member?.username ?: "")
                                    showUserRepliesSheet = true
                                }
                                showReplySheet = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.show_conversation)) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedReply?.let {
                                    viewModel.showConversation(it.id)
                                    showUserRepliesSheet = true
                                }
                                showReplySheet = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.report_abuse)) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Report,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                showReplySheet = false
                                onReportClick(reportReplyTitle, reportReplyContent)
                            }
                        )
                    }
                }
            }

            if (showReportSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showReportSheet = false },
                    sheetState = reportSheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(bottom = 32.dp)) {
                        Text(
                            text = stringResource(R.string.report_reason_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        val reasons = stringArrayResource(R.array.report_reasons)
                        reasons.forEach { reason ->
                            val reportTitle = stringResource(R.string.report_topic_title, uiState.topic?.title ?: "")
                            val reportContent = stringResource(R.string.report_topic_content, topicUrl, uiState.topic?.member?.username ?: "", reason)
                            ListItem(
                                headlineContent = { Text(reason) },
                                modifier = Modifier.clickable {
                                    showReportSheet = false
                                     onReportClick(reportTitle, reportContent)
                                 }
                             )
                         }
                     }
                 }
             }

             if (showUserRepliesSheet) {
                 ModalBottomSheet(
                     onDismissRequest = { 
                         showUserRepliesSheet = false
                         viewModel.clearFilter()
                     },
                     sheetState = rememberModalBottomSheetState(),
                     containerColor = MaterialTheme.colorScheme.surface,
                     tonalElevation = 0.dp
                 ) {
                     Column(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(bottom = 32.dp)
                     ) {
                         Text(
                             text = if (uiState.filterType == FilterType.User) 
                                 stringResource(R.string.all_replies_of_user, uiState.filterTarget ?: "")
                             else if (uiState.filterType == FilterType.Conversation)
                                 stringResource(R.string.viewing_conversation)
                             else "",
                             style = MaterialTheme.typography.titleMedium,
                             modifier = Modifier.padding(16.dp)
                         )
                         
                         LazyColumn(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .weight(1f, fill = false)
                         ) {
                             items(uiState.filteredReplies) { reply ->
                                 ReplyItem(
                                     reply = reply,
                                     onMemberClick = { onMemberClick(it ?: "") },
                                     onReplyClick = {
                                         val text = "@${it.member?.username} "
                                         replyText = TextFieldValue(
                                             text = text,
                                             selection = TextRange(text.length)
                                         )
                                         showUserRepliesSheet = false
                                         viewModel.clearFilter()
                                         focusRequester.requestFocus()
                                     },
                                     onThankClick = {
                                         if (im.fdx.v2ex.utils.verifyLogin(
                                                 context,
                                                 snackbarHostState,
                                                 scope,
                                                 actionLabel = loginActionStr,
                                                 onLoginClick = onLoginClick
                                             )
                                         ) {
                                             if (!it.isThanked) {
                                                 replyToThank = it
                                                 showThankDialog = true
                                             }
                                         }
                                     },
                                     onLongClick = {
                                         selectedReply = it
                                         showReplySheet = true
                                     },
                                     onQuoteClick = { _, _, _ -> },
                                     onMentionClick = { _, _ -> }
                                 )
                                 HorizontalDivider()
                             }
                         }
                     }
                 }
             }

            // Quote Dialog to show referenced reply
            if (showQuoteDialog && quotedReply != null) {
                Popup(
                    onDismissRequest = { showQuoteDialog = false },
                    offset = IntOffset(
                        clickOffset.x.toInt(),
                        clickOffset.y.toInt()
                    ),
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        visible = true
                    }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(16.dp)
                                .clickable {
                                    val targetReply = quotedReply
                                    showQuoteDialog = false
                                    if (targetReply != null) {
                                        val index = uiState.replies.indexOf(targetReply)
                                        if (index != -1) {
                                            scope.launch {
                                                listState.animateScrollToItem(index + 1)
                                            }
                                        }
                                    }
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (quotedReply?.getRowNum() ?: 0 > 0) "@${quotedReply?.member?.username} #${quotedReply?.getRowNum()}" else "@${quotedReply?.member?.username}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = AnnotatedString.fromHtml(quotedReply?.content_rendered ?: ""),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }


@Composable
fun BottomReplyInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    placeholder = { Text(stringResource(R.string.post_reply_hint)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (value.text.isNotBlank()) {
                            onSend(value.text)
                            onValueChange(TextFieldValue(""))
                        }
                    },
                    enabled = value.text.isNotBlank()
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






