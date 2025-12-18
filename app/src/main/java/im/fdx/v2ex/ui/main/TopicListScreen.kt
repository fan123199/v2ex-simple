package im.fdx.v2ex.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicListScreen(
    tab: String? = null,
    type: Int? = null,
    username: String? = null,
    nodeName: String? = null,
    viewModel: TopicListViewModel = viewModel(),
    onTopicClick: (Topic) -> Unit,
                onMemberClick: (String?) -> Unit,
    onNodeClick: (String?) -> Unit,
    header: (@Composable () -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        viewModel.init(tab, type, username, nodeName)
    }

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Infinite Scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                index != null && index >= uiState.topics.size - 2 && !uiState.isLoading && uiState.topics.isNotEmpty()
            }
            .collect {
                viewModel.loadMore()
            }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading && uiState.page == 1,
        onRefresh = { viewModel.refresh() }
    ) {
        if (uiState.topics.isEmpty() && !uiState.isLoading && header == null) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("暂无内容")
             }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (header != null) {
                    item { header() }
                }
                items(uiState.topics, key = { it.id }) { topic ->
                    TopicItem(
                        topic = topic,
                        onClick = { onTopicClick(topic) },
                        onNodeClick = onNodeClick,
                        onMemberClick = onMemberClick
                    )
                }

                if (uiState.isLoading && uiState.page > 1) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
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
