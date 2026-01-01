package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Topic
import android.app.SearchManager
import android.content.Context
import android.content.Intent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String?) -> Unit,
    onNodeClick: (String?) -> Unit
) {
    // val context = LocalContext.current // Context unused
    val viewModel: TopicListViewModel = viewModel()
    var query by remember { mutableStateOf(initialQuery ?: "") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrEmpty()) {
            viewModel.search(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search V2EX") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                             viewModel.search(query)
                             keyboardController?.hide()
                        }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val searchOption by viewModel.searchOption.collectAsState()
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = searchOption?.sort == im.fdx.v2ex.data.model.CREATED,
                    onClick = { 
                        if(searchOption?.sort != im.fdx.v2ex.data.model.CREATED) {
                            viewModel.updateSearchFilter(sort = im.fdx.v2ex.data.model.CREATED) 
                        }
                    },
                    label = { Text("按时间") }
                )
                
               FilterChip(
                    selected = searchOption?.sort == im.fdx.v2ex.data.model.SUMUP,
                    onClick = { 
                        if(searchOption?.sort != im.fdx.v2ex.data.model.SUMUP) {
                            viewModel.updateSearchFilter(sort = im.fdx.v2ex.data.model.SUMUP) 
                        }
                    },
                    label = { Text("按相关度") }
                )
                
                // Order Toggle
                if (searchOption?.sort == im.fdx.v2ex.data.model.CREATED) {
                     FilterChip(
                        selected = searchOption?.order == im.fdx.v2ex.data.model.NEW_FIRST,
                        onClick = { 
                            val newOrder = if (searchOption?.order == im.fdx.v2ex.data.model.NEW_FIRST) im.fdx.v2ex.data.model.OLD_FIRST else im.fdx.v2ex.data.model.NEW_FIRST
                            viewModel.updateSearchFilter(order = newOrder) 
                        },
                        label = { Text(if (searchOption?.order == im.fdx.v2ex.data.model.NEW_FIRST) "从新到旧" else "从旧到新") },
                        trailingIcon = {
                            // Optional arrow icon
                        }
                    )
                }
            }
            
            TopicListScreen(
                viewModel = viewModel,
                onTopicClick = onTopicClick,
                onMemberClick = onMemberClick,
                onNodeClick = onNodeClick,
                enableAutoInit = false
            )
        }
    }
}



