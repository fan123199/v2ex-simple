package im.fdx.v2ex.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    onBackClick: () -> Unit,
    viewModel: DailyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Check") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is DailyState.Loading -> {
                    CircularProgressIndicator()
                }
                is DailyState.LinkError -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${s.msg}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadDaily() }) {
                            Text("Retry")
                        }
                    }
                }
                is DailyState.Ready -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = s.statusText.ifEmpty { "Welcome to Daily Mission!" },
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        if (s.once != null) {
                            Button(
                                onClick = { viewModel.checkIn(s.once) },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Check In Now")
                            }
                        } else {
                            Text(
                                "You have already checked in today.",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                is DailyState.Success -> {
                    // Should be handled by Ready state update, but just in case
                     Text("Success!")
                }
            }
        }
    }
}
