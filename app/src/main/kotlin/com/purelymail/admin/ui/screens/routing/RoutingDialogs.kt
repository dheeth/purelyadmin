package com.purelymail.admin.ui.screens.routing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.purelymail.admin.data.model.CreateRoutingRequest
import com.purelymail.admin.ui.components.CopyableRow
import com.purelymail.admin.ui.components.copyToClipboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutingRuleDialog(
    domains: List<String>,
    users: List<String>,
    onDismiss: () -> Unit,
    onCreate: (CreateRoutingRequest) -> Unit
) {
    var selectedDomain by remember { mutableStateOf(domains.firstOrNull() ?: "") }
    var domainExpanded by remember { mutableStateOf(false) }

    var matchUser by remember { mutableStateOf("") }
    var isPrefix by remember { mutableStateOf(false) }
    var isCatchall by remember { mutableStateOf(false) }

    var targetAddressInput by remember { mutableStateOf("") }
    var targetAddresses by remember { mutableStateOf(if (users.isNotEmpty()) listOf(users.first()) else emptyList<String>()) }

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Routing Rule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Domain picker
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

                // Match User
                if (!isCatchall) {
                    OutlinedTextField(
                        value = matchUser,
                        onValueChange = { matchUser = it.trim().lowercase() },
                        label = { Text(if (isPrefix) "User Prefix" else "Match User") },
                        placeholder = { Text(if (isPrefix) "e.g. newsletter-" else "e.g. support") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Prefix Match", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Matches user-*@domain", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isPrefix,
                        onCheckedChange = {
                            isPrefix = it
                            if (it) isCatchall = false
                        },
                        enabled = !isCatchall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Catch-all Rule", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Forwards unmatched mail for domain", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isCatchall,
                        onCheckedChange = {
                            isCatchall = it
                            if (it) isPrefix = false
                        }
                    )
                }

                HorizontalDivider()

                // Target addresses
                Text("Forward To:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetAddressInput,
                        onValueChange = { targetAddressInput = it.trim() },
                        label = { Text("Target Email Address") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (targetAddressInput.isNotBlank() && !targetAddresses.contains(targetAddressInput)) {
                                targetAddresses = targetAddresses + targetAddressInput
                                targetAddressInput = ""
                            }
                        },
                        enabled = targetAddressInput.isNotBlank() && targetAddressInput.contains("@")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add target", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Targets chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    targetAddresses.forEach { address ->
                        InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text(address) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            targetAddresses = targetAddresses - address
                                        }
                                )
                            }
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreate(
                                CreateRoutingRequest(
                                    domainName = selectedDomain,
                                    prefix = isPrefix,
                                    matchUser = if (isCatchall) "*" else matchUser,
                                    targetAddresses = targetAddresses,
                                    catchall = isCatchall
                                )
                            )
                        },
                        enabled = selectedDomain.isNotBlank() && (isCatchall || matchUser.isNotBlank()) && targetAddresses.isNotEmpty()
                    ) {
                        Text("Create Rule")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAliasGeneratorSheet(
    domains: List<String>,
    users: List<String>,
    onDismiss: () -> Unit,
    onGenerate: (service: String, domain: String, targetEmail: String) -> Unit
) {
    var serviceInput by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf(domains.firstOrNull() ?: "") }
    var selectedTarget by remember { mutableStateOf(users.firstOrNull() ?: "") }

    val presets = listOf("Amazon", "Netflix", "GitHub", "Banking", "Newsletter", "Shopping", "Food Delivery", "Travel")

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Instant Service Alias Generator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Generate a private, unique forwarding alias for signing up to apps & websites. If you ever start receiving spam, simply delete the alias!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Popular Services:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    SuggestionChip(
                        onClick = { serviceInput = preset },
                        label = { Text(preset) }
                    )
                }
            }

            OutlinedTextField(
                value = serviceInput,
                onValueChange = { serviceInput = it },
                label = { Text("Service or Purpose Name") },
                placeholder = { Text("e.g. spotify") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Domain selection chips
            Text("Choose Domain:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                domains.forEach { domain ->
                    FilterChip(
                        selected = selectedDomain == domain,
                        onClick = { selectedDomain = domain },
                        label = { Text(domain) }
                    )
                }
            }

            // Target destination
            Text("Deliver Forwarded Mail To:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = selectedTarget,
                onValueChange = { selectedTarget = it },
                label = { Text("Destination Mailbox") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onGenerate(serviceInput, selectedDomain, selectedTarget)
                },
                enabled = serviceInput.isNotBlank() && selectedDomain.isNotBlank() && selectedTarget.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Copy Alias")
            }
        }
    }
}
