package by.tigre.logger.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import by.tigre.logger.DbLogger
import by.tigre.logger.LogsProvider
import kotlinx.coroutines.launch

open class DebugActivity : ComponentActivity() {

    protected open fun logsProvider(): LogsProvider = DbLogger.getLogsProvider()

    protected open fun createExtraPages(logsProvider: LogsProvider): List<DebugPage> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val provider = logsProvider()
        val pages = listOf(
            object : DebugPage {
                override val title: String = "Logs"

                @Composable
                override fun Content() {
                    DebugLogsScreen(logsProvider = provider)
                }
            },
        ) + createExtraPages(provider)

        setContent {
            MaterialTheme {
                Column(
                    Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                ) {
                    if (pages.size == 1) {
                        pages[0].Content()
                    } else {
                        val pagerState = rememberPagerState { pages.size }
                        val scope = rememberCoroutineScope()

                        ScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                            pages.forEachIndexed { index, page ->
                                Tab(
                                    text = { Text(page.title) },
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            pages[page].Content()
                        }
                    }
                }
            }
        }
    }
}
