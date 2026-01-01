package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Topic
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

import im.fdx.v2ex.utils.Keys
import androidx.core.os.bundleOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.ui.draw.clip

// import im.fdx.v2ex.data.model.TopicsFragment keys logic

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
                if (route.startsWith("member:")) {
                    onMemberClick(route.removePrefix("member:"))
                } else {
                    onMenuClick(route)
                }
            })
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("V2EX") },
                    navigationIcon = {
                        val user by im.fdx.v2ex.utils.UserStore.user.collectAsState()
                        if (user != null) {
                            coil.compose.AsyncImage(
                                model = user!!.avatar_normal,
                                contentDescription = "User Avatar",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable {
                                        coroutineScope.launch { drawerState.open() }
                                    }
                            )
                        } else {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                             Icon(
                                 imageVector = TopicListScreenDefaults.SearchIcon,
                                 contentDescription = "Search",
                                 tint = MaterialTheme.colorScheme.primary
                             )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                SecondaryScrollableTabRow(
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
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge
                                ) 
                            }
                        )
                    }
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    beyondViewportPageCount = 1
                ) { page ->
                     val tabPath = tabPaths[page]
                     TopicListScreen(
                         tab = tabPath,
                         onTopicClick = onTopicClick,
                         onMemberClick = { u -> if(u != null) onMemberClick(u) },
                         onNodeClick = { n -> if(n != null) onNodeClick(n) },
                         contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
                     )
                }
            }
        }
    }
}

object TopicListScreenDefaults {
     val SearchIcon = Icons.Default.Search
}





