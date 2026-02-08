package com.example.aevum.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.TimeUnit

class StopwatchService : Service(), TextToSpeech.OnInitListener {

    inner class LocalBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }

    private val binder = LocalBinder()
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private var startTime = 0L
    private var accumulatedTime = 0L
    private var _isRunning = false

    private val _timeMillis = MutableStateFlow(0L)
    val timeMillis: StateFlow<Long> = _timeMillis.asStateFlow()

    private val _isRunningFlow = MutableStateFlow(false)
    val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle error
            } else {
                isTtsReady = true
            }
        }
    }

    fun start() {
        if (_isRunning) return
        startTime = System.currentTimeMillis()
        _isRunning = true
        _isRunningFlow.value = true
        
        timerJob = serviceScope.launch {
            while (_isRunning) {
                val current = System.currentTimeMillis()
                val elapsed = accumulatedTime + (current - startTime)
                _timeMillis.value = elapsed
                
                checkAnnouncement(elapsed)
                
                delay(100) // Update UI frequently
            }
        }
    }

    fun stop() {
        if (!_isRunning) return
        _isRunning = false
        _isRunningFlow.value = false
        val current = System.currentTimeMillis()
        accumulatedTime += (current - startTime)
        timerJob?.cancel()
    }

    fun reset() {
        stop()
        accumulatedTime = 0L
        _timeMillis.value = 0L
    }

    private var lastAnnouncedMinute = 0L

    private fun checkAnnouncement(millis: Long) {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)

        // Announce every minute
        if (minutes > lastAnnouncedMinute) {
            lastAnnouncedMinute = minutes
            speakTime(minutes, 0)
        }
    }

    private fun speakTime(minutes: Long, seconds: Long) {
        if (!isTtsReady) return
        val text = if (minutes > 0) "$minutes minutes" else "$seconds seconds"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }
}
