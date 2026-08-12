package by.tigre.logger.debug

import androidx.compose.runtime.Composable
import by.tigre.logger.LogsProvider

interface DebugPage {
    val title: String

    @Composable
    fun Content()
}

/**
 * Extra tab with a pre-filled tag filter (substring match).
 * Hosts can override [DebugActivity.createExtraPages] to add presets like "Analytics".
 */
fun filteredLogsPage(
    title: String,
    initialTagFilter: String,
    logsProvider: LogsProvider,
): DebugPage = object : DebugPage {
    override val title: String = title

    @Composable
    override fun Content() {
        DebugLogsScreen(
            logsProvider = logsProvider,
            initialTagFilter = initialTagFilter,
        )
    }
}
