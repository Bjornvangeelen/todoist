package com.dagplanner.app.ui.components

import android.location.Geocoder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.length >= 3) {
            delay(400)
            val addresses = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale("nl")).getFromLocationName(value, 5)
                }.getOrNull()
            }
            suggestions = addresses?.mapNotNull { addr ->
                buildString {
                    addr.thoroughfare?.let { append(it) }
                    addr.subThoroughfare?.let { append(" $it") }
                    addr.locality?.let { if (isNotEmpty()) append(", "); append(it) }
                    addr.countryName?.let { if (isNotEmpty()) append(", "); append(it) }
                }.trim().ifBlank { null }
            } ?: emptyList()
            expanded = suggestions.isNotEmpty()
        } else {
            suggestions = emptyList()
            expanded = false
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!it) expanded = false },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (it.length < 3) { expanded = false; suggestions = emptyList() }
            },
            label = { Text("Locatie (optioneel)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.menuAnchor(),
        )
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
