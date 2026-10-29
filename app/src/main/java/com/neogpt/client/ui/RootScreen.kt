package com.neogpt.client.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neogpt.client.AppContainer
import com.neogpt.client.ui.chat.ChatScreen
import com.neogpt.client.ui.home.HomeScreen
import com.neogpt.client.ui.models.ModelPickerSheet
import com.neogpt.client.ui.settings.SettingsScreen
import com.neogpt.client.ui.sidebar.SidebarContent
import com.neogpt.client.ui.theme.NeoTheme
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CHAT = "chat/{conversationId}"
    const val ARG_CONVERSATION_ID = "conversationId"

    fun chat(conversationId: String) = "chat/$conversationId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(container: AppContainer) {
    val viewModel: RootViewModel = viewModel(
        factory = ContainerViewModelFactory { RootViewModel(container) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val currentConversationId = backStackEntry?.arguments?.getString(Routes.ARG_CONVERSATION_ID)
    val drawerEnabled = route != Routes.SETTINGS

    var showModelPicker by remember { mutableStateOf(false) }
    var sharedDraft by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        container.shareInbox.receiveAsFlow().collect { text ->
            sharedDraft = text
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(route, currentConversationId) {
        viewModel.setActiveConversation(if (route == Routes.CHAT) currentConversationId else null)
    }

    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    val goHome: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerEnabled,
        scrimColor = NeoTheme.colors.scrim,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(304.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                drawerContainerColor = NeoTheme.colors.sidebar,
            ) {
                SidebarContent(
                    conversations = state.conversations,
                    activeConversationId = state.activeConversationId,
                    modelLabel = state.modelLabel,
                    onNewChat = {
                        closeDrawer()
                        goHome()
                    },
                    onSelectConversation = { id ->
                        closeDrawer()
                        navController.navigate(Routes.chat(id)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onRename = viewModel::renameConversation,
                    onDelete = { id ->
                        viewModel.deleteConversation(id)
                        if (currentConversationId == id) {
                            closeDrawer()
                            goHome()
                        }
                    },
                    onOpenSettings = {
                        closeDrawer()
                        navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    modelLabel = state.modelLabel,
                    onOpenDrawer = openDrawer,
                    onOpenModelPicker = { showModelPicker = true },
                    onConversationCreated = { id ->
                        navController.navigate(Routes.chat(id)) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                    sharedDraft = sharedDraft,
                    onSharedDraftConsumed = { sharedDraft = null },
                )
            }

            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument(Routes.ARG_CONVERSATION_ID) { type = NavType.StringType }),
            ) { entry ->
                val conversationId = entry.arguments?.getString(Routes.ARG_CONVERSATION_ID)
                if (!conversationId.isNullOrBlank()) {
                    ChatScreen(
                        container = container,
                        conversationId = conversationId,
                        modelLabel = state.modelLabel,
                        onOpenDrawer = openDrawer,
                        onOpenModelPicker = { showModelPicker = true },
                        onNewChat = {
                            goHome()
                        },
                    )
                }
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }

    if (showModelPicker) {
        ModelPickerSheet(
            selectedModelId = state.modelId,
            onSelect = viewModel::selectModel,
            onDismiss = { showModelPicker = false },
        )
    }
}
