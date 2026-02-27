package com.dagplanner.app.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.ui.theme.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                account.email?.let { email -> viewModel.onGoogleAccountLinked(email) }
            } catch (e: ApiException) { }
        }
    }

    fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instellingen") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Berichten
            uiState.syncMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
                    }
                }
            }

            // ── Thema kiezen ───────────────────────────────────────────
            SettingsSection(title = "Thema") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Huidig: ${uiState.selectedTheme.themeName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    // 2 rijen van 5 thema's
                    val themes = AppTheme.entries
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in themes.chunked(5)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { theme ->
                                    ThemeSwatch(
                                        theme = theme,
                                        isSelected = uiState.selectedTheme == theme,
                                        onClick = { viewModel.setTheme(theme) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Google Agenda ──────────────────────────────────────────
            SettingsSection(title = "Google Agenda") {
                if (uiState.googleAccountName != null) {
                    ListItem(
                        headlineContent = { Text("Gekoppeld account") },
                        supportingContent = { Text(uiState.googleAccountName!!) },
                        leadingContent = {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Nu synchroniseren") },
                        supportingContent = { Text("Evenementen ophalen van Google Agenda") },
                        leadingContent = {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickableIfEnabled(!uiState.isSyncing) { viewModel.syncNow() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = {
                            Text("Ontkoppelen", color = MaterialTheme.colorScheme.error)
                        },
                        supportingContent = { Text("Verwijder de koppeling met Google Agenda") },
                        leadingContent = {
                            Icon(Icons.Default.LinkOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier.clickableIfEnabled(true) { viewModel.unlinkGoogleAccount() }
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("Google Agenda koppelen") },
                        supportingContent = { Text("Koppel je Google account om je agenda-evenementen te bekijken") },
                        leadingContent = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = { Icon(Icons.Default.Link, contentDescription = null) },
                        modifier = Modifier.clickableIfEnabled(true) { startGoogleSignIn() }
                    )
                }
            }

            // ── Over de app ────────────────────────────────────────────
            SettingsSection(title = "Over") {
                ListItem(
                    headlineContent = { Text("DagPlanner") },
                    supportingContent = { Text("Versie 1.0.0") },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Privacy") },
                    supportingContent = {
                        Text("Deze app slaat agenda-gegevens lokaal op je apparaat op. Er worden geen gegevens naar externe servers gestuurd.")
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeSwatch(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(theme.previewColor)
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else Modifier.border(1.dp, Color.Transparent, CircleShape)
                )
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = theme.themeName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(content = content)
        }
    }
}

fun Modifier.clickableIfEnabled(enabled: Boolean, onClick: () -> Unit): Modifier =
    if (enabled) this.then(Modifier.clickable(onClick = onClick))
    else this
