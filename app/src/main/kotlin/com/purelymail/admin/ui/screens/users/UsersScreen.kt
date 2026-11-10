package com.purelymail.admin.ui.screens.users

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purelymail.admin.ui.components.EmptyState
import com.purelymail.admin.ui.components.copyToClipboard
import com.purelymail.admin.ui.theme.PurelyPurplePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showModifyDialog by remember { mutableStateOf(false) }
    var showAppPasswordDialog by remember { mutableStateOf(false) }
    var showClientConfigSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mailboxes", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.users.size} active account(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("New Mailbox") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by user or domain...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (uiState.isLoading && uiState.users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredUsers.isEmpty()) {
                EmptyState(
                    title = if (uiState.searchQuery.isBlank()) "No Mailboxes Found" else "No matching accounts",
                    description = if (uiState.searchQuery.isBlank()) "Create your first email user on one of your verified domains." else "Try refining your search keyword.",
                    icon = Icons.Default.MailOutline,
                    actionText = if (uiState.searchQuery.isBlank()) "Create Mailbox" else null,
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredUsers, key = { it }) { user ->
                        val parts = user.split("@")
                        val localPart = parts.getOrNull(0) ?: user
                        val domainPart = parts.getOrNull(1) ?: ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectUser(user) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(PurelyPurplePrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = localPart.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = PurelyPurplePrimary,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = localPart,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "@$domainPart",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { copyToClipboard(context, "Email", user) }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy email",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet for selected user
    if (uiState.selectedUser != null) {
        UserDetailBottomSheet(
            userName = uiState.selectedUser ?: "",
            details = uiState.selectedUserDetails,
            isLoading = uiState.isLoadingDetails,
            onDismiss = { viewModel.clearSelectedUser() },
            onEditUser = { showModifyDialog = true },
            onDeleteUser = {
                uiState.selectedUser?.let { viewModel.deleteUser(it) }
            },
            onCreateAppPassword = { showAppPasswordDialog = true },
            onAddRecoveryMethod = { viewModel.upsertPasswordReset(it) },
            onDeleteRecoveryMethod = { target ->
                uiState.selectedUser?.let { viewModel.deletePasswordReset(it, target) }
            },
            onShowClientConfig = { showClientConfigSheet = true }
        )
    }

    // Dialogs
    if (showCreateDialog) {
        CreateUserDialog(
            domains = uiState.domains,
            onDismiss = { showCreateDialog = false },
            onCreate = { request ->
                viewModel.createUser(request) { showCreateDialog = false }
            }
        )
    }

    if (showModifyDialog && uiState.selectedUser != null) {
        ModifyUserDialog(
            userName = uiState.selectedUser ?: "",
            currentSettings = uiState.selectedUserDetails,
            onDismiss = { showModifyDialog = false },
            onSave = { request ->
                viewModel.modifyUser(request) { showModifyDialog = false }
            }
        )
    }

    if (showAppPasswordDialog && uiState.selectedUser != null) {
        CreateAppPasswordDialog(
            userHandle = uiState.selectedUser ?: "",
            onDismiss = { showAppPasswordDialog = false },
            onCreate = { label ->
                viewModel.createAppPassword(uiState.selectedUser ?: "", label)
                showAppPasswordDialog = false
            }
        )
    }

    // Result dialog showing generated app password
    uiState.newlyGeneratedAppPassword?.let { (label, pass) ->
        AppPasswordResultDialog(
            label = label,
            password = pass,
            userName = uiState.selectedUser ?: "",
            onDismiss = { viewModel.clearNewAppPassword() }
        )
    }

    if (showClientConfigSheet && uiState.selectedUser != null) {
        EmailClientSetupSheet(
            userName = uiState.selectedUser ?: "",
            onDismiss = { showClientConfigSheet = false }
        )
    }
}
