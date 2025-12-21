package im.fdx.v2ex.ui.member

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.foundation.text.selection.SelectionContainer
import coil.compose.AsyncImage
import im.fdx.v2ex.ui.main.TopicListScreen
import im.fdx.v2ex.ui.main.Topic
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import im.fdx.v2ex.utils.TimeUtil
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberScreen(
    username: String,
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String?) -> Unit = {},
    onNodeClick: (String?) -> Unit = {}
) {
    val viewModel: MemberViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(username) {
        viewModel.init(username)
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            // Member Header
            uiState.member?.let { member ->
                MemberHeader(member)
            }

            // Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Topics") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Replies") }
                )
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> TopicListScreen(
                        username = username,
                        onTopicClick = onTopicClick,
                        onMemberClick = onMemberClick,
                        onNodeClick = onNodeClick,
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    )
                    1 -> MemberRepliesList(
                        replies = uiState.replies,
                        onLoadMore = { viewModel.loadMoreReplies() },
                        isLoading = uiState.isRepliesLoading,
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}

@Composable
fun MemberHeader(member: Member) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
             AsyncImage(
                model = member.avatarLargeUrl,
                contentDescription = "Member Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = member.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (!member.tagline.isNullOrEmpty()) {
                    Text(text = member.tagline!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Text(text = "Joined ${TimeUtil.getAbsoluteTime(member.created.toLongOrNull()?:0L)}", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (!member.bio.isNullOrEmpty()) {
             Spacer(modifier = Modifier.height(12.dp))
             Text(text = member.bio!!, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MemberRepliesList(
    replies: List<MemberReplyModel>,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
     val listState = rememberLazyListState()
     
     // Infinite Scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                index != null && index >= replies.size - 2 && !isLoading && replies.isNotEmpty()
            }
            .collect {
                onLoadMore()
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        items(replies) { reply ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                     Text(text = "Replied to: ${reply.topic.title}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                     Spacer(modifier = Modifier.height(4.dp))
                     
                      SelectionContainer {
                          Text(
                              text = AnnotatedString.fromHtml(reply.content ?: ""),
                              style = MaterialTheme.typography.bodySmall
                          )
                      }
                     
                     Spacer(modifier = Modifier.height(4.dp))
                     Text(text = reply.createdOriginal, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            HorizontalDivider()
        }
        if(isLoading) {
             item { Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        }
    }
}
