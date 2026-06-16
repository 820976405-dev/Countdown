package com.expiryreminder.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import com.expiryreminder.app.ui.theme.AppColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.expiryreminder.app.ui.screens.AddItemScreen
import com.expiryreminder.app.ui.screens.AddItemEntryScreen
import com.expiryreminder.app.ui.screens.AddItemDetailScreen
import com.expiryreminder.app.ui.screens.AllCategoriesScreen
import com.expiryreminder.app.ui.screens.CategoryDetailScreen
import com.expiryreminder.app.ui.screens.CustomItemScreen
import com.expiryreminder.app.ui.screens.TemplateSelectionScreen
import com.expiryreminder.app.ui.screens.CategoryManageScreen
import com.expiryreminder.app.ui.screens.CloudSyncScreen
import com.expiryreminder.app.ui.screens.DetailScreen
import com.expiryreminder.app.ui.screens.EditItemScreen
import com.expiryreminder.app.ui.screens.HomeScreen
import com.expiryreminder.app.ui.screens.ProfileScreen
import com.expiryreminder.app.ui.screens.ReminderItemsScreen
import com.expiryreminder.app.ui.screens.SearchScreen
import com.expiryreminder.app.ui.screens.StatisticsScreen
import com.expiryreminder.app.ui.screens.UserProfileScreen
import com.expiryreminder.app.ui.screens.EditUsernameScreen
import com.expiryreminder.app.ui.screens.EditSignatureScreen
import com.expiryreminder.app.ui.screens.EditAvatarScreen
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddItem : Screen("add_item")
    object AddItemEntry : Screen("add_item_entry")
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_detail/$categoryId"
    }
    object TemplateSelection : Screen("template_selection/{categoryName}") {
        fun createRoute(categoryName: String) = "template_selection/${java.net.URLEncoder.encode(categoryName, "UTF-8")}"
    }
    
    object AllCategories : Screen("all_categories")
    
    object AddItemDetail : Screen("add_item_detail?templateName={templateName}&defaultTag={defaultTag}") {
        fun createRoute(templateName: String? = null, defaultTag: String? = null) = buildString {
            append("add_item_detail")
            val params = mutableListOf<String>()
            templateName?.let { params.add("templateName=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            defaultTag?.let { params.add("defaultTag=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
    }
    object CustomItem : Screen("custom_item")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object Statistics : Screen("statistics")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Long) = "detail/$itemId"
    }
    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: Long) = "edit_item/$itemId"
    }
    object CategoryManage : Screen("category_manage")
    object ReminderItems : Screen("reminder_items")
    object CloudSync : Screen("cloud_sync")
    object UserProfile : Screen("user_profile")
    object EditUsername : Screen("edit_username")
    object EditSignature : Screen("edit_signature")
    object EditAvatar : Screen("edit_avatar")
}

