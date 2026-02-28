package com.dagplanner.app.ui.screens.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.ui.screens.tasks.TaskCard
import com.dagplanner.app.ui.screens.tasks.TaskEditDialog
import com.dagplanner.app.ui.screens.tasks.TaskGroupHeader
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    navController: NavController,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    if (editorState.isOpen) {
        TaskEditDialog(
            state = editorState,
            onDismiss = { viewModel.closeEditor() },
            onSave = { viewModel.saveItem() },
            onDelete = { editorState.taskToEdit?.let { viewModel.deleteItem(it) } },
            onFieldUpdate = { viewModel.updateField(it) },
        )
    }

    val today = LocalDate.now()

    val overdue = items.filter { !it.isCompleted && it.deadline != null && it.deadline.isBefore(today) }
    val todayItems = items.filter { !it.isCompleted && it.date == today && (it.deadline == null || !it.deadline.isBefore(today)) }
    val upcoming = items.filter {
        !it.isCompleted && it.date != null && it.date.isAfter(today) &&
        (it.deadline == null || !it.deadline.isBefore(today))
    }
    val noDate = items.filter { !it.isCompleted && it.date == null && (it.deadline == null || !it.deadline.isBefore(today)) }
    val completed = items.filter { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boodschappen") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewItemEditor() }) {
                Icon(Icons.Default.Add, contentDescription = "Nieuw artikel")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nog geen boodschappen",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tik op + om een artikel toe te voegen.",
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
                    items(overdue, key = { it.id }) { item ->
                        TaskCard(task = item, onToggle = { viewModel.toggleComplete(item) }, onClick = { viewModel.openEditItemEditor(item) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (todayItems.isNotEmpty()) {
                    item { TaskGroupHeader("Vandaag") }
                    items(todayItems, key = { it.id }) { item ->
                        TaskCard(task = item, onToggle = { viewModel.toggleComplete(item) }, onClick = { viewModel.openEditItemEditor(item) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (upcoming.isNotEmpty()) {
                    item { TaskGroupHeader("Gepland") }
                    items(upcoming, key = { it.id }) { item ->
                        TaskCard(task = item, onToggle = { viewModel.toggleComplete(item) }, onClick = { viewModel.openEditItemEditor(item) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (noDate.isNotEmpty()) {
                    item { TaskGroupHeader("Te kopen") }
                    items(noDate, key = { it.id }) { item ->
                        TaskCard(task = item, onToggle = { viewModel.toggleComplete(item) }, onClick = { viewModel.openEditItemEditor(item) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (completed.isNotEmpty()) {
                    item { TaskGroupHeader("In mandje", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(completed, key = { it.id }) { item ->
                        TaskCard(task = item, onToggle = { viewModel.toggleComplete(item) }, onClick = { viewModel.openEditItemEditor(item) })
                    }
                }
            }
        }
    }
}
