package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.model.Node
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import im.fdx.v2ex.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTopicScreen(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    nodeName: String?,
    nodeTitle: String?,
    onNodeClick: () -> Unit,
    isLoading: Boolean,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    val newTopicStr = stringResource(R.string.new_topic)
    val closeStr = stringResource(R.string.close)
    val postReplyStr = stringResource(R.string.post_reply)
    val chooseNodeStr = stringResource(R.string.choose_node)
    val topicTitleStr = stringResource(R.string.topic_title)
    val contentStr = stringResource(R.string.content)
    val loginActionStr = stringResource(R.string.login) // For the snackbar action

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = newTopicStr) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = closeStr,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (im.fdx.v2ex.utils.verifyLogin(context, snackbarHostState, scope, loginActionStr, onLoginClick = { })) {
                            onSendClick()
                        }
                    }, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = postReplyStr,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNodeClick)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (!nodeName.isNullOrEmpty()) "$nodeTitle / $nodeName" else chooseNodeStr,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!nodeName.isNullOrEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(topicTitleStr) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Content Input
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text(contentStr) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}