val bottomNavRoutes = listOf(Screen.Home.route, Screen.Profile.route)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    itemViewModel: ItemViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    initialRoute: String? = null
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    var selectedTab by rememberSaveable { mutableStateOf("home") }

    // 处理从小组件跳转的初始路由
    LaunchedEffect(initialRoute) {
        if (initialRoute != null) {
            navController.navigate(initialRoute)
        }
    }

    val showBottomBar = bottomNavRoutes.any { route ->
        currentRoute?.startsWith(route) == true
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (selectedTab != "home") {
                                        selectedTab = "home"
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    }
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "首页",
                            tint = if (selectedTab == "home") MaterialTheme.colorScheme.primary else AppColors.extended.textTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "首页",
                            color = if (selectedTab == "home") MaterialTheme.colorScheme.primary else AppColors.extended.textTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { navController.navigate(Screen.AddItem.route) }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = "添加",
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "添加",
                            color = AppColors.extended.textTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (selectedTab != "profile") {
                                        selectedTab = "profile"
                                        navController.navigate(Screen.Profile.route) {
                                            popUpTo(Screen.Profile.route) { inclusive = true }
                                        }
                                    }
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOutline,
                            contentDescription = "我的",
                            tint = if (selectedTab == "profile") MaterialTheme.colorScheme.primary else AppColors.extended.textTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "我的",
                            color = if (selectedTab == "profile") MaterialTheme.colorScheme.primary else AppColors.extended.textTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Home.route) {
                selectedTab = "home"
                HomeScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddItem.route) },
                    onNavigateToProfile = {
                        selectedTab = "profile"
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onItemClicked = { itemId -> navController.navigate(Screen.Detail.createRoute(itemId)) },
                    onAllItemsClicked = { navController.navigate(Screen.Detail.createRoute(-1)) },
                    onExpiringItemsClicked = { navController.navigate(Screen.Detail.createRoute(-2)) },
                    onNavigateToReminderItems = { navController.navigate(Screen.ReminderItems.route) },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onItemClicked = { itemId -> navController.navigate(Screen.Detail.createRoute(itemId)) },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.AddItem.route) {
                AddItemEntryScreen(
                    categories = emptyList(),
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCategory = { categoryId -> 
                        navController.navigate(Screen.CategoryDetail.createRoute(categoryId)) 
                    },
                    onNavigateToCustom = { 
                        navController.navigate(Screen.AddItemDetail.route)
                    },
                    onQuickAdd = { name, category ->
                        navController.navigate(Screen.AddItemDetail.createRoute(templateName = name, defaultTag = category))
                    },
                    onNavigateToMore = {
                        navController.navigate(Screen.AllCategories.route)
                    }
                )
            }

            composable(Screen.AddItemEntry.route) {
                AddItemEntryScreen(
                    categories = emptyList(),
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCategory = { categoryId -> 
                        navController.navigate(Screen.CategoryDetail.createRoute(categoryId)) 
                    },
                    onNavigateToCustom = { 
                        navController.navigate(Screen.CustomItem.route) 
                    },
                    onQuickAdd = { name, category ->
                        navController.navigate(Screen.AddItemDetail.createRoute(templateName = name, defaultTag = category))
                    },
                    onNavigateToMore = {
                        navController.navigate(Screen.AllCategories.route)
                    }
                )
            }

            composable(
                route = Screen.CategoryDetail.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTemplates = { categoryName, templates ->
                        navController.navigate(Screen.TemplateSelection.createRoute(categoryName))
                    },
                    onNavigateToCustom = { 
                        navController.navigate(Screen.CustomItem.route) 
                    },
                    onItemSelected = { template, subCategoryName ->
                        navController.navigate(Screen.AddItemDetail.createRoute(templateName = template.name, defaultTag = subCategoryName))
                    }
                )
            }

            composable(
                route = Screen.TemplateSelection.route,
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("categoryName") ?: "物品", 
                    "UTF-8"
                )
                TemplateSelectionScreen(
                    categoryName = categoryName,
                    templates = emptyList(),
                    onNavigateBack = { navController.popBackStack() },
                    onTemplateSelected = { template ->
                        navController.navigate(Screen.AddItemDetail.createRoute(templateName = template.name, defaultTag = categoryName))
                    }
                )
            }

            composable(Screen.AllCategories.route) {
                AllCategoriesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCategorySelected = { name, subCategoryName ->
                        navController.navigate(Screen.AddItemDetail.createRoute(templateName = name, defaultTag = subCategoryName))
                    }
                )
            }

            composable(
                route = Screen.AddItemDetail.route,
                arguments = listOf(
                    navArgument("templateName") { defaultValue = null; nullable = true },
                    navArgument("defaultTag") { defaultValue = null; nullable = true }
                )
            ) { backStackEntry ->
                val templateName = backStackEntry.arguments?.getString("templateName")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                val defaultTag = backStackEntry.arguments?.getString("defaultTag")
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                AddItemDetailScreen(
                    templateName = templateName,
                    defaultTag = defaultTag,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.CustomItem.route) {
                CustomItemScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.Profile.route) {
                selectedTab = "profile"
                ProfileScreen(
                    onNavigateToCategoryManage = { navController.navigate(Screen.CategoryManage.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToCloudSync = { navController.navigate(Screen.CloudSync.route) },
                    onNavigateToUserProfile = { navController.navigate(Screen.UserProfile.route) },
                    onClearData = {
                        itemViewModel.deleteAllItems()
                    },
                    onClearAllStorage = {
                        itemViewModel.deleteAllItems()
                        categoryViewModel.deleteAllCategories()
                    },
                    onNavigateToHome = {
                        selectedTab = "home"
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToAdd = { navController.navigate(Screen.AddItem.route) }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
                DetailScreen(
                    itemId = itemId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditClicked = { id -> navController.navigate(Screen.EditItem.createRoute(id)) },
                    itemViewModel = itemViewModel
                )
            }

            composable(
                route = Screen.EditItem.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                EditItemScreen(
                    itemId = itemId,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.CategoryManage.route) {
                CategoryManageScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ReminderItems.route) {
                ReminderItemsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onItemClicked = { itemId -> navController.navigate(Screen.Detail.createRoute(itemId)) },
                    itemViewModel = itemViewModel
                )
            }

            composable(Screen.CloudSync.route) {
                CloudSyncScreen(
                    onNavigateBack = { navController.popBackStack() },
                    itemViewModel = itemViewModel,
                    categoryViewModel = categoryViewModel
                )
            }

            composable(Screen.UserProfile.route) {
                UserProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditUsername = { navController.navigate(Screen.EditUsername.route) },
                    onNavigateToEditAvatar = { navController.navigate(Screen.EditAvatar.route) },
                    onNavigateToEditSignature = { navController.navigate(Screen.EditSignature.route) }
                )
            }

            composable(Screen.EditUsername.route) {
                EditUsernameScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditSignature.route) {
                EditSignatureScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditAvatar.route) {
                EditAvatarScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
