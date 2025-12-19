package im.fdx.v2ex.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import im.fdx.v2ex.ui.settings.tabPaths
import im.fdx.v2ex.ui.settings.tabTitles
import kotlinx.coroutines.launch
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import androidx.core.os.bundleOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
// import im.fdx.v2ex.ui.main.TopicsFragment keys logic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String) -> Unit,
    onNodeClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: (String) -> Unit // Route string ?? Or separate callbacks
) {
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    // val context = LocalContext.current // Context no longer needed for navigation (ideally)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(onItemClick = { route ->
                coroutineScope.launch { drawerState.close() }
                onMenuClick(route) // MainDrawer should return a generic identifier or route
            })
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("V2EX") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                             Icon(im.fdx.v2ex.ui.main.TopicListScreenDefaults.SearchIcon, contentDescription = "Search")
                             // Assuming SearchIcon is available or standard icon. 
                             // Wait, search icon was not in original MainScreen topBar actions (it was empty).
                             // But AppNavigation expects onSearchClick.
                             // Let's add it if desired, or keep it empty. 
                             // Legacy 'TopicsFragment' had search in options menu.
                             // Compose AppBar usually puts search icon here.
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                 ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                     val tabPath = tabPaths[page]
                     TopicListScreen(
                         tab = tabPath,
                         onTopicClick = onTopicClick,
                         onMemberClick = { u -> if(u != null) onMemberClick(u) },
                         onNodeClick = { n -> if(n != null) onNodeClick(n) }
                     )
                }
            }
        }
    }
}

object TopicListScreenDefaults {
     val SearchIcon = Icons.Default.Search
}

