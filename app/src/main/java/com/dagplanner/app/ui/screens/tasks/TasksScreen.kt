package com.dagplanner.app.ui.screens.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSortMenu by remember { mutableStateOf(false) }

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
    val overdue = tasks.filter { !it.isCompleted && it.deadline != null && it.deadline.isBefore(today) }
    val todayTasks = tasks.filter { !it.isCompleted && it.date == today && (it.deadline == null || !it.deadline.isBefore(today)) }
    val upcoming = tasks.filter {
        !it.isCompleted && it.date != null && it.date.isAfter(today) &&
        (it.deadline == null || !it.deadline.isBefore(today))
    }
    val noDate = tasks.filter { !it.isCompleted && it.date == null && (it.deadline == null || !it.deadline.isBefore(today)) }
    val completed = tasks.filter { it.isCompleted }

    // Undo-verwijder helper
    fun handleDelete(task: Task) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "\"${task.title}\" verwijderd",
                actionLabel = "Ongedaan",
                duration = SnackbarDuration.Short,
            )
            if (result != SnackbarResult.ActionPerformed) {
                viewModel.deleteTask(task)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Taken") },
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sorteren")
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                TaskSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (sortOrder == order) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Spacer(Modifier.size(16.dp))
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                Text(order.label)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Zoeken in taken...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wis")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewTaskEditor() }) {
                Icon(Icons.Default.Add, contentDescription = "Nieuwe taak")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(if (searchQuery.isNotEmpty()) "Geen taken gevonden" else "Nog geen taken", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (searchQuery.isNotEmpty()) "Probeer een andere zoekterm." else "Tik op + om je eerste taak toe te voegen.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (overdue.isNotEmpty()) {
                    item { TaskGroupHeader("Verlopen", color = MaterialTheme.colorScheme.error) }
                    items(overdue, key = { it.id }) { task ->
                        SwipeableTaskItem(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) }, onDelete = { handleDelete(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                if (todayTasks.isNotEmpty()) {
                    item { TaskGroupHeader("Vandaag") }
                    items(todayTasks, key = { it.id }) { task ->
                        SwipeableTaskItem(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) }, onDelete = { handleDelete(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                if (upcoming.isNotEmpty()) {
                    item { TaskGroupHeader("Gepland") }
                    items(upcoming, key = { it.id }) { task ->
                        SwipeableTaskItem(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) }, onDelete = { handleDelete(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                if (noDate.isNotEmpty()) {
                    item { TaskGroupHeader("Geen datum") }
                    items(noDate, key = { it.id }) { task ->
                        SwipeableTaskItem(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) }, onDelete = { handleDelete(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                if (completed.isNotEmpty()) {
                    item { TaskGroupHeader("Afgerond", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(completed, key = { it.id }) { task ->
                        SwipeableTaskItem(task = task, onToggle = { viewModel.toggleComplete(task) }, onClick = { viewModel.openEditTaskEditor(task) }, onDelete = { handleDelete(task) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onToggle(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); false }
                else -> false
            }
        },
        positionalThreshold = { it * 0.4f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                    else -> Color.Transparent
                },
                label = "swipeColor"
            )
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(color).padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    SwipeToDismissBoxValue.EndToStart -> Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    else -> {}
                }
            }
        },
        content = { TaskCard(task = task, onToggle = onToggle, onClick = onClick) }
    )
}

@Composable
fun TaskGroupHeader(title: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = color, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onClick: () -> Unit) {
    val prioColor = priorityColor(task.priority)
    val today = LocalDate.now()
    val isOverdue = !task.isCompleted && task.deadline != null && task.deadline.isBefore(today)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.Top) {
            if (task.priority != TaskPriority.NONE) {
                Box(modifier = Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(prioColor))
                Spacer(Modifier.width(10.dp))
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (task.date != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(11.dp), tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = buildString {
                                    append(formatTaskDate(task.date))
                                    task.time?.let { append(" ${formatTaskTime(it)}") }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (task.deadline != null && task.deadline != task.date) {
                        Text("deadline ${formatTaskDate(task.deadline)}", style = MaterialTheme.typography.bodySmall, color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    task.label?.let { label ->
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)).padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    if (task.recurrence.name != "NONE") {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!task.location.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(2.dp))
                        Text(task.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (task.reminder != null) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
