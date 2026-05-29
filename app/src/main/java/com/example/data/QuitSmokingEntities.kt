package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quit_smoking_config")
data class QuitSmokingConfig(
    @PrimaryKey val id: Int = 1,
    val quitDateTimestamp: Long? = null, // When the user starts quitting
    val cigarettesPerDay: Int = 15,     // Daily amount before quitting
    val pricePerPack: Double = 25.0,    // Average pack price
    val currencySymbol: String = "¥",
    val notifiedPerson1: String = "",
    val notifiedPerson2: String = "",
    val notifiedPerson3: String = "",
    val stepCleanEnvironment: Boolean = false,
    val stepNrtSetup: Boolean = false,
    val isCommitted: Boolean = false,
    val useEnglish: Boolean = false
)

@Entity(tableName = "habit_triggers")
data class HabitTrigger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cue: String,            // What triggers the urge (e.g., "饭后")
    val behavior: String,       // What to do instead (e.g., "去散步")
    val isBuiltIn: Boolean = false,
    val executionCount: Int = 0  // How many times successfully executed
)

@Entity(tableName = "craving_logs")
data class CravingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val associatedCue: String? = null,
    val notes: String? = null
)
