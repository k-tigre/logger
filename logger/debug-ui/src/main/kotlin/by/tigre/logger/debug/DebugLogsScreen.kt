package by.tigre.logger.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import by.tigre.logger.LogsProvider
import bytigreloggerdb.Logs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
@Composable
internal fun DebugLogsScreen(
    logsProvider: LogsProvider,
    initialTagFilter: String = "",
) {
    var tagFilter by remember { mutableStateOf(initialTagFilter) }
    var logs by remember { mutableStateOf<List<Logs>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshTick by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS", Locale.US) }

    LaunchedEffect(logsProvider, refreshTick) {
        snapshotFlow { tagFilter.trim() }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    loading = true
                    val source = if (query.isEmpty()) {
                        logsProvider.getLogsFlow(0)
                    } else {
                        logsProvider.getLogsFlow(0, "%$query%")
                    }
                    source
                        .onStart {
                            // brief delay so pull-to-refresh indicator is visible on manual refresh
                            if (refreshTick > 0) delay(150)
                        }
                        .collect { emit(it) }
                }
            }
            .collectLatest { list ->
                logs = list
                loading = false
            }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = tagFilter,
            onValueChange = { tagFilter = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true,
            label = { Text("Tag filter") },
            placeholder = { Text("substring, e.g. Analytics") },
        )

        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = {
                scope.launch {
                    loading = true
                    refreshTick += 1
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (loading && logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = logs, key = { it.id }) { log ->
                        LogCard(log = log, dateFormat = dateFormat)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: Logs, dateFormat: SimpleDateFormat) {
    var isExpanded by remember(log.id) { mutableStateOf(false) }
    val background = when (log.level) {
        "WARN" -> Color.Yellow.copy(alpha = 0.35f)
        "ERROR" -> Color.Red.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(background)
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp),
        ) {
            Text(
                text = dateFormat.format(log.timestemp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Tag: ${log.tag.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "PID: ${log.pid}, Thread: ${log.thread.orEmpty()}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Message: ${log.message.orEmpty()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (isExpanded) {
                val otherFields = log.otherFields
                if (!otherFields.isNullOrBlank()) {
                    Text(
                        text = otherFields,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                val stacktrace = log.stacktrace
                if (!stacktrace.isNullOrBlank()) {
                    Text(
                        text = stacktrace,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
