package im.fdx.v2ex.ui.login

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import im.fdx.v2ex.ui.theme.V2ExTheme

@Composable
fun LoginScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    captcha: String,
    onCaptchaChange: (String) -> Unit,
    captchaUrl: GlideUrl?,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onCaptchaClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
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

                if (captchaUrl != null) {
                     AsyncImage(
                        model = captchaUrl,
                        contentDescription = "Captcha",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(56.dp)
                            .width(100.dp)
                            .clickable { onCaptchaClick() }
                    )
                } else {
                     Box(
                         modifier = Modifier
                             .height(56.dp)
                             .width(100.dp)
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
