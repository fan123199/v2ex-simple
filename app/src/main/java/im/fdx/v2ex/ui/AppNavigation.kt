package im.fdx.v2ex.ui

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
import im.fdx.v2ex.ui.main.Topic
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
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
                        "daily" -> {
                             // Legacy activity support
                             try {
                                 val intent = android.content.Intent(navController.context, Class.forName("im.fdx.v2ex.ui.daily.DailyActivity"))
                                 navController.context.startActivity(intent)
                             } catch (e: Exception) {
                                 e.printStackTrace()
                             }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Topic.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("id") ?: return@composable
            TopicDetailScreen(
                topicId = topicId,
                initialTopic = null,
                onBackClick = { navController.popBackStack() },
                onMemberClick = { username -> if(username!=null) navController.navigate(Screen.Member.createRoute(username)) }
            )
        }

        composable(
            route = Screen.Member.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: return@composable
            MemberScreen(
                username = username,
                onBackClick = { navController.popBackStack() },
                onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) },
                onMemberClick = { u -> if(u!=null) navController.navigate(Screen.Member.createRoute(u)) },
                onNodeClick = { n -> if(n!=null) navController.navigate(Screen.Node.createRoute(n)) }
            )
        }

        composable(
            route = Screen.Node.route,
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
             val nodeName = backStackEntry.arguments?.getString("name") ?: return@composable
             NodeScreen(
                 nodeName = nodeName,
                 onBackClick = { navController.popBackStack() },
                 onTopicClick = { topic -> navController.navigate(Screen.Topic.createRoute(topic.id)) }
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
                    navController.navigate(Screen.Node.createRoute(node.name))
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
    }
}
