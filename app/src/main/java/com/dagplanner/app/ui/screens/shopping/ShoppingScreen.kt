package com.dagplanner.app.ui.screens.shopping

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.Task
import com.dagplanner.app.ui.screens.tasks.SwipeableTaskItem
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
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

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
    val active = items.filter { !it.isCompleted }
    val done = items.filter { it.isCompleted }

    // Groepeer actieve items op categorie (label), "Overig" als geen label
    val byCategory: Map<String, List<Task>> = active
        .groupBy { it.label ?: "Overig" }
        .entries
        .sortedWith(compareBy { if (it.key == "Overig") "\uFFFF" else it.key })
        .associate { it.key to it.value }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Boodschappen") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Zoeken in boodschappen...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wis zoekopdracht")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewItemEditor() }) {
                Icon(Icons.Default.Add, contentDescription = "Nieuw artikel")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        if (searchQuery.isNotEmpty()) "Geen artikelen gevonden"
                        else "Nog geen boodschappen",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (searchQuery.isNotEmpty()) "Probeer een andere zoekterm."
                        else "Tik op + om een artikel toe te voegen.",
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
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Actieve items gegroepeerd per categorie
                byCategory.forEach { (category, categoryItems) ->
                    item {
                        TaskGroupHeader(
                            title = category,
                            color = if (category == "Overig") MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        SwipeableTaskItem(
                            task = item,
                            onToggle = { viewModel.toggleComplete(item) },
                            onClick = { viewModel.openEditItemEditor(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                    item { Spacer(Modifier.height(4.dp)) }
                }

                // Afgeronde items
                if (done.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item { TaskGroupHeader("In mandje ✓", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(done, key = { it.id }) { item ->
                        SwipeableTaskItem(
                            task = item,
                            onToggle = { viewModel.toggleComplete(item) },
                            onClick = { viewModel.openEditItemEditor(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }
}
