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
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
            title = { Text("两步验证") },
            text = {
                Column {
                    Text("请输入您的两步验证码")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = twoStepCode,
                        onValueChange = onTwoStepCodeChange,
                        label = { Text("验证码") },
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
                        Text("验证")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onTwoStepDismiss) {
                    Text("取消")
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
                text = "登录 V2EX",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("用户名 / 邮箱") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
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
                    label = { Text("验证码") },
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
                         Text("点击刷新")
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
                    Text("登录")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text("没有账号？去注册")
            }
        }
    }
}
