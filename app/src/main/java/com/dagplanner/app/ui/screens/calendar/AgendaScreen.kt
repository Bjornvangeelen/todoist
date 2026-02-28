package com.dagplanner.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.CalendarEvent
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AgendaViewMode { DAY, WEEK }

data class DayAgendaItem(
    val date: LocalDate,
    val events: List<CalendarEvent>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthEvents by viewModel.monthEvents.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    var viewMode by remember { mutableStateOf(AgendaViewMode.DAY) }
    val today = LocalDate.now()
    var weekStart by remember { mutableStateOf(today.with(DayOfWeek.MONDAY)) }
    val coroutineScope = rememberCoroutineScope()

    if (editorState.isOpen) {
        EventEditDialog(
            state = editorState,
            onDismiss = { viewModel.closeEventEditor() },
            onSave = { viewModel.saveEvent() },
            onDelete = { editorState.eventToEdit?.let { viewModel.deleteEvent(it) } },
            onFieldUpdate = { viewModel.updateEditorField(it) },
        )
    }

    val agendaItems = remember(monthEvents, uiState.displayedMonth) {
        buildAgendaItems(monthEvents, uiState.displayedMonth)
    }

    val listState = rememberLazyListState()

    // Scroll naar vandaag bij eerste keer laden
    LaunchedEffect(agendaItems) {
        val todayIndex = agendaItems.indexOfFirst { it.date >= today }
        if (todayIndex >= 0) listState.scrollToItem(todayIndex)
    }

    // Zorg dat evenementen geladen worden voor de zichtbare week
    LaunchedEffect(weekStart) {
        viewModel.selectDate(weekStart)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (viewMode == AgendaViewMode.DAY) "Dagoverzicht"
                            else "Planningoverzicht"
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            when (viewMode) {
                                AgendaViewMode.DAY -> {
                                    coroutineScope.launch {
                                        val todayIndex = agendaItems.indexOfFirst { it.date >= today }
                                        if (todayIndex >= 0) listState.animateScrollToItem(todayIndex)
                                    }
                                }
                                AgendaViewMode.WEEK -> {
                                    weekStart = today.with(DayOfWeek.MONDAY)
                                }
                            }
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Vandaag")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(selectedTabIndex = viewMode.ordinal) {
                    Tab(
                        selected = viewMode == AgendaViewMode.DAY,
                        onClick = { viewMode = AgendaViewMode.DAY },
                        text = { Text("Dag") }
                    )
                    Tab(
                        selected = viewMode == AgendaViewMode.WEEK,
                        onClick = { viewMode = AgendaViewMode.WEEK },
                        text = { Text("Planning") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.googleAccountName != null) {
                FloatingActionButton(onClick = { viewModel.openNewEventEditor() }) {
                    Icon(Icons.Default.Add, contentDescription = "Nieuw evenement")
                }
            }
        }
    ) { padding ->
        if (uiState.googleAccountName == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Geen agenda gekoppeld",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Koppel je Google Agenda via Instellingen\nom je evenementen te bekijken.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (viewMode) {
                AgendaViewMode.DAY -> {
                    if (agendaItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp,
                                top = padding.calculateTopPadding() + 8.dp,
                                bottom = padding.calculateBottomPadding() + 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(agendaItems, key = { it.date.toString() }) { dayItem ->
                                AgendaDaySection(
                                    dayItem = dayItem,
                                    isToday = dayItem.date == today,
                                    onEventClick = { viewModel.openEditEventEditor(it) }
                                )
                            }
                        }
                    }
                }
                AgendaViewMode.WEEK -> {
                    WeekPlanningView(
                        weekStart = weekStart,
                        monthEvents = monthEvents,
                        today = today,
                        padding = padding,
                        onPreviousWeek = { weekStart = weekStart.minusWeeks(1) },
                        onNextWeek = { weekStart = weekStart.plusWeeks(1) },
                        onEventClick = { viewModel.openEditEventEditor(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekPlanningView(
    weekStart: LocalDate,
    monthEvents: List<CalendarEvent>,
    today: LocalDate,
    padding: PaddingValues,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val weekEnd = weekStart.plusDays(6)
    val eventsByDate = remember(monthEvents) { monthEvents.groupBy { it.startDate } }
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())
    ) {
        WeekNavigationHeader(
            weekStart = weekStart,
            weekEnd = weekEnd,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
        )
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = padding.calculateBottomPadding() + 16.dp)
        ) {
            weekDays.forEach { date ->
                val dayEvents = (eventsByDate[date] ?: emptyList()).sortedWith(
                    compareBy({ !it.isAllDay }, { it.startTime })
                )
                AgendaDaySection(
                    dayItem = DayAgendaItem(date = date, events = dayEvents),
                    isToday = date == today,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
private fun WeekNavigationHeader(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val locale = Locale("nl")
    val startFormatter = DateTimeFormatter.ofPattern(
        if (weekStart.month == weekEnd.month) "d" else "d MMM",
        locale
    )
    val endFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    val rangeText = "${weekStart.format(startFormatter)} – ${weekEnd.format(endFormatter)}"
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Vorige week")
        }
        Text(
            text = rangeText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Volgende week")
        }
    }
}

@Composable
fun AgendaDaySection(
    dayItem: DayAgendaItem,
    isToday: Boolean,
    onEventClick: (CalendarEvent) -> Unit = {},
) {
    val date = dayItem.date
    val dayFormatter = DateTimeFormatter.ofPattern("d", Locale("nl"))
    val dayNameFormatter = DateTimeFormatter.ofPattern("EEE", Locale("nl"))
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("nl"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Datumkolom (links)
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.format(dayNameFormatter).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .then(
                        if (isToday) Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            if (date.dayOfMonth == 1) {
                Text(
                    text = date.format(monthFormatter).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Evenementen kolom (rechts)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = 8.dp)
        ) {
            if (dayItem.events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "Geen evenementen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                dayItem.events.forEach { event ->
                    AgendaEventItem(event = event, onClick = { onEventClick(event) })
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}

@Composable
fun AgendaEventItem(event: CalendarEvent, onClick: (() -> Unit)? = null) {
    val eventColor = event.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) }
        catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(eventColor.copy(alpha = 0.1f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(eventColor)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!event.isAllDay) {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val startStr = event.startTime?.format(timeFormatter) ?: ""
                val endStr = event.endTime?.format(timeFormatter) ?: ""
                Text(
                    text = if (endStr.isNotEmpty()) "$startStr – $endStr" else startStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Hele dag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            event.location?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Bouwt een lijst van DayAgendaItem voor de komende 3 maanden,
 * met alle evenementen per dag.
 */
private fun buildAgendaItems(
    events: List<CalendarEvent>,
    startMonth: YearMonth
): List<DayAgendaItem> {
    val today = LocalDate.now()
    val startDate = minOf(today, startMonth.atDay(1))
    val endDate = startMonth.plusMonths(2).atEndOfMonth()

    val eventsByDate = events.groupBy { it.startDate }

    val items = mutableListOf<DayAgendaItem>()
    var current = startDate
    while (!current.isAfter(endDate)) {
        val dayEvents = eventsByDate[current] ?: emptyList()
        if (dayEvents.isNotEmpty() || current == today || !current.isBefore(today.minusDays(1))) {
            items.add(DayAgendaItem(date = current, events = dayEvents.sortedWith(
                compareBy({ !it.isAllDay }, { it.startTime })
            )))
        }
        current = current.plusDays(1)
    }
    return items
}
