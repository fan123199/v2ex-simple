package im.fdx.v2ex.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import im.fdx.v2ex.R

@OptIn(ExperimentalMaterial3Api::class)
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
    onGoogleLoginClick: () -> Unit,
    onCaptchaClick: () -> Unit,
    onBackClick: () -> Unit,
    fieldErrors: LoginFieldErrors = LoginFieldErrors(),
    isCaptchaLoading: Boolean = false,
    captchaError: String? = null,
    errorMessage: String? = null,
    showTwoStepDialog: Boolean = false,
    twoStepCode: String = "",
    onTwoStepCodeChange: (String) -> Unit = {},
    onTwoStepSubmit: () -> Unit = {},
    onTwoStepDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val captchaFocus = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }
    var imageLoading by remember(captchaInfo) { mutableStateOf(captchaInfo != null) }
    var imageFailed by remember(captchaInfo) { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )
    val canSubmit = !isLoading && !isCaptchaLoading && captchaInfo != null &&
        !imageLoading && !imageFailed
    val submitLogin = {
        if (canSubmit) {
            when {
                username.isBlank() -> usernameFocus.requestFocus()
                password.isEmpty() -> passwordFocus.requestFocus()
                captcha.isBlank() -> captchaFocus.requestFocus()
                else -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
            onLoginClick()
        }
    }

    if (showTwoStepDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onTwoStepDismiss() },
            title = { Text(stringResource(R.string.two_step_verification)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.input_two_step_code))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = twoStepCode,
                        onValueChange = onTwoStepCodeChange,
                        label = { Text(stringResource(R.string.verify_code)) },
                        singleLine = true,
                        enabled = !isLoading,
                        isError = errorMessage != null,
                        shape = shape,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (twoStepCode.isNotBlank() && !isLoading) onTwoStepSubmit()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LoginErrorMessage(errorMessage)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onTwoStepSubmit, enabled = twoStepCode.isNotBlank() && !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(if (isLoading) R.string.login_verifying else R.string.verify))
                }
            },
            dismissButton = {
                TextButton(onClick = onTwoStepDismiss, enabled = !isLoading) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.login_back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
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
                    enabled = !isLoading,
                    isError = fieldErrors.username,
                    supportingText = if (fieldErrors.username) {
                        { Text(stringResource(R.string.login_enter_username)) }
                    } else null,
                    shape = shape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(usernameFocus)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    enabled = !isLoading,
                    isError = fieldErrors.password,
                    supportingText = if (fieldErrors.password) {
                        { Text(stringResource(R.string.login_enter_password)) }
                    } else null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = stringResource(
                                    if (passwordVisible) R.string.login_hide_password else R.string.login_show_password
                                )
                            )
                        }
                    },
                    shape = shape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { captchaFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .width(216.dp)
                            .height(54.dp)
                            .clickable(
                                enabled = !isLoading && !isCaptchaLoading && !imageLoading,
                                role = Role.Button,
                                onClickLabel = stringResource(R.string.login_refresh_captcha),
                                onClick = onCaptchaClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (captchaInfo != null && !imageFailed) {
                            val imageRequest = remember(captchaInfo) {
                                ImageRequest.Builder(context)
                                    .data(captchaInfo.url)
                                    .apply {
                                        captchaInfo.headers.forEach { (key, value) -> addHeader(key, value) }
                                    }
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .build()
                            }
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = stringResource(R.string.login_captcha_image),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center,
                                onLoading = { imageLoading = true },
                                onSuccess = { imageLoading = false },
                                onError = { imageLoading = false; imageFailed = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (isCaptchaLoading || imageLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).align(Alignment.Center),
                                strokeWidth = 2.dp
                            )
                        } else if (captchaInfo == null || imageFailed) {
                            Text(
                                text = stringResource(R.string.login_captcha_retry),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    TextButton(
                        onClick = onCaptchaClick,
                        enabled = !isLoading && !isCaptchaLoading && !imageLoading,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(
                            if (isCaptchaLoading || imageLoading) R.string.login_captcha_loading
                            else R.string.login_refresh_captcha
                        ))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = captcha,
                    onValueChange = onCaptchaChange,
                    label = { Text(stringResource(R.string.verify_code)) },
                    singleLine = true,
                    enabled = !isLoading && !isCaptchaLoading,
                    isError = fieldErrors.captcha,
                    supportingText = if (fieldErrors.captcha) {
                        { Text(stringResource(R.string.login_enter_captcha)) }
                    } else null,
                    shape = shape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { submitLogin() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(captchaFocus)
                )
                Spacer(modifier = Modifier.height(12.dp))
                val captchaErrorMessage = captchaError ?: if (imageFailed) stringResource(R.string.login_captcha_load_failed) else null
                if (captchaErrorMessage != null) {
                    LoginErrorMessage(captchaErrorMessage)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (errorMessage != null && !showTwoStepDialog) {
                    LoginErrorMessage(errorMessage)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = submitLogin,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = shape,
                    enabled = canSubmit
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = stringResource(if (isLoading) R.string.login_in_progress else R.string.login),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onGoogleLoginClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = shape,
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.google_login), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun LoginErrorMessage(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
    }
}
