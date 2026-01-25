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
    
    val twoStepVerificationStr = stringResource(R.string.two_step_verification)
    val inputTwoStepCodeStr = stringResource(R.string.input_two_step_code)
    val verifyCodeStr = stringResource(R.string.verify_code)
    val verifyStr = stringResource(R.string.verify)
    val cancelStr = stringResource(R.string.cancel)
    val logInV2EXStr = stringResource(R.string.log_in_v2ex)
    val usernameOrEmailStr = stringResource(R.string.username_or_email)
    val passwordStr = stringResource(R.string.password)
    val menuRefreshStr = stringResource(R.string.menu_refresh)
    val loginStr = stringResource(R.string.login)
    val noAccountSignUpStr = stringResource(R.string.no_account_signup)
    
    // 2FA Dialog
    if (showTwoStepDialog) {
        AlertDialog(
            onDismissRequest = onTwoStepDismiss,
            title = { Text(twoStepVerificationStr) },
            text = {
                Column {
                    Text(inputTwoStepCodeStr)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = twoStepCode,
                        onValueChange = onTwoStepCodeChange,
                        label = { Text(verifyCodeStr) },
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
                        Text(verifyStr)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onTwoStepDismiss) {
                    Text(cancelStr)
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
                text = logInV2EXStr,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text(usernameOrEmailStr) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(passwordStr) },
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
                    label = { Text(verifyCodeStr) },
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
                         Text(menuRefreshStr)
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
                    Text(loginStr)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text(noAccountSignUpStr)
            }
        }
    }
}
