package com.dagplanner.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.data.model.TaskPriority
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    if (editorState.isOpen) {
        TaskEditDialog(
            state = editorState,
            onDismiss = { viewModel.closeEditor() },
            onSave = { viewModel.saveTask() },
            onDelete = { editorState.taskToEdit?.let { viewModel.deleteTask(it) } },
            onFieldUpdate = { viewModel.updateField(it) },
        )
    }

    val today = LocalDate.now()

    // Groepeer taken
    val overdue = tasks.filter { !it.isCompleted && it.deadline != null && it.deadline.isBefore(today) }
    val todayTasks = tasks.filter { !it.isCompleted && it.date == today && (it.deadline == null || !it.deadline.isBefore(today)) }
    val upcoming = tasks.filter {
        !it.isCompleted && it.date != null && it.date.isAfter(today) &&
        (it.deadline == null || !it.deadline.isBefore(today))
    }
    val noDate = tasks.filter { !it.isCompleted && it.date == null && (it.deadline == null || !it.deadline.isBefore(today)) }
    val completed = tasks.filter { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taken") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewTaskEditor() }) {
                Icon(Icons.Default.Add, contentDescription = "Nieuwe taak")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nog geen taken",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tik op + om je eerste taak toe te voegen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (overdue.isNotEmpty()) {
                    item { TaskGroupHeader("Verlopen", color = MaterialTheme.colorScheme.error) }
                    items(overdue, key = { it.id }) { task ->
                        TaskCard(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (todayTasks.isNotEmpty()) {
                    item { TaskGroupHeader("Vandaag") }
                    items(todayTasks, key = { it.id }) { task ->
                        TaskCard(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (upcoming.isNotEmpty()) {
                    item { TaskGroupHeader("Gepland") }
                    items(upcoming, key = { it.id }) { task ->
                        TaskCard(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (noDate.isNotEmpty()) {
                    item { TaskGroupHeader("Geen datum") }
                    items(noDate, key = { it.id }) { task ->
                        TaskCard(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (completed.isNotEmpty()) {
                    item { TaskGroupHeader("Afgerond", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(completed, key = { it.id }) { task ->
                        TaskCard(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskGroupHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    val prioColor = priorityColor(task.priority)
    val today = LocalDate.now()
    val isOverdue = !task.isCompleted && task.deadline != null && task.deadline.isBefore(today)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Prioriteitsstreep links
            if (task.priority != TaskPriority.NONE) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(prioColor)
                )
                Spacer(Modifier.width(10.dp))
            }

            // Checkbox
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Afgerond" else "Markeer als afgerond",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Titel
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Metadata rij
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Datum + tijd
                    if (task.date != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = if (isOverdue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = buildString {
                                    append(formatTaskDate(task.date))
                                    task.time?.let { append(" ${formatTaskTime(it)}") }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Deadline (als afwijkend van datum)
                    if (task.deadline != null && task.deadline != task.date) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "deadline ${formatTaskDate(task.deadline)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Label
                    task.label?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Locatie
                if (!task.location.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = task.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Herinnering icoon
            if (task.reminder != null) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Herinnering",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
