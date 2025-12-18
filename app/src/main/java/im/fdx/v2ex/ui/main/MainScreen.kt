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
import im.fdx.v2ex.ui.tabPaths
import im.fdx.v2ex.ui.tabTitles
import kotlinx.coroutines.launch
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import androidx.core.os.bundleOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
// import im.fdx.v2ex.ui.main.TopicsFragment keys logic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(onItemClick = { 
                // Handle complex interactions or close drawer
                coroutineScope.launch { drawerState.close() }
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
                     // logic for type is missing from global variable but we can assume mostly simple tabs
                     // In MyViewPagerAdapter: MyTab(tabTitles[index], tabPaths[index])
                     // MyTab also has 'type'.
                     // We need to access that data. 
                     // For now assume standard tabs.
                     
                     TopicListScreen(
                         tab = tabPath,
                         onTopicClick = { topic ->
                             val intent = Intent(context, TopicActivity::class.java).apply {
                                putExtra(Keys.KEY_TOPIC_MODEL, topic)
                             }
                             context.startActivity(intent)
                         },
                         onMemberClick = { username ->
                              val intent = Intent(context, MemberActivity::class.java).apply {
                                putExtra(Keys.KEY_USERNAME, username)
                             }
                             context.startActivity(intent)
                         },
                         onNodeClick = { nodeName ->
                              val intent = Intent(context, NodeActivity::class.java).apply {
                                putExtra(Keys.KEY_NODE_NAME, nodeName)
                             }
                             context.startActivity(intent)
                         }
                     )
                }
            }
        }
    }
}
