package im.fdx.v2ex.ui.node

import im.fdx.v2ex.data.model.Node
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.ui.theme.V2ExTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNodesScreen(
    onBackClick: () -> Unit,
    onNodeClick: (Node) -> Unit
) {
    val viewModel: AllNodesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { // Simplified TopBar with Search
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchQuery = it
                        viewModel.search(it)
                    },
                    onSearch = { isSearchActive = false },
                    active = true,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search nodes") },
                    leadingIcon = {
                         IconButton(onClick = { 
                             isSearchActive = false 
                             searchQuery = ""
                             viewModel.search("")
                         }) {
                             Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                         }
                    }
                ) {}
            } else {
                TopAppBar(
                    title = { Text("All Nodes") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (!uiState.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.displayedNodes.forEach { (category, nodes) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            FlowRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                nodes.forEach { node ->
                                    SuggestionChip(
                                        onClick = { onNodeClick(node) },
                                        label = { Text(node.title) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
     androidx.compose.foundation.layout.FlowRow(
         modifier = modifier,
         horizontalArrangement = horizontalArrangement,
         verticalArrangement = verticalArrangement,
         content = { content() }
     )
}



