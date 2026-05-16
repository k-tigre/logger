package by.tigre.logger

import bytigreloggerdb.Logs
import kotlinx.coroutines.flow.Flow

interface LogsProvider {
    suspend fun getLogs(offset: Long): List<Logs>
    suspend fun getLogsFlow(offset: Long): Flow<List<Logs>>
    suspend fun getLogs(offset: Long, tagFilter: String): List<Logs>
    suspend fun getLogsFlow(offset: Long, tagFilter: String): Flow<List<Logs>>
}
