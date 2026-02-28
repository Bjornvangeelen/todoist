package com.dagplanner.app.ui.screens.email

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.EmailMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailScreen(
    navController: NavController,
    viewModel: EmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.selectedEmail?.let { email ->
        EmailDetailSheet(
            email = email,
            isLoadingBody = uiState.isLoadingBody,
            isReplying = uiState.isReplying,
            replyText = uiState.replyText,
            isSendingReply = uiState.isSendingReply,
            isDeleting = uiState.isDeleting,
            onReplyTextChange = { viewModel.updateReplyText(it) },
            onStartReply = { viewModel.startReply() },
            onCancelReply = { viewModel.cancelReply() },
            onSendReply = { viewModel.sendReply() },
            onDelete = { viewModel.deleteEmail(email) },
            onDismiss = { viewModel.closeEmail() },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-mail") },
                actions = {
                    if (uiState.accountName != null) {
                        IconButton(
                            onClick = { viewModel.loadInbox() },
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Vernieuwen")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            uiState.accountName == null -> NoAccountMessage(padding)

            uiState.isLoading && uiState.emails.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Kon e-mails niet laden",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadInbox() }) { Text("Opnieuw proberen") }
                    }
                }
            }

            uiState.emails.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Geen e-mails gevonden", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    )
                ) {
                    androidx.compose.foundation.lazy.items(uiState.emails, key = { it.id }) { email ->
                        EmailListItem(
                            email = email,
                            onClick = { viewModel.openEmail(email) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        uiState.error?.let { error ->
            if (uiState.emails.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier.padding(padding),
                    action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
                ) { Text(error) }
            }
        }
    }
}

@Composable
private fun NoAccountMessage(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Geen account gekoppeld",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Koppel je Google account via Instellingen\nom je e-mails te bekijken.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun EmailListItem(email: EmailMessage, onClick: () -> Unit) {
    val senderLabel = email.fromName ?: email.from
    val initial = senderLabel.firstOrNull()?.uppercaseChar() ?: '?'
    val avatarColor = remember(email.from) { generateAvatarColor(email.from) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (!email.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatEmailDate(email.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!email.isRead) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = email.subject,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (!email.isRead) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = email.snippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!email.isRead) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailSheet(
    email: EmailMessage,
    isLoadingBody: Boolean,
    isReplying: Boolean,
    replyText: String,
    isSendingReply: Boolean,
    isDeleting: Boolean,
    onReplyTextChange: (String) -> Unit,
    onStartReply: () -> Unit,
    onCancelReply: () -> Unit,
    onSendReply: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("E-mail verwijderen?") },
            text = { Text("De e-mail wordt naar de prullenbak verplaatst.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Verwijderen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuleren") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Onderwerp
            Text(
                text = email.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // Meta
            EmailMetaRow("Van", email.fromName?.let { "$it <${email.from}>" } ?: email.from)
            email.to?.let { EmailMetaRow("Aan", it) }
            EmailMetaRow("Datum", formatEmailDateFull(email.date))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Berichttekst
            if (isLoadingBody) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                val bodyText = email.body
                if (bodyText.isNullOrBlank()) {
                    Text(
                        email.snippet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Actieknoppen
            if (!isReplying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onStartReply,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Beantwoorden")
                    }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        enabled = !isDeleting,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Verwijderen")
                        }
                    }
                }
            } else {
                // Reply compose
                OutlinedTextField(
                    value = replyText,
                    onValueChange = onReplyTextChange,
                    label = { Text("Jouw antwoord") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    minLines = 4,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelReply,
                        modifier = Modifier.weight(1f)
                    ) { Text("Annuleren") }
                    Button(
                        onClick = onSendReply,
                        enabled = replyText.isNotBlank() && !isSendingReply,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSendingReply) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Versturen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailMetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatEmailDate(epochMillis: Long): String {
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = epochMillis }
    return when {
        now.get(Calendar.DATE) == msgCal.get(Calendar.DATE) &&
                now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) ->
            SimpleDateFormat("HH:mm", Locale("nl")).format(Date(epochMillis))
        now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) ->
            SimpleDateFormat("d MMM", Locale("nl")).format(Date(epochMillis))
        else ->
            SimpleDateFormat("d MMM yy", Locale("nl")).format(Date(epochMillis))
    }
}

private fun formatEmailDateFull(epochMillis: Long): String =
    SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", Locale("nl"))
        .format(Date(epochMillis))
        .replaceFirstChar { it.uppercase() }

private fun generateAvatarColor(seed: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFD32F2F),
        Color(0xFF7B1FA2), Color(0xFFF57C00), Color(0xFF0288D1),
        Color(0xFF00796B), Color(0xFFC62828), Color(0xFF4527A0),
    )
    return colors[(seed.hashCode() and 0x7FFFFFFF) % colors.size]
}
