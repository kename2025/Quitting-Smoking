package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuietSmokingDao {
    // Config
    @Query("SELECT * FROM quit_smoking_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<QuitSmokingConfig?>

    @Query("SELECT * FROM quit_smoking_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): QuitSmokingConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: QuitSmokingConfig)

    // Triggers
    @Query("SELECT * FROM habit_triggers ORDER BY id ASC")
    fun getAllTriggersFlow(): Flow<List<HabitTrigger>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: HabitTrigger)

    @Update
    suspend fun updateTrigger(trigger: HabitTrigger)

    @Query("DELETE FROM habit_triggers WHERE id = :id")
    suspend fun deleteTriggerById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTriggers(triggers: List<HabitTrigger>)

    // Craving logs
    @Query("SELECT * FROM craving_logs ORDER BY timestamp DESC")
    fun getAllCravingLogsFlow(): Flow<List<CravingLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCravingLog(log: CravingLog)
}
