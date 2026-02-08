package com.example.aevum.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aevum.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OriginViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    
    private val _daysCount = MutableStateFlow(0L)
    val daysCount: StateFlow<Long> = _daysCount.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.startDate.collect { startDate ->
                calculateDays(startDate)
            }
        }
    }

    private fun calculateDays(startDate: Long?) {
        if (startDate == null) {
            _daysCount.value = 0
            return
        }
        val current = System.currentTimeMillis()
        val diff = current - startDate
        // Only count full days? Or assume 0 if same day.
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        _daysCount.value = if (days < 0) 0 else days
    }

    fun onReset() {
        viewModelScope.launch {
            preferences.setStartDate(System.currentTimeMillis())
        }
    }
}
