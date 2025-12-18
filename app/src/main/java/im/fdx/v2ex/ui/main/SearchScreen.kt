package im.fdx.v2ex.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.ui.topic.TopicActivity
import im.fdx.v2ex.ui.theme.V2ExTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
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
                            disabledContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                             viewModel.search(query)
                             keyboardController?.hide()
                        }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            TopicListScreen(
                viewModel = viewModel,
                onTopicClick = { topic ->
                    val intent = Intent(context, TopicActivity::class.java)
                    intent.putExtra(Keys.KEY_TOPIC_ID, topic.id)
                    context.startActivity(intent)
                },
                onMemberClick = { username ->
                     if (username != null) {
                        val intent = Intent(context, MemberActivity::class.java)
                        intent.putExtra(Keys.KEY_USERNAME, username)
                        context.startActivity(intent)
                     }
                },
                onNodeClick = { nodeName ->
                    if (nodeName != null) {
                         val intent = Intent(context, NodeActivity::class.java)
                         intent.putExtra(Keys.KEY_NODE_NAME, nodeName)
                         context.startActivity(intent)
                    }
                }
            )
        }
    }
}
