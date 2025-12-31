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
import androidx.compose.ui.unit.dp
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
    onUploadClick: (() -> Unit)?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "R.string.new_topic") }, // We should use string resource
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (onUploadClick != null) {
                         IconButton(onClick = onUploadClick) {
                             Icon(
                                 painter = painterResource(id = R.drawable.ic_image),
                                 contentDescription = "Upload Image",
                                 tint = MaterialTheme.colorScheme.primary
                             )
                         }
                    }
                    IconButton(onClick = onSendClick, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            // Node Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNodeClick)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (!nodeName.isNullOrEmpty()) "$nodeName | $nodeTitle" else "选择节点",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!nodeName.isNullOrEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("标题") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Content Input
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("内容") },
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
        }
    }
}



