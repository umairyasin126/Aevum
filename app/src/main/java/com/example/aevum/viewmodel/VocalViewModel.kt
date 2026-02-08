package com.example.aevum.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aevum.service.StopwatchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VocalViewModel(application: Application) : AndroidViewModel(application), RecognitionListener {

    private var stopwatchService: StopwatchService? = null
    private var isBound = false

    private val _timeMillis = MutableStateFlow(0L)
    val timeMillis: StateFlow<Long> = _timeMillis.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
    private val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as StopwatchService.LocalBinder
            stopwatchService = binder.getService()
            isBound = true
            
            // Observe service flow
            viewModelScope.launch {
                stopwatchService?.timeMillis?.collect {
                    _timeMillis.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            stopwatchService = null
        }
    }

    init {
        Intent(application, StopwatchService::class.java).also { intent ->
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        speechRecognizer.setRecognitionListener(this)
    }
    
    fun startListening() {
        try {
            speechRecognizer.startListening(speechIntent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("VocalViewModel", "Error starting listening", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
         speechRecognizer.stopListening()
         _isListening.value = false
    }

    // RecognitionListener methods
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        // Continuous listening: restart
        _isListening.value = false
        startListening() 
    }
    override fun onError(error: Int) {
         _isListening.value = false
         // Restart on error too (except maybe meaningful errors)
         // startListening() // Careful with loops
         // We'll let the UI control the restart or simple button press for now to avoid infinite loops
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            processCommand(matches[0])
        }
        // Restart listening
        startListening()
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun processCommand(command: String) {
        val cmd = command.lowercase(Locale.getDefault())
        when {
            cmd.contains("start") -> stopwatchService?.start()
            cmd.contains("stop") || cmd.contains("freeze") -> stopwatchService?.stop()
            cmd.contains("reset") -> stopwatchService?.reset()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        if (isBound) {
            getApplication<Application>().unbindService(connection)
            isBound = false
        }
    }
}
