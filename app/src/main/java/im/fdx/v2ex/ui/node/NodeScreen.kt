package im.fdx.v2ex.ui.node

import im.fdx.v2ex.data.model.Node
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import im.fdx.v2ex.ui.main.TopicListScreen
import im.fdx.v2ex.ui.main.TopicListViewModel
import im.fdx.v2ex.data.model.Topic
import im.fdx.v2ex.R
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(
    nodeName: String,
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String?) -> Unit,
    onNewTopicClick: (String?) -> Unit,
    onLoginClick: () -> Unit
) {
    val viewModel: TopicListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val showAppBarTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = showAppBarTitle,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        Text(
                            text = uiState.node?.title ?: nodeName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                expandedHeight = 56.dp,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                    // Favorite Toggle
                    IconButton(onClick = { 
                        if (im.fdx.v2ex.utils.verifyLogin(
                                context = context,
                                snackbarHostState = snackbarHostState,
                                scope = coroutineScope,
                                actionLabel = "Login",
                                onLoginClick = onLoginClick
                            )) {
                            viewModel.favorNode()
                        }
                    }) {
                        Icon(
                            imageVector = if (uiState.isFavored) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavored) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // More Menu for Ignore
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(if (uiState.isIgnored) stringResource(R.string.already_ignore_node) else stringResource(R.string.ignore_node)) 
                                },
                                onClick = {
                                    showMenu = false
                                    if (im.fdx.v2ex.utils.verifyLogin(
                                            context = context,
                                            snackbarHostState = snackbarHostState,
                                            scope = coroutineScope,
                                            actionLabel = "Login",
                                            onLoginClick = onLoginClick
                                        )) {
                                        viewModel.ignoreNode()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val loginLabel = stringResource(id = R.string.login)
            im.fdx.v2ex.ui.common.PostTopicFAB(
                onClick = {
                    if (im.fdx.v2ex.utils.verifyLogin(
                            context = context,
                            snackbarHostState = snackbarHostState,
                            scope = coroutineScope,
                            actionLabel = loginLabel,
                            onLoginClick = onLoginClick
                        )) {
                        onNewTopicClick(nodeName)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            TopicListScreen(
                nodeName = nodeName,
                viewModel = viewModel,
                listState = listState,
                onTopicClick = onTopicClick,
                onMemberClick = onMemberClick, 
                onNodeClick = { /* Already on Node */ },
                contentPadding = PaddingValues(bottom = 88.dp),
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = node.avatarLargeUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.node_topic_count,
                            NumberFormat.getIntegerInstance().format(node.topics)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!node.header.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = node.header.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    }
}
