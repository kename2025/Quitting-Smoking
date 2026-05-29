package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class QuitSmokingRepository(private val dao: QuietSmokingDao) {

    val configFlow: Flow<QuitSmokingConfig?> = dao.getConfigFlow()
    val allTriggersFlow: Flow<List<HabitTrigger>> = dao.getAllTriggersFlow()
    val allCravingsFlow: Flow<List<CravingLog>> = dao.getAllCravingLogsFlow()

    suspend fun getOrInitializeConfig(): QuitSmokingConfig {
        val current = dao.getConfig()
        return if (current == null) {
            val default = QuitSmokingConfig()
            dao.insertConfig(default)
            default
        } else {
            current
        }
    }

    suspend fun saveConfig(config: QuitSmokingConfig) {
        dao.insertConfig(config)
    }

    suspend fun addTrigger(cue: String, behavior: String) {
        dao.insertTrigger(HabitTrigger(cue = cue, behavior = behavior))
    }

    suspend fun deleteTrigger(id: Int) {
        dao.deleteTriggerById(id)
    }

    suspend fun incrementTriggerCount(trigger: HabitTrigger) {
        dao.updateTrigger(trigger.copy(executionCount = trigger.executionCount + 1))
    }

    suspend fun addCravingLog(associatedCue: String? = null, notes: String? = null) {
        dao.insertCravingLog(CravingLog(associatedCue = associatedCue, notes = notes))
    }

    suspend fun populateDefaultsIfEmpty() {
        // Initialize default config if missing
        getOrInitializeConfig()

        // Initialize default triggers if empty
        val currentTriggers = dao.getAllTriggersFlow().firstOrNull() ?: emptyList()
        if (currentTriggers.isEmpty()) {
            val defaults = listOf(
                HabitTrigger(
                    cue = "饭后完成进食",
                    behavior = "立刻起身做5分钟拉伸，或出去散步10分钟",
                    isBuiltIn = true
                ),
                HabitTrigger(
                    cue = "感到工作/生活压力大",
                    behavior = "做5次深呼吸（使用SOS呼吸教练），不让自己握住烟盒",
                    isBuiltIn = true
                ),
                HabitTrigger(
                    cue = "早起配咖啡/茶时",
                    behavior = "换成柠檬温水，改用茶匙搅拌，打乱原本的机械习惯配对",
                    isBuiltIn = true
                ),
                HabitTrigger(
                    cue = "社交聚会有人递烟",
                    behavior = "礼貌微笑并坚定拒绝：“谢了，我正在把身体奖励系统恢复原状呢”，嚼一颗无糖口香糖",
                    isBuiltIn = true
                )
            )
            dao.insertAllTriggers(defaults)
        }
    }
}
