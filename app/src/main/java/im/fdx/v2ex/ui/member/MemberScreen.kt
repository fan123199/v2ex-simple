package im.fdx.v2ex.ui.member

import im.fdx.v2ex.data.model.Reply
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
import im.fdx.v2ex.data.model.Topic
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import im.fdx.v2ex.utils.TimeUtil
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier.Companion
import im.fdx.v2ex.data.model.Member
import im.fdx.v2ex.data.model.MemberReplyModel
import im.fdx.v2ex.R
import androidx.compose.ui.res.painterResource

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFollow() }) {
                        Icon(
                            painter = painterResource(id = if (uiState.isFollowed) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp),
                            contentDescription = if (uiState.isFollowed) "Unfollow" else "Follow",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.toggleBlock() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_block_primary_24dp),
                            contentDescription = if (uiState.isBlocked) "Unblock" else "Block",
                            tint = if (uiState.isBlocked) Color.Red else MaterialTheme.colorScheme.primary
                        )
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
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(pagerState.currentPage)
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
                        onTopicClick = onTopicClick,
                        onLoadMore = { viewModel.loadMoreReplies() },
                        isLoading = uiState.isRepliesLoading,
                        isEnd = uiState.isRepliesEnd,
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
                Text(
                    text = member.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (!member.tagline.isNullOrEmpty()) {
                    Text(
                        text = member.tagline!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (member.id.isNotEmpty()) {
                    Text(
                        text = "V2EX 第 ${member.id} 号会员",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (member.created.isNotEmpty()) {
                    Text(
                        text = "加入于 ${member.created}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        if (!member.bio.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = member.bio!!, style = MaterialTheme.typography.bodyMedium)
        }

        // Social Links
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            member.website?.let {
                SocialIcon(iconId = R.drawable.ic_website, url = it)
            }
            member.twitter?.let {
                SocialIcon(iconId = R.drawable.ic_twitter, url = it)
            }
            member.github?.let {
                SocialIcon(iconId = R.drawable.ic_github, url = it)
            }
            member.location?.let {
                SocialIcon(iconId = R.drawable.ic_location, label = it)
            }
        }
    }
}

@Composable
fun SocialIcon(iconId: Int, url: String? = null, label: String? = null) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
        url?.let {
             try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                context.startActivity(intent)
            } catch (e: Exception) {}
        }
    }.padding(4.dp)) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        if (!label.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun MemberRepliesList(
    replies: List<MemberReplyModel>,
    onTopicClick: (Topic) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    isEnd: Boolean,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
     val listState = rememberLazyListState()
     
     // Infinite Scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                index != null && index >= replies.size - 2 && !isLoading && !isEnd && replies.isNotEmpty()
            }
            .collect {
                onLoadMore()
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        items(replies) { reply ->
            Card(
                onClick = { onTopicClick(reply.topic) },
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





