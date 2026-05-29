package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuitSmokingViewModel(private val repository: QuitSmokingRepository) : ViewModel() {

    // Config & Lists holding database flow
    val configState: StateFlow<QuitSmokingConfig?> = repository.configFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val triggersState: StateFlow<List<HabitTrigger>> = repository.allTriggersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cravingsState: StateFlow<List<CravingLog>> = repository.allCravingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for SOS Breathing exercise
    private val _breathingState = MutableStateFlow<BreathingState>(BreathingState.Idle)
    val breathingState: StateFlow<BreathingState> = _breathingState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.populateDefaultsIfEmpty()
        }
    }

    // ACTIONS
    fun setQuitDate(timestamp: Long?) {
        viewModelScope.launch {
            val current = repository.getOrInitializeConfig()
            repository.saveConfig(current.copy(quitDateTimestamp = timestamp))
        }
    }

    fun updateChecklistStep(step: Int, value: Boolean) {
        viewModelScope.launch {
            val current = repository.getOrInitializeConfig()
            val updated = when (step) {
                2 -> current.copy(stepCleanEnvironment = value)
                3 -> current.copy(stepNrtSetup = value)
                else -> current
            }
            repository.saveConfig(updated)
        }
    }

    fun updateSavingsConfig(cigarettesPerDay: Int, pricePerPack: Double, currency: String) {
        viewModelScope.launch {
            val current = repository.getOrInitializeConfig()
            repository.saveConfig(
                current.copy(
                    cigarettesPerDay = cigarettesPerDay,
                    pricePerPack = pricePerPack,
                    currencySymbol = currency
                )
            )
        }
    }

    fun setPublicCommitments(p1: String, p2: String, p3: String) {
        viewModelScope.launch {
            val current = repository.getOrInitializeConfig()
            repository.saveConfig(
                current.copy(
                    notifiedPerson1 = p1,
                    notifiedPerson2 = p2,
                    notifiedPerson3 = p3,
                    isCommitted = p1.isNotBlank() || p2.isNotBlank() || p3.isNotBlank()
                )
            )
        }
    }

    fun addNewTrigger(cue: String, behavior: String) {
        if (cue.isBlank() || behavior.isBlank()) return
        viewModelScope.launch {
            repository.addTrigger(cue, behavior)
        }
    }

    fun removeTrigger(id: Int) {
        viewModelScope.launch {
            repository.deleteTrigger(id)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = repository.getOrInitializeConfig()
            repository.saveConfig(current.copy(useEnglish = !current.useEnglish))
        }
    }

    fun executeTriggerAction(trigger: HabitTrigger) {
        viewModelScope.launch {
            repository.incrementTriggerCount(trigger)
            val currentConfig = repository.getOrInitializeConfig()
            val notes = if (currentConfig.useEnglish) {
                "Successfully activated coping replacement: ${trigger.behavior}"
            } else {
                "成功启动替代行为：${trigger.behavior}"
            }
            repository.addCravingLog(associatedCue = trigger.cue, notes = notes)
        }
    }

    fun logDirectCraving(cueName: String?, notes: String? = null) {
        viewModelScope.launch {
            val currentConfig = repository.getOrInitializeConfig()
            val defaultNote = if (currentConfig.useEnglish) {
                "Voluntarily suppressed nicotine craving signal"
            } else {
                "自主克制毒瘾信号释放"
            }
            repository.addCravingLog(associatedCue = cueName, notes = notes ?: defaultNote)
        }
    }

    // Breathing Coach Controller
    fun startBreathingExercise(cueName: String? = null) {
        _breathingState.value = BreathingState.Ongoing(
            phase = BreathPhase.IN,
            cycle = 1,
            associatedCue = cueName,
            secondsRemaining = 4
        )
    }

    fun updateBreathingPhase(phase: BreathPhase, cycle: Int, secondsRemaining: Int) {
        val current = _breathingState.value
        if (current is BreathingState.Ongoing) {
            _breathingState.value = current.copy(
                phase = phase,
                cycle = cycle,
                secondsRemaining = secondsRemaining
            )
        }
    }

    fun completeBreathingExercise() {
        val current = _breathingState.value
        if (current is BreathingState.Ongoing) {
            val cue = current.associatedCue
            _breathingState.value = BreathingState.Completed
            viewModelScope.launch {
                val currentConfig = repository.getOrInitializeConfig()
                val note = if (currentConfig.useEnglish) {
                    "Used first-principles deconstruction to relax, successfully completed 5 cycles of deep breathing."
                } else {
                    "使用第一性原理解构放松，顺利撑过5循环呼吸法。"
                }
                logDirectCraving(cueName = cue, notes = note)
            }
        }
    }

    fun resetBreathingState() {
        _breathingState.value = BreathingState.Idle
    }
}

// Breathing states for Custom SOS Canvas Coach
sealed interface BreathingState {
    object Idle : BreathingState
    data class Ongoing(
        val phase: BreathPhase,
        val cycle: Int,
        val associatedCue: String?,
        val secondsRemaining: Int
    ) : BreathingState
    object Completed : BreathingState
}

enum class BreathPhase(val text: String, val durationSec: Int) {
    IN("吸气... 将清爽新鲜的氧气充满肺部", 4),
    HOLD("屏息... 体验神经末梢的平静", 4),
    OUT("呼气... 彻底排出二氧化碳与虚无焦虑", 4)
}

// ViewModel Factory
class QuitSmokingViewModelFactory(private val repository: QuitSmokingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuitSmokingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuitSmokingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
