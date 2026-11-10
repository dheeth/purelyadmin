package com.purelymail.admin.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.purelymail.admin.data.model.GetUserResponse
import com.purelymail.admin.data.model.UpsertPasswordResetRequest
import com.purelymail.admin.ui.components.ConfirmDeleteDialog
import com.purelymail.admin.ui.components.CopyableRow
import com.purelymail.admin.ui.components.copyToClipboard
import com.purelymail.admin.ui.theme.PurelyPurplePrimary
import com.purelymail.admin.ui.theme.StatusSuccess
import com.purelymail.admin.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailBottomSheet(
    userName: String,
    details: GetUserResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onEditUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onCreateAppPassword: () -> Unit,
    onAddRecoveryMethod: (UpsertPasswordResetRequest) -> Unit,
    onDeleteRecoveryMethod: (target: String) -> Unit,
    onShowClientConfig: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddRecoveryDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PurelyPurplePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = PurelyPurplePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mailbox Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { copyToClipboard(context, "Email Address", userName) }) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (details != null) {
                // Feature chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureChip(
                        name = "2FA",
                        enabled = details.requireTwoFactorAuthentication,
                        activeText = "2FA Required",
                        inactiveText = "2FA Optional",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureChip(
                        name = "Search Index",
                        enabled = details.enableSearchIndexing,
                        activeText = "Search Indexed",
                        inactiveText = "No Index",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureChip(
                        name = "Reset",
                        enabled = details.recoveryEnabled,
                        activeText = "Reset Allowed",
                        inactiveText = "Reset Disabled",
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                // Quick Action Buttons
                Text(
                    text = "Mailbox Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCreateAppPassword,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("App Password", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = onShowClientConfig,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Client Setup", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditUser,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit User")
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Mailbox")
                    }
                }

                HorizontalDivider()

                // Recovery Methods Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password Reset Methods",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddRecoveryDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                if (details.resetMethods.isEmpty()) {
                    Text(
                        text = "No recovery methods configured. If this user forgets their password, only an admin can reset it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    details.resetMethods.forEach { method ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (method.type == "phone") Icons.Default.Phone else Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = method.target, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        if (method.description.isNotBlank()) {
                                            Text(text = method.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                IconButton(onClick = { onDeleteRecoveryMethod(method.target) }) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Mailbox?",
            message = "Are you sure you want to delete '$userName'? All emails and settings associated with this mailbox will be permanently removed.",
            onConfirm = {
                onDeleteUser()
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showAddRecoveryDialog) {
        AddRecoveryMethodDialog(
            userName = userName,
            onDismiss = { showAddRecoveryDialog = false },
            onAdd = {
                onAddRecoveryMethod(it)
                showAddRecoveryDialog = false
            }
        )
    }
}

@Composable
fun FeatureChip(
    name: String,
    enabled: Boolean,
    activeText: String,
    inactiveText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (enabled) StatusSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (enabled) activeText else inactiveText,
                fontSize = 10.sp,
                color = if (enabled) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddRecoveryMethodDialog(
    userName: String,
    onDismiss: () -> Unit,
    onAdd: (UpsertPasswordResetRequest) -> Unit
) {
    var type by remember { mutableStateOf("email") }
    var target by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var allowMfaReset by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recovery Method") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "email",
                        onClick = { type = "email" },
                        label = { Text("Email") }
                    )
                    FilterChip(
                        selected = type == "phone",
                        onClick = { type = "phone" },
                        label = { Text("Phone") }
                    )
                }

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text(if (type == "email") "Email Address" else "Phone Number (+1...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Allow MFA Reset", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = allowMfaReset, onCheckedChange = { allowMfaReset = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (target.isNotBlank()) {
                        onAdd(
                            UpsertPasswordResetRequest(
                                userName = userName,
                                type = type,
                                target = target.trim(),
                                description = description.trim(),
                                allowMfaReset = allowMfaReset
                            )
                        )
                    }
                },
                enabled = target.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailClientSetupSheet(
    userName: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Email Client Setup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Use these settings in Thunderbird, Outlook, Apple Mail, or K-9 Mail for '$userName':",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CopyableRow(label = "Username", value = userName)
            CopyableRow(label = "Incoming (IMAP) Server", value = "mail.purelymail.com : 993 (SSL/TLS)")
            CopyableRow(label = "Outgoing (SMTP) Server", value = "mail.purelymail.com : 465 (SSL) / 587 (STARTTLS)")

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Note: If 2FA is enabled for this account, you must generate an App Password rather than using your login password.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
