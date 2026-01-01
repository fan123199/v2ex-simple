package im.fdx.v2ex.ui.navigation

import im.fdx.v2ex.data.model.Member
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import im.fdx.v2ex.ui.main.MainScreen
import im.fdx.v2ex.ui.topic.TopicDetailScreen
import im.fdx.v2ex.ui.member.MemberScreen
import im.fdx.v2ex.ui.node.NodeScreen
import im.fdx.v2ex.ui.settings.SettingsScreen
import im.fdx.v2ex.ui.settings.TabSettingScreen
import im.fdx.v2ex.ui.node.AllNodesScreen
import im.fdx.v2ex.ui.main.SearchScreen
import im.fdx.v2ex.ui.favor.FavoriteScreen
import im.fdx.v2ex.ui.notification.NotificationScreen
import im.fdx.v2ex.data.model.Topic
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewmodel.compose.viewModel
import im.fdx.v2ex.ui.login.LoginViewModel
import im.fdx.v2ex.ui.login.LoginResult
import im.fdx.v2ex.ui.main.NewTopicViewModel
import im.fdx.v2ex.ui.main.SendResult
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import im.fdx.v2ex.data.model.Node
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import android.content.Intent
import androidx.compose.runtime.setValue
import im.fdx.v2ex.ui.common.PhotoScreen
import im.fdx.v2ex.ui.common.WebViewScreen
import im.fdx.v2ex.ui.login.LoginScreen
import im.fdx.v2ex.ui.login.LoginScreen
import im.fdx.v2ex.ui.main.NewTopicScreen
import im.fdx.v2ex.ui.main.DailyViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Topic : Screen("topic/{id}") {
        fun createRoute(id: String) = "topic/$id"
    }
    object Member : Screen("member/{username}") {
        fun createRoute(username: String) = "member/$username"
    }
    object Node : Screen("node/{name}") {
        fun createRoute(name: String) = "node/$name"
    }
    object Settings : Screen("settings")
    object TabSettings : Screen("tab_settings")
    object AllNodes : Screen("all_nodes")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Notifications : Screen("notifications")
    object Login : Screen("login")
    object Photo : Screen("photo?url={url}") {
        fun createRoute(url: String) = "photo?url=${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
    }
    object WebView : Screen("webview?url={url}") {
        fun createRoute(url: String) = "webview?url=${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
    }

    object NewTopic : Screen("new_topic?title={title}&content={content}&node={node}") {
        fun createRoute(title: String? = null, content: String? = null, node: String? = null): String {
             val sb = StringBuilder("new_topic")
             val params = mutableListOf<String>()
             if (title != null) params.add("title=${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}")
             if (content != null) params.add("content=${URLEncoder.encode(content, StandardCharsets.UTF_8.toString())}")
             if (node != null) params.add("node=${URLEncoder.encode(node, StandardCharsets.UTF_8.toString())}")
             
             if (params.isNotEmpty()) {
                 sb.append("?")
                 sb.append(params.joinToString("&"))
             }
             return sb.toString()
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    intent: Intent? = null
) {
    val context = LocalContext.current
    val dailyViewModel: DailyViewModel = viewModel()
    
    // Observer for daily check messages
    LaunchedEffect(Unit) {
        dailyViewModel.toastMsg.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(intent) {
        intent?.let {
            if (it.action == Intent.ACTION_SEND && it.type == "text/plain") {
                val sharedText = it.getStringExtra(Intent.EXTRA_TEXT)
                val title = it.getStringExtra(Intent.EXTRA_TITLE)
                if (sharedText != null) {
                    navController.navigate(Screen.NewTopic.createRoute(title = title, content = sharedText))
                }
            } else {
                navController.handleDeepLink(it)
            }
        }
    }
    
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        
        composable(Screen.Home.route) {
            MainScreen(
                onTopicClick = { topic -> 
                     navController.navigate(Screen.Topic.createRoute(topic.id)) 
                },
                onMemberClick = { username -> navController.navigate(Screen.Member.createRoute(username)) },
                onNodeClick = { nodeName -> navController.navigate(Screen.Node.createRoute(nodeName)) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onMenuClick = { route -> 
                    when(route) {
                        "settings" -> navController.navigate(Screen.Settings.route)
                        "all_nodes" -> navController.navigate(Screen.AllNodes.route)
                        "favorites" -> navController.navigate(Screen.Favorites.route)
                        "notifications" -> navController.navigate(Screen.Notifications.route)
                        "login" -> navController.navigate(Screen.Login.route)
                        "daily" -> dailyViewModel.startDailyCheck()
                    }
                },
                onNewTopicClick = {
                    navController.navigate(Screen.NewTopic.createRoute())
                }
            )
        }

        composable(
            route = Screen.Topic.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://www.v2ex.com/t/{id}" },
                navDeepLink { uriPattern = "https://v2ex.com/t/{id}" },
                navDeepLink { uriPattern = "http://www.v2ex.com/t/{id}" },
                navDeepLink { uriPattern = "http://v2ex.com/t/{id}" }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("id") ?: return@composable
            TopicDetailScreen(
                topicId = topicId,
                initialTopic = null,
                onBackClick = { navController.popBackStack() },
                onMemberClick = { username -> if(username!=null) navController.navigate(Screen.Member.createRoute(username)) },
                onReportClick = { title, content ->
                    navController.navigate(Screen.NewTopic.createRoute(title = title, content = content))
                },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.Member.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://www.v2ex.com/member/{username}" },
                navDeepLink { uriPattern = "https://v2ex.com/member/{username}" },
                navDeepLink { uriPattern = "http://www.v2ex.com/member/{username}" },
                navDeepLink { uriPattern = "http://v2ex.com/member/{username}" }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: return@composable
            MemberScreen(
                username = username,
                onBackClick = { navController.popBackStack() },
                onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) },
                onMemberClick = { u -> if(u!=null) navController.navigate(Screen.Member.createRoute(u)) },
                onNodeClick = { n -> if(n!=null) navController.navigate(Screen.Node.createRoute(n)) },
                onReportClick = { title, content -> 
                    navController.navigate(Screen.NewTopic.createRoute(title = title, content = content))
                },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.Node.route,
            arguments = listOf(navArgument("name") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://www.v2ex.com/go/{name}" },
                navDeepLink { uriPattern = "https://v2ex.com/go/{name}" },
                navDeepLink { uriPattern = "http://www.v2ex.com/go/{name}" },
                navDeepLink { uriPattern = "http://v2ex.com/go/{name}" }
            )
        ) { backStackEntry ->
             val nodeName = backStackEntry.arguments?.getString("name") ?: return@composable
             NodeScreen(
                 nodeName = nodeName,
                 onBackClick = { navController.popBackStack() },
                 onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) },
                 onMemberClick = { username -> if(username!=null) navController.navigate(Screen.Member.createRoute(username)) },
                 onNewTopicClick = { node ->
                     navController.navigate(Screen.NewTopic.createRoute(node = node))
                 },
                 onLoginClick = { navController.navigate(Screen.Login.route) }
             )
        }
        
        composable(Screen.Settings.route) {
             SettingsScreen(
                 onBackClick = { navController.popBackStack() },
                 onTabSettingClick = { navController.navigate(Screen.TabSettings.route) }
             )
        }
        
        composable(Screen.TabSettings.route) {
            TabSettingScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AllNodes.route) {
            AllNodesScreen(
                onBackClick = { navController.popBackStack() },
                onNodeClick = { node ->
                    // Check if we came from NewTopic? 
                    // To simplify, we can always set result. If caller doesn't define it, no harm.
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_node", node)
                    
                    // If we want to navigate like "AllNodesActivity", we can check
                    // But here we might just want to return if we are picking.
                    // How to know if picking? Route params or Logic.
                    // For now, let's assume if "selected_node" is consumed, it's fine.
                    // But if we are in "Browser Mode", clicking a node should go to Node Detail.
                    // This creates a conflict since AllNodes logic was split in Activity.
                    // Let's pass a mode to AllNodes screen?
                    
                    // Simplified: Check if previous entry is NewTopic?
                    if (navController.previousBackStackEntry?.destination?.route?.startsWith("new_topic") == true) {
                         navController.popBackStack()
                    } else {
                         navController.navigate(Screen.Node.createRoute(node.name))
                    }
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) },
                onMemberClick = { username -> if(username!=null) navController.navigate(Screen.Member.createRoute(username)) },
                onNodeClick = { nodeName -> if(nodeName!=null) navController.navigate(Screen.Node.createRoute(nodeName)) }
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) },
                onMemberClick = { username -> if(username!=null) navController.navigate(Screen.Member.createRoute(username)) },
                onNodeClick = { nodeName -> if(nodeName!=null) navController.navigate(Screen.Node.createRoute(nodeName)) }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationScreen(
                onBackClick = { navController.popBackStack() },
                onTopicClick = { id -> navController.navigate(Screen.Topic.createRoute(id)) },
                 onMemberClick = { username -> navController.navigate(Screen.Member.createRoute(username)) }
            )

        }



        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel()
            val context = LocalContext.current
            val uiState by loginViewModel.loginResult.collectAsState()
            val username by loginViewModel.username.collectAsState()
            val password by loginViewModel.password.collectAsState()
            val captcha by loginViewModel.captcha.collectAsState()
            val captchaInfo by loginViewModel.captchaInfo.collectAsState()
            val isLoading by loginViewModel.isLoading.collectAsState()
            
            // 2FA dialog state
            var showTwoStepDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            var twoStepCode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

            LaunchedEffect(uiState) {
                when (val result = uiState) {
                    is LoginResult.Success -> {
                        showTwoStepDialog = false
                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    is LoginResult.TwoStep -> {
                        showTwoStepDialog = true
                    }
                    is LoginResult.Error -> {
                         Toast.makeText(context, result.msg, Toast.LENGTH_SHORT).show()
                         loginViewModel.resetResult()
                    }
                    else -> {}
                }
            }

             LoginScreen(
                 username = username,
                 onUsernameChange = loginViewModel::onUsernameChange,
                 password = password,
                 onPasswordChange = loginViewModel::onPasswordChange,
                 captcha = captcha,
                 onCaptchaChange = loginViewModel::onCaptchaChange,
                 captchaInfo = captchaInfo,
                 isLoading = isLoading,
                 onLoginClick = loginViewModel::login,
                 onCaptchaClick = loginViewModel::getLoginElement,
                 onSignUpClick = {
                      navController.navigate(Screen.WebView.createRoute("https://www.v2ex.com/signup"))
                 },
                 showTwoStepDialog = showTwoStepDialog,
                 twoStepCode = twoStepCode,
                 onTwoStepCodeChange = { twoStepCode = it },
                 onTwoStepSubmit = { loginViewModel.submitTwoStepCode(twoStepCode) },
                 onTwoStepDismiss = { 
                     showTwoStepDialog = false
                     twoStepCode = ""
                     loginViewModel.resetResult()
                 }
             )
        }

        composable(
            route = Screen.Photo.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")
            if (url != null) {
                PhotoScreen(
                    photos = listOf(url),
                    initialPage = 0,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
             route = Screen.WebView.route,
             arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
             val url = backStackEntry.arguments?.getString("url")
             if (url != null) {
                  WebViewScreen(
                      url = url,
                      onBackClick = { navController.popBackStack() },
                      onLoginSuccess = { 
                          // Refresh global state if needed
                          navController.popBackStack()
                      }
                  )
             }
        }

        composable(
            route = Screen.NewTopic.route,
            arguments = listOf(
                navArgument("title") { nullable = true },
                navArgument("content") { nullable = true },
                navArgument("node") { nullable = true }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title")
            val content = backStackEntry.arguments?.getString("content")
            val nodeArg = backStackEntry.arguments?.getString("node")
            
            val viewModel: NewTopicViewModel = viewModel()
            val context = LocalContext.current
            // Initialize once
            LaunchedEffect(Unit) {
                viewModel.setInitialData(title, content, nodeArg)
            }
            
            // Handle node selection result
            val savedStateHandle = backStackEntry.savedStateHandle
            val selectedNode by savedStateHandle.getStateFlow<Node?>("selected_node", null).collectAsState()
            
            LaunchedEffect(selectedNode) {
                 selectedNode?.let {
                     viewModel.onNodeNameChange(it.name)
                     savedStateHandle.remove<Node>("selected_node")
                 }
            }

            val uiState by viewModel.sendResult.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val mTitle by viewModel.title.collectAsState()
            val mContent by viewModel.content.collectAsState()
            val mNodeName by viewModel.nodeName.collectAsState()
            
            LaunchedEffect(uiState) {
                when(val result = uiState) {
                    is SendResult.Success -> {
                        Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    is SendResult.Error -> {
                        Toast.makeText(context, result.msg, Toast.LENGTH_SHORT).show()
                        viewModel.resetResult()
                    }
                    else -> {}
                }
            }
            
            NewTopicScreen(
                 title = mTitle,
                 onTitleChange = viewModel::onTitleChange,
                 content = mContent,
                 onContentChange = viewModel::onContentChange,
                 nodeName = mNodeName,
                 nodeTitle = mNodeName, // Temporary mapper or can be extracted from a map/DB if needed
                 onNodeClick = { 
                      navController.navigate(Screen.AllNodes.route)
                 },
                 isLoading = isLoading,
                 onSendClick = viewModel::send,
                 onBackClick = { navController.popBackStack() }
            )
        }
    }
}





