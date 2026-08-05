package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FrequencyBands
import com.example.data.db.MindDecoderDatabase
import com.example.data.NeuralRepository
import com.example.data.NeuralState
import com.example.data.db.DecodedThoughtEntity
import com.example.data.db.NeuralSessionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MindDecoderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MindDecoderDatabase.getDatabase(application)
    private val repository = NeuralRepository(db.neuralDao())

    val savedSessions: StateFlow<List<NeuralSessionEntity>> = repository.savedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val decodedThoughtsHistory: StateFlow<List<DecodedThoughtEntity>> = repository.decodedThoughts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _neuralState = MutableStateFlow(
        NeuralState(
            mainWaveform = emptyList(),
            channels = emptyList(),
            frequencyBands = FrequencyBands(0.2f, 0.2f, 0.4f, 0.4f, 0.2f),
            confidence = 0.88f,
            latencyMs = 20,
            primaryDecoding = "Initializing Sensor Array...",
            focusIndex = 0.5f,
            relaxationIndex = 0.5f,
            isArtifactDetected = false
        )
    )
    val neuralState: StateFlow<NeuralState> = _neuralState.asStateFlow()

    private val _gain = MutableStateFlow(1.0f)
    val gain: StateFlow<Float> = _gain.asStateFlow()

    private val _isFilterEnabled = MutableStateFlow(true)
    val isFilterEnabled: StateFlow<Boolean> = _isFilterEnabled.asStateFlow()

    private val _selectedChannel = MutableStateFlow(0) // 0 = Composite, 1..8 = specific channel
    val selectedChannel: StateFlow<Int> = _selectedChannel.asStateFlow()

    // Recording session state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private var recordingJob: Job? = null

    // Baseline Calibration
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    private val _calibrationProgress = MutableStateFlow(0f)
    val calibrationProgress: StateFlow<Float> = _calibrationProgress.asStateFlow()

    // AI Thought Decoding
    private val _aiThoughtOutput = MutableStateFlow("")
    val aiThoughtOutput: StateFlow<String> = _aiThoughtOutput.asStateFlow()

    private val _isAiDecodingLoading = MutableStateFlow(false)
    val isAiDecodingLoading: StateFlow<Boolean> = _isAiDecodingLoading.asStateFlow()

    // Biofeedback Trainer
    private val _biofeedbackTargetMode = MutableStateFlow("Focus") // "Focus" or "Relaxation"
    val biofeedbackTargetMode: StateFlow<String> = _biofeedbackTargetMode.asStateFlow()

    private val _biofeedbackScore = MutableStateFlow(750)
    val biofeedbackScore: StateFlow<Int> = _biofeedbackScore.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        startTelemetryStream()
    }

    fun startTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            repository.getNeuralStream(_gain.value, _isFilterEnabled.value).collectLatest { state ->
                _neuralState.value = state

                // Biofeedback score update
                if (_biofeedbackTargetMode.value == "Focus") {
                    if (state.focusIndex > 0.55f) {
                        _biofeedbackScore.value = (_biofeedbackScore.value + 2).coerceAtMost(9990)
                    }
                } else {
                    if (state.relaxationIndex > 0.55f) {
                        _biofeedbackScore.value = (_biofeedbackScore.value + 2).coerceAtMost(9990)
                    }
                }
            }
        }
    }

    fun setGain(newGain: Float) {
        _gain.value = newGain
        startTelemetryStream()
    }

    fun toggleFilter() {
        _isFilterEnabled.value = !_isFilterEnabled.value
        startTelemetryStream()
    }

    fun selectChannel(index: Int) {
        _selectedChannel.value = index
    }

    fun toggleSessionRecording() {
        if (_isRecording.value) {
            // Stop recording & save to Room DB
            _isRecording.value = false
            recordingJob?.cancel()

            val currentState = _neuralState.value
            viewModelScope.launch {
                repository.saveSession(
                    NeuralSessionEntity(
                        durationSeconds = _recordingDurationSec.value,
                        avgConfidence = currentState.confidence,
                        peakState = currentState.primaryDecoding,
                        primaryFrequencyBand = determinePrimaryBand(currentState.frequencyBands),
                        sampleCount = _recordingDurationSec.value * 25,
                        notes = "Neural stream recorded with gain ${_gain.value}x."
                    )
                )
                _recordingDurationSec.value = 0
            }
        } else {
            // Start recording
            _isRecording.value = true
            _recordingDurationSec.value = 0
            recordingJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordingDurationSec.value++
                }
            }
        }
    }

    fun startCalibration() {
        if (_isCalibrating.value) return
        _isCalibrating.value = true
        _calibrationProgress.value = 0f

        viewModelScope.launch {
            for (i in 1..100) {
                delay(150) // 15 seconds total calibration
                _calibrationProgress.value = i / 100f
            }
            _isCalibrating.value = false
        }
    }

    fun runAiThoughtDecoding(customPrompt: String = "") {
        if (_isAiDecodingLoading.value) return
        _isAiDecodingLoading.value = true

        viewModelScope.launch {
            val currentState = _neuralState.value
            val result = repository.decodeThoughtWithAi(currentState, customPrompt)
            _aiThoughtOutput.value = result
            _isAiDecodingLoading.value = false

            // Save decoded thought to history
            repository.saveThought(
                DecodedThoughtEntity(
                    decodedText = currentState.primaryDecoding,
                    stateCategory = if (currentState.focusIndex > 0.5f) "High Focus" else "Deep Relaxation",
                    confidence = currentState.confidence,
                    aiInsight = result
                )
            )
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }

    fun deleteThought(id: Long) {
        viewModelScope.launch { repository.deleteThought(id) }
    }

    fun clearThoughtHistory() {
        viewModelScope.launch { repository.clearThoughts() }
    }

    fun setBiofeedbackTargetMode(mode: String) {
        _biofeedbackTargetMode.value = mode
    }

    private fun determinePrimaryBand(bands: FrequencyBands): String {
        val map = mapOf(
            "Delta" to bands.deltaPower,
            "Theta" to bands.thetaPower,
            "Alpha" to bands.alphaPower,
            "Beta" to bands.betaPower,
            "Gamma" to bands.gammaPower
        )
        return map.maxByOrNull { it.value }?.key ?: "Beta"
    }
}
