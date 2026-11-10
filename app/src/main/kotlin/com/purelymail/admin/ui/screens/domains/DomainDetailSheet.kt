package com.purelymail.admin.ui.screens.domains

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purelymail.admin.data.model.ApiDomainInfo
import com.purelymail.admin.data.model.UpdateDomainSettingsRequest
import com.purelymail.admin.ui.components.ConfirmDeleteDialog
import com.purelymail.admin.ui.components.CopyableRow
import com.purelymail.admin.ui.components.DnsBadgeRow
import com.purelymail.admin.ui.components.copyToClipboard
import com.purelymail.admin.ui.theme.PurelyPurplePrimary
import com.purelymail.admin.ui.theme.StatusSuccess
import com.purelymail.admin.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainDetailSheet(
    domain: ApiDomainInfo,
    ownershipCode: String?,
    onDismiss: () -> Unit,
    onUpdateSettings: (UpdateDomainSettingsRequest) -> Unit,
    onDeleteDomain: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var allowAccountReset by remember { mutableStateOf(domain.allowAccountReset) }
    var symbolicSubaddressing by remember { mutableStateOf(domain.symbolicSubaddressing) }

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
                            .background(Color(0xFF00B0FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Color(0xFF00B0FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = domain.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (domain.isShared) "Shared Domain (Purelymail Managed)" else "Custom Domain",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { copyToClipboard(context, "Domain Name", domain.name) }) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            // DNS Health Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (domain.dnsSummary.isFullyPassing) {
                        StatusSuccess.copy(alpha = 0.08f)
                    } else {
                        StatusWarning.copy(alpha = 0.08f)
                    }
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DNS Health Check",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        DnsBadgeRow(dnsSummary = domain.dnsSummary)
                    }

                    Text(
                        text = if (domain.dnsSummary.isFullyPassing) {
                            "All DNS records (MX, SPF, DKIM, DMARC) are fully verified and passing."
                        } else {
                            "Some DNS records are missing or unverified. Emails may bounce or land in spam until fixed."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            onUpdateSettings(
                                UpdateDomainSettingsRequest(
                                    name = domain.name,
                                    recheckDns = true
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recheck DNS Now")
                    }
                }
            }

            // DNS Setup Instructions Cheat Sheet
            if (!domain.isShared) {
                Text(
                    text = "Required DNS Records",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (ownershipCode != null) {
                    CopyableRow(
                        label = "Ownership TXT Record (Host: @)",
                        value = ownershipCode
                    )
                }

                CopyableRow(
                    label = "MX Record (Priority: 10, Host: @)",
                    value = "mail.purelymail.com"
                )

                CopyableRow(
                    label = "SPF TXT Record (Host: @)",
                    value = "v=spf1 include:_spf.purelymail.com ~all"
                )

                CopyableRow(
                    label = "DMARC TXT Record (Host: _dmarc)",
                    value = "v=DMARC1; p=quarantine;"
                )
            }

            HorizontalDivider()

            // Domain Settings
            if (!domain.isShared) {
                Text(
                    text = "Domain Options",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Symbolic Subaddressing", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Allows plus-tagging (user+tag@domain)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = symbolicSubaddressing,
                        onCheckedChange = {
                            symbolicSubaddressing = it
                            onUpdateSettings(
                                UpdateDomainSettingsRequest(
                                    name = domain.name,
                                    symbolicSubaddressing = it
                                )
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Account Reset", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Allow resetting account password from this domain", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = allowAccountReset,
                        onCheckedChange = {
                            allowAccountReset = it
                            onUpdateSettings(
                                UpdateDomainSettingsRequest(
                                    name = domain.name,
                                    allowAccountReset = it
                                )
                            )
                        }
                    )
                }

                HorizontalDivider()

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Domain")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Domain?",
            message = "Are you sure you want to delete '${domain.name}'? All mailboxes, aliases, and settings for this domain will be permanently deleted.",
            onConfirm = {
                onDeleteDomain(domain.name)
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
