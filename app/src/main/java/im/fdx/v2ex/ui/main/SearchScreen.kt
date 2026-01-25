package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Topic
import android.app.SearchManager
import android.content.Context
import android.content.Intent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.data.model.Node
import im.fdx.v2ex.utils.TimeUtil
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    selectedNode: Node? = null,
    onNodeClear: () -> Unit = {},
    onChooseNodeClick: () -> Unit = {},
    onBackClick: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onMemberClick: (String?) -> Unit,
    onNodeClick: (String?) -> Unit
) {
    // val context = LocalContext.current // Context unused
    val viewModel: TopicListViewModel = viewModel()
    var query by rememberSaveable { mutableStateOf(initialQuery ?: "") }
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
                        placeholder = { Text(stringResource(im.fdx.v2ex.R.string.search_v2ex_hint)) },
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
            
            LaunchedEffect(selectedNode) {
                 if (selectedNode != null) {
                     viewModel.updateSearchFilter(node = selectedNode.name, nodeTitle = selectedNode.title)
                 }
            }

            var showSortMenu by remember { mutableStateOf(false) }
            var showDateRangePicker by remember { mutableStateOf(false) }
            val dateRangePickerState = rememberDateRangePickerState()

            if (showDateRangePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDateRangePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val startMillis = dateRangePickerState.selectedStartDateMillis
                            val endMillis = dateRangePickerState.selectedEndDateMillis
                            
                            val startStr = startMillis?.let { 
                                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                                TimeUtil.toDisplay(cal)
                            }
                            val endStr = endMillis?.let { 
                                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = it }
                                TimeUtil.toDisplay(cal)
                            }
                            
                            viewModel.updateSearchFilter(gte = startStr ?: "CLEAR", lte = endStr ?: "CLEAR")
                            showDateRangePicker = false
                        }) {
                            Text(stringResource(im.fdx.v2ex.R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDateRangePicker = false }) {
                            Text(stringResource(im.fdx.v2ex.R.string.cancel))
                        }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Dropdown
                Box {
                    val sortLabel = when {
                        searchOption?.sort == im.fdx.v2ex.data.model.SUMUP -> stringResource(im.fdx.v2ex.R.string.search_sort_relevance)
                        searchOption?.order == im.fdx.v2ex.data.model.OLD_FIRST -> stringResource(im.fdx.v2ex.R.string.search_order_oldest_first)
                        else -> stringResource(im.fdx.v2ex.R.string.search_order_newest_first)
                    }
                    
                    FilterChip(
                        selected = true,
                        onClick = { showSortMenu = true },
                        label = { Text(sortLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(im.fdx.v2ex.R.string.search_order_newest_first)) },
                            onClick = {
                                viewModel.updateSearchFilter(sort = im.fdx.v2ex.data.model.CREATED, order = im.fdx.v2ex.data.model.NEW_FIRST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(im.fdx.v2ex.R.string.search_order_oldest_first)) },
                            onClick = {
                                viewModel.updateSearchFilter(sort = im.fdx.v2ex.data.model.CREATED, order = im.fdx.v2ex.data.model.OLD_FIRST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(im.fdx.v2ex.R.string.search_sort_relevance)) },
                            onClick = {
                                viewModel.updateSearchFilter(sort = im.fdx.v2ex.data.model.SUMUP)
                                showSortMenu = false
                            }
                        )
                    }
                }

                // Date Filter
                FilterChip(
                    selected = searchOption?.gte != null || searchOption?.lte != null,
                    onClick = { showDateRangePicker = true },
                    label = { 
                        val text = if (searchOption?.gte != null || searchOption?.lte != null) {
                            "${searchOption?.gte ?: ""} - ${searchOption?.lte ?: ""}"
                        } else {
                            stringResource(im.fdx.v2ex.R.string.search_sort_time)
                        }
                        Text(text)
                    },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchOption?.gte != null || searchOption?.lte != null) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp).clickable {
                                    viewModel.updateSearchFilter(gte = "CLEAR", lte = "CLEAR")
                                }
                            )
                        }
                    }
                )

                // Node Filter
                FilterChip(
                    selected = searchOption?.node != null,
                    onClick = { onChooseNodeClick() },
                    label = { Text(searchOption?.nodeTitle ?: searchOption?.node ?: stringResource(im.fdx.v2ex.R.string.node)) },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchOption?.node != null) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp).clickable {
                                    viewModel.updateSearchFilter(node = "CLEAR", nodeTitle = "CLEAR")
                                    onNodeClear()
                                }
                            )
                        }
                    }
                )
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



