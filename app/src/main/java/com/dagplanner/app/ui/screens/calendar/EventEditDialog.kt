package com.dagplanner.app.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditDialog(
    state: EventEditorState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onFieldUpdate: (EventEditorState.() -> EventEditorState) -> Unit,
) {
    val isEditing = state.eventToEdit != null

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var startTimeExpanded by remember { mutableStateOf(false) }
    var endTimeExpanded by remember { mutableStateOf(false) }

    val timeSlots = remember {
        (0..23).flatMap { hour -> listOf(0, 15, 30, 45).map { min -> LocalTime.of(hour, min) } }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Evenement verwijderen?") },
            text = { Text("Dit evenement wordt ook verwijderd uit Google Agenda.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Verwijderen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuleren") }
            }
        )
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { date ->
                onFieldUpdate { copy(startDate = date, endDate = date) }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Evenement bewerken" else "Nieuw evenement") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Titel
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onFieldUpdate { copy(title = it) } },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.title.isBlank(),
                )

                // Hele dag toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hele dag", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.isAllDay,
                        onCheckedChange = { onFieldUpdate { copy(isAllDay = it) } }
                    )
                }

                // Datum (één veld)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatEventDate(state.startDate))
                }

                // Start- en eindtijd als dropdowns (alleen bij niet hele dag)
                if (!state.isAllDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = startTimeExpanded,
                            onExpandedChange = { startTimeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = formatEventTime(state.startTime),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Van") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startTimeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                            )
                            ExposedDropdownMenu(
                                expanded = startTimeExpanded,
                                onDismissRequest = { startTimeExpanded = false }
                            ) {
                                timeSlots.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(formatEventTime(time)) },
                                        onClick = {
                                            onFieldUpdate {
                                                copy(
                                                    startTime = time,
                                                    endTime = if (!endTime.isAfter(time)) time.plusHours(1) else endTime
                                                )
                                            }
                                            startTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("–", style = MaterialTheme.typography.bodyMedium)

                        ExposedDropdownMenuBox(
                            expanded = endTimeExpanded,
                            onExpandedChange = { endTimeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = formatEventTime(state.endTime),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tot") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endTimeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                            )
                            ExposedDropdownMenu(
                                expanded = endTimeExpanded,
                                onDismissRequest = { endTimeExpanded = false }
                            ) {
                                timeSlots.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(formatEventTime(time)) },
                                        onClick = {
                                            onFieldUpdate { copy(endTime = time) }
                                            endTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Locatie
                OutlinedTextField(
                    value = state.location,
                    onValueChange = { onFieldUpdate { copy(location = it) } },
                    label = { Text("Locatie (optioneel)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Beschrijving
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onFieldUpdate { copy(description = it) } },
                    label = { Text("Beschrijving (optioneel)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Verwijderknop (alleen bij bewerken)
                if (isEditing) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        enabled = !state.isSaving
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Evenement verwijderen")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.title.isNotBlank() && !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Opslaan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSaving) { Text("Annuleren") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 86_400_000L
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(LocalDate.ofEpochDay(millis / 86_400_000L))
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuleren") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatEventDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("nl")))
        .replaceFirstChar { it.uppercase() }

private fun formatEventTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm"))
