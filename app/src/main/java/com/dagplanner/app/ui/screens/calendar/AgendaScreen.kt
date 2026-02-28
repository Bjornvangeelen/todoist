package com.dagplanner.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dagplanner.app.data.model.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
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
    var selectedDay by remember { mutableStateOf(today) }
    var weekStart by remember { mutableStateOf(today.with(DayOfWeek.MONDAY)) }
    // Dialog met alle evenementen van een dag in het weekraster
    var dayEventsDialog by remember { mutableStateOf<Pair<LocalDate, List<CalendarEvent>>?>(null) }

    // Toon alle evenementen van een dag als de gebruiker op "+N meer" tikt
    dayEventsDialog?.let { (date, events) ->
        val locale = Locale("nl")
        val fmt = DateTimeFormatter.ofPattern("EEEE d MMMM", locale)
        AlertDialog(
            onDismissRequest = { dayEventsDialog = null },
            title = { Text(date.format(fmt).replaceFirstChar { it.uppercase() }) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    events.forEach { event ->
                        AgendaEventItem(
                            event = event,
                            onClick = {
                                dayEventsDialog = null
                                viewModel.openEditEventEditor(event)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dayEventsDialog = null }) { Text("Sluiten") }
            }
        )
    }

    if (editorState.isOpen) {
        EventEditDialog(
            state = editorState,
            onDismiss = { viewModel.closeEventEditor() },
            onSave = { viewModel.saveEvent() },
            onDelete = { editorState.eventToEdit?.let { viewModel.deleteEvent(it) } },
            onFieldUpdate = { viewModel.updateEditorField(it) },
        )
    }

    // Zorg dat events geladen zijn voor de zichtbare datum/week
    LaunchedEffect(selectedDay, weekStart, viewMode) {
        when (viewMode) {
            AgendaViewMode.DAY -> viewModel.selectDate(selectedDay)
            AgendaViewMode.WEEK -> viewModel.selectDate(weekStart)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(if (viewMode == AgendaViewMode.DAY) "Dagoverzicht" else "Planningoverzicht")
                    },
                    actions = {
                        IconButton(onClick = {
                            when (viewMode) {
                                AgendaViewMode.DAY -> selectedDay = today
                                AgendaViewMode.WEEK -> weekStart = today.with(DayOfWeek.MONDAY)
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
                AgendaViewMode.DAY -> DayView(
                    selectedDay = selectedDay,
                    monthEvents = monthEvents,
                    today = today,
                    padding = padding,
                    onPreviousDay = { selectedDay = selectedDay.minusDays(1) },
                    onNextDay = { selectedDay = selectedDay.plusDays(1) },
                    onEventClick = { viewModel.openEditEventEditor(it) },
                )
                AgendaViewMode.WEEK -> WeekGridView(
                    weekStart = weekStart,
                    monthEvents = monthEvents,
                    today = today,
                    padding = padding,
                    onPreviousWeek = { weekStart = weekStart.minusWeeks(1) },
                    onNextWeek = { weekStart = weekStart.plusWeeks(1) },
                    onEventClick = { viewModel.openEditEventEditor(it) },
                    onMoreClick = { date, events -> dayEventsDialog = Pair(date, events) },
                )
            }
        }
    }
}

// ─── Dagoverzicht ──────────────────────────────────────────────────────────

@Composable
private fun DayView(
    selectedDay: LocalDate,
    monthEvents: List<CalendarEvent>,
    today: LocalDate,
    padding: PaddingValues,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val locale = Locale("nl")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)
    val dayEvents = remember(monthEvents, selectedDay) {
        monthEvents.filter { it.startDate == selectedDay }
            .sortedWith(compareBy({ !it.isAllDay }, { it.startTime }))
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        // Navigatie
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Vorige dag")
            }
            Text(
                text = selectedDay.format(dateFormatter).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (selectedDay == today) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Volgende dag")
            }
        }
        HorizontalDivider()

        if (dayEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Geen evenementen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp
                    )
            ) {
                dayEvents.forEach { event ->
                    AgendaEventItem(event = event, onClick = { onEventClick(event) })
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

// ─── Planningoverzicht (weekraster) ────────────────────────────────────────

@Composable
private fun WeekGridView(
    weekStart: LocalDate,
    monthEvents: List<CalendarEvent>,
    today: LocalDate,
    padding: PaddingValues,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onMoreClick: (LocalDate, List<CalendarEvent>) -> Unit,
) {
    val eventsByDate = remember(monthEvents) { monthEvents.groupBy { it.startDate } }
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    val weekNumber = weekStart.get(WeekFields.ISO.weekOfWeekBasedYear())
    val locale = Locale("nl")
    val monthYear = weekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))

    Column(
        modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
    ) {
        // Weeknavigatie header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Vorige week")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "WEEK $weekNumber",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    monthYear.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onNextWeek) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Volgende week")
            }
        }
        HorizontalDivider()

        // 2-koloms raster: ma-do links, vr-zo rechts
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Linker kolom: ma, di, wo, do
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                weekDays.take(4).forEach { date ->
                    val dayEvents = (eventsByDate[date] ?: emptyList())
                        .sortedWith(compareBy({ !it.isAllDay }, { it.startTime }))
                    WeekDayCell(
                        date = date,
                        events = dayEvents,
                        isToday = date == today,
                        onEventClick = onEventClick,
                        onMoreClick = { onMoreClick(date, dayEvents) },
                        modifier = Modifier.weight(1f).padding(end = 3.dp, bottom = 3.dp),
                    )
                }
            }
            // Rechter kolom: vr, za, zo + lege cel
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                weekDays.drop(4).forEach { date ->
                    val dayEvents = (eventsByDate[date] ?: emptyList())
                        .sortedWith(compareBy({ !it.isAllDay }, { it.startTime }))
                    WeekDayCell(
                        date = date,
                        events = dayEvents,
                        isToday = date == today,
                        onEventClick = onEventClick,
                        onMoreClick = { onMoreClick(date, dayEvents) },
                        modifier = Modifier.weight(1f).padding(start = 3.dp, bottom = 3.dp),
                    )
                }
                // Lege 4e cel zodat beide kolommen even hoog zijn
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeekDayCell(
    date: LocalDate,
    events: List<CalendarEvent>,
    isToday: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale("nl")
    val dayAbbr = date.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isToday) 1.5.dp else 0.5.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(6.dp)
    ) {
        // Datum-header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .then(
                        if (isToday) Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = dayAbbr,
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))

        // Max 2 evenementen tonen
        val maxShow = 2
        events.take(maxShow).forEach { event ->
            CellEventItem(event = event, onClick = { onEventClick(event) })
            Spacer(Modifier.height(2.dp))
        }
        if (events.size > maxShow) {
            Text(
                text = "+${events.size - maxShow} meer",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onMoreClick() }
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun CellEventItem(event: CalendarEvent, onClick: () -> Unit) {
    val eventColor = event.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
    } ?: MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(eventColor.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(5.dp).clip(CircleShape).background(eventColor)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp
        )
    }
}

// ─── Evenement chip (dagoverzicht) ─────────────────────────────────────────

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
