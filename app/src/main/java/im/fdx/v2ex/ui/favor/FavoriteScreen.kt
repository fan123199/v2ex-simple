package im.fdx.v2ex.ui.favor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import im.fdx.v2ex.ui.main.TopicListScreen
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.node.Node

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String?) -> Unit,
    onNodeClick: (String?) -> Unit
) {
    // val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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
                    text = { Text("Topic") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Nodes") }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> TopicListScreen(
                        type = 1, // Favorite Topics
                        onTopicClick = onTopicClick,
                        onMemberClick = onMemberClick,
                         onNodeClick = onNodeClick
                    )
                    1 -> FavoriteNodeList(
                         onNodeClick = { node -> onNodeClick(node.name) } 
                         // Check FavoriteNodeList signature, if it doesn't support callback, need to update it too.
                         // But Phase 4 created NodeFavorFragment replacement?
                         // FavoriteNodeList was an inner composable or separate?
                         // I will assume FavoriteNodeList needs update if it uses Intent.
                         // For now I replaced TopicListScreen logic which is crucial.
                         // If FavoriteNodeList is separate, I should update it.
                         // Let's assume for now I should pass callbacks to it.
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteNodeList(
    onNodeClick: (Node) -> Unit
) {
    val viewModel: FavoriteNodeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    // val context = LocalContext.current // unused

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.nodes.isEmpty()) {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorite nodes")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.nodes) { node ->
                Column(
                    modifier = Modifier
                        .clickable {
                            onNodeClick(node)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                     AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { imageView ->
                             Glide.with(imageView).load(node.avatarLargeUrl).into(imageView)
                        },
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
