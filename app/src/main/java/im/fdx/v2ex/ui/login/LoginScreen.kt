package im.fdx.v2ex.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import im.fdx.v2ex.R

@Composable
fun LoginScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    captcha: String,
    onCaptchaChange: (String) -> Unit,
    captchaInfo: CaptchaInfo?,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onCaptchaClick: () -> Unit,
    onSignUpClick: () -> Unit,
    showTwoStepDialog: Boolean = false,
    twoStepCode: String = "",
    onTwoStepCodeChange: (String) -> Unit = {},
    onTwoStepSubmit: () -> Unit = {},
    onTwoStepDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // 2FA Dialog
    if (showTwoStepDialog) {
        AlertDialog(
            onDismissRequest = onTwoStepDismiss,
            title = { Text(stringResource(R.string.two_step_verification)) },
            text = {
                Column {
                    Text(stringResource(R.string.input_two_step_code))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = twoStepCode,
                        onValueChange = onTwoStepCodeChange,
                        label = { Text(stringResource(R.string.verify_code)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onTwoStepSubmit,
                    enabled = twoStepCode.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.verify))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onTwoStepDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.log_in_v2ex),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.username_or_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = captcha,
                    onValueChange = onCaptchaChange,
                    label = { Text(stringResource(R.string.verify_code)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (captchaInfo != null) {
                     val imageRequest = remember(captchaInfo) {
                         ImageRequest.Builder(context)
                             .data(captchaInfo.url)
                             .apply {
                                 captchaInfo.headers.forEach { (key, value) ->
                                     addHeader(key, value)
                                 }
                             }
                             .build()
                     }
                     AsyncImage(
                        model = imageRequest,
                        contentDescription = "Captcha",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable { onCaptchaClick() }
                    )
                } else {
                     Box(
                         modifier = Modifier
                             .weight(1f)
                             .height(56.dp)
                             .clickable { onCaptchaClick() },
                         contentAlignment = Alignment.Center
                     ) {
                         Text(stringResource(R.string.menu_refresh))
                     }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.login))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text(stringResource(R.string.no_account_signup))
            }
        }
    }
}
