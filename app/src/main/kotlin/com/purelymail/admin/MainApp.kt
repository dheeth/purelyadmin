package com.purelymail.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.purelymail.admin.data.repository.PurelymailRepository
import com.purelymail.admin.data.repository.TokenManager
import com.purelymail.admin.ui.screens.dashboard.DashboardScreen
import com.purelymail.admin.ui.screens.dashboard.DashboardViewModel
import com.purelymail.admin.ui.screens.domains.DomainsScreen
import com.purelymail.admin.ui.screens.domains.DomainsViewModel
import com.purelymail.admin.ui.screens.routing.RoutingScreen
import com.purelymail.admin.ui.screens.routing.RoutingViewModel
import com.purelymail.admin.ui.screens.settings.SettingsScreen
import com.purelymail.admin.ui.screens.settings.SettingsViewModel
import com.purelymail.admin.ui.screens.users.UsersScreen
import com.purelymail.admin.ui.screens.users.UsersViewModel

enum class PurelyNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Dashboard("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    Users("Mailboxes", Icons.Filled.People, Icons.Outlined.People),
    Domains("Domains", Icons.Filled.Dns, Icons.Outlined.Dns),
    Routing("Routing", Icons.Filled.AltRoute, Icons.Outlined.AltRoute),
    Settings("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainApp(
    repository: PurelymailRepository
) {
    var selectedTab by remember { mutableStateOf(PurelyNavTab.Dashboard) }

    val dashboardViewModel = remember { DashboardViewModel(repository) }
    val usersViewModel = remember { UsersViewModel(repository) }
    val domainsViewModel = remember { DomainsViewModel(repository) }
    val routingViewModel = remember { RoutingViewModel(repository) }
    val settingsViewModel = remember { SettingsViewModel(repository) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                PurelyNavTab.values().forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                PurelyNavTab.Dashboard -> {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToUsers = { selectedTab = PurelyNavTab.Users },
                        onNavigateToDomains = { selectedTab = PurelyNavTab.Domains },
                        onNavigateToRouting = { selectedTab = PurelyNavTab.Routing },
                        onNavigateToSettings = { selectedTab = PurelyNavTab.Settings },
                        onOpenCreateUser = {
                            selectedTab = PurelyNavTab.Users
                        },
                        onOpenAddDomain = {
                            selectedTab = PurelyNavTab.Domains
                        },
                        onOpenCreateRouting = {
                            selectedTab = PurelyNavTab.Routing
                        }
                    )
                }
                PurelyNavTab.Users -> {
                    UsersScreen(viewModel = usersViewModel)
                }
                PurelyNavTab.Domains -> {
                    DomainsScreen(viewModel = domainsViewModel)
                }
                PurelyNavTab.Routing -> {
                    RoutingScreen(viewModel = routingViewModel)
                }
                PurelyNavTab.Settings -> {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}
