package com.dagplanner.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dagplanner.app.data.model.TaskPriority
import com.dagplanner.app.ui.components.LocationAutocompleteField
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val reminderOptions = listOf(
    null to "Geen herinnering",
    "5min" to "5 minuten van tevoren",
    "15min" to "15 minuten van tevoren",
    "30min" to "30 minuten van tevoren",
    "1h" to "1 uur van tevoren",
    "1d" to "1 dag van tevoren",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    state: TaskEditorState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onFieldUpdate: (TaskEditorState.() -> TaskEditorState) -> Unit,
) {
    val isEditing = state.taskToEdit != null

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var reminderExpanded by remember { mutableStateOf(false) }

    val timeSlots = remember {
        (0..23).flatMap { hour -> listOf(0, 15, 30, 45).map { min -> LocalTime.of(hour, min) } }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Taak verwijderen?") },
            text = { Text("Weet je zeker dat je deze taak wilt verwijderen?") },
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
        TaskDatePickerDialog(
            initialDate = state.date ?: LocalDate.now(),
            onDateSelected = { date ->
                onFieldUpdate { copy(date = date) }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showDeadlinePicker) {
        TaskDatePickerDialog(
            initialDate = state.deadline ?: LocalDate.now(),
            onDateSelected = { date ->
                onFieldUpdate { copy(deadline = date) }
                showDeadlinePicker = false
            },
            onDismiss = { showDeadlinePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Taak bewerken" else "Nieuwe taak") },
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
                    label = { Text("Taak omschrijving") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.title.isBlank(),
                )

                // Datum (1 veld)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            state.date?.let { formatTaskDate(it) } ?: "Datum",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.date != null) {
                        TextButton(onClick = { onFieldUpdate { copy(date = null, time = null) } }) {
                            Text("Wis", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Tijd dropdown (alleen als datum is ingesteld)
                if (state.date != null) {
                    ExposedDropdownMenuBox(
                        expanded = timeExpanded,
                        onExpandedChange = { timeExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.time?.let { formatTaskTime(it) } ?: "Geen tijd",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tijdstip") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true,
                        )
                        ExposedDropdownMenu(
                            expanded = timeExpanded,
                            onDismissRequest = { timeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Geen tijd") },
                                onClick = {
                                    onFieldUpdate { copy(time = null) }
                                    timeExpanded = false
                                }
                            )
                            timeSlots.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(formatTaskTime(time)) },
                                    onClick = {
                                        onFieldUpdate { copy(time = time) }
                                        timeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Label
                OutlinedTextField(
                    value = state.label,
                    onValueChange = { onFieldUpdate { copy(label = it) } },
                    label = { Text("Label (optioneel)") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Prioriteit
                Text(
                    "Prioriteit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.entries.forEach { prio ->
                        FilterChip(
                            selected = state.priority == prio,
                            onClick = { onFieldUpdate { copy(priority = prio) } },
                            label = { Text(prio.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priorityColor(prio).copy(alpha = 0.2f),
                                selectedLabelColor = priorityColor(prio),
                            )
                        )
                    }
                }

                // Deadline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeadlinePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            state.deadline?.let { "Deadline: ${formatTaskDate(it)}" } ?: "Deadline (optioneel)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.deadline != null) {
                        TextButton(onClick = { onFieldUpdate { copy(deadline = null) } }) {
                            Text("Wis", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Locatie
                LocationAutocompleteField(
                    value = state.location,
                    onValueChange = { onFieldUpdate { copy(location = it) } },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Herinnering dropdown
                ExposedDropdownMenuBox(
                    expanded = reminderExpanded,
                    onExpandedChange = { reminderExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = reminderOptions.find { it.first == state.reminder }?.second ?: "Geen herinnering",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Herinnering") },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = reminderExpanded,
                        onDismissRequest = { reminderExpanded = false }
                    ) {
                        reminderOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onFieldUpdate { copy(reminder = value) }
                                    reminderExpanded = false
                                }
                            )
                        }
                    }
                }

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
                        Text("Taak verwijderen")
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
private fun TaskDatePickerDialog(
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

@Composable
fun priorityColor(priority: TaskPriority) = when (priority) {
    TaskPriority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    TaskPriority.LOW -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    TaskPriority.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFF9800)
    TaskPriority.HIGH -> androidx.compose.ui.graphics.Color(0xFFF44336)
}

fun formatTaskDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("nl")))

fun formatTaskTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm"))
