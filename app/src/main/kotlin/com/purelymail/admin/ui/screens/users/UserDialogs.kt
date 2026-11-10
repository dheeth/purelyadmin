package com.purelymail.admin.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.purelymail.admin.data.model.CreateUserRequest
import com.purelymail.admin.data.model.GetUserResponse
import com.purelymail.admin.data.model.ModifyUserRequest
import com.purelymail.admin.data.model.UpsertPasswordResetRequest
import com.purelymail.admin.ui.components.CopyableRow
import com.purelymail.admin.ui.components.copyToClipboard
import com.purelymail.admin.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    domains: List<String>,
    onDismiss: () -> Unit,
    onCreate: (CreateUserRequest) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf(domains.firstOrNull() ?: "purelymail.com") }
    var domainExpanded by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var recoveryEmail by remember { mutableStateOf("") }
    var recoveryPhone by remember { mutableStateOf("") }
    var enableSearchIndexing by remember { mutableStateOf(true) }
    var enablePasswordReset by remember { mutableStateOf(true) }
    var sendWelcomeEmail by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    fun generateStrongPassword(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*"
        return (1..16).map { chars.random() }.joinToString("")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Mailbox",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Anti-abuse policy reminder
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Purelymail Policy: Accounts must not be used for domain warming, bulk marketing lists, or mass automation. Violations lead to immediate termination without refund.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Username & Domain
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it.trim().lowercase() },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. alex") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Domain dropdown
                ExposedDropdownMenuBox(
                    expanded = domainExpanded,
                    onExpandedChange = { domainExpanded = !domainExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDomain,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Domain") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = domainExpanded,
                        onDismissRequest = { domainExpanded = false }
                    ) {
                        domains.forEach { domain ->
                            DropdownMenuItem(
                                text = { Text(domain) },
                                onClick = {
                                    selectedDomain = domain
                                    domainExpanded = false
                                }
                            )
                        }
                    }
                }

                // Password with generator
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { password = generateStrongPassword(); passwordVisible = true }) {
                                Icon(imageVector = Icons.Default.Casino, contentDescription = "Generate Password", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Recovery fields
                OutlinedTextField(
                    value = recoveryEmail,
                    onValueChange = { recoveryEmail = it.trim() },
                    label = { Text("Recovery Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = recoveryPhone,
                    onValueChange = { recoveryPhone = it.trim() },
                    label = { Text("Recovery Phone (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Options
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Search Indexing", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enableSearchIndexing, onCheckedChange = { enableSearchIndexing = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Allow Password Reset", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enablePasswordReset, onCheckedChange = { enablePasswordReset = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Send Welcome Email", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = sendWelcomeEmail, onCheckedChange = { sendWelcomeEmail = it })
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (userName.isNotBlank() && password.isNotBlank()) {
                                onCreate(
                                    CreateUserRequest(
                                        userName = userName,
                                        domainName = selectedDomain,
                                        password = password,
                                        enablePasswordReset = enablePasswordReset,
                                        recoveryEmail = recoveryEmail.ifBlank { null },
                                        recoveryPhone = recoveryPhone.ifBlank { null },
                                        enableSearchIndexing = enableSearchIndexing,
                                        sendWelcomeEmail = sendWelcomeEmail
                                    )
                                )
                            }
                        },
                        enabled = userName.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun ModifyUserDialog(
    userName: String,
    currentSettings: GetUserResponse?,
    onDismiss: () -> Unit,
    onSave: (ModifyUserRequest) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var searchIndexing by remember { mutableStateOf(currentSettings?.enableSearchIndexing ?: true) }
    var passwordReset by remember { mutableStateOf(currentSettings?.recoveryEnabled ?: true) }
    var require2FA by remember { mutableStateOf(currentSettings?.requireTwoFactorAuthentication ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit User Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (Leave blank to keep)") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Search Indexing", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Enables fast mailbox search", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = searchIndexing, onCheckedChange = { searchIndexing = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Allow Password Reset", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Enable self-service recovery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = passwordReset, onCheckedChange = { passwordReset = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Require 2FA", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Mandatory two-factor auth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = require2FA, onCheckedChange = { require2FA = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                ModifyUserRequest(
                                    userName = userName,
                                    newPassword = newPassword.ifBlank { null },
                                    enableSearchIndexing = searchIndexing,
                                    enablePasswordReset = passwordReset,
                                    requireTwoFactorAuthentication = require2FA
                                )
                            )
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAppPasswordDialog(
    userHandle: String,
    onDismiss: () -> Unit,
    onCreate: (name: String) -> Unit
) {
    var label by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate App Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "App passwords bypass 2FA for email apps like Thunderbird, Outlook, Apple Mail, and K-9 Mail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Application Name") },
                    placeholder = { Text("e.g. Work Laptop Thunderbird") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onCreate(label) }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}

@Composable
fun AppPasswordResultDialog(
    label: String,
    password: String,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("App Password Generated", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Copy this password now. Purelymail will never show it to you again!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusWarning,
                    fontWeight = FontWeight.SemiBold
                )

                CopyableRow(label = "Application", value = label, monospace = false)
                CopyableRow(label = "User Email", value = userName)
                CopyableRow(label = "App Password", value = password)

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Use this in your IMAP (port 993) and SMTP (port 465) settings with username '$userName'.",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                copyToClipboard(context, "App Password", password)
                onDismiss()
            }) {
                Text("Copy & Close")
            }
        }
    )
}
