package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin

data class NeuralChannel(
    val name: String, // e.g. "Fp1", "Fp2", "C3", "C4", "O1", "O2", "T3", "T4"
    val region: String, // "Prefrontal", "Motor Cortex", "Occipital", "Temporal"
    val signal: List<Float>,
    val impedanceOhm: Int, // Electrode contact quality (e.g. 5 kOhm = Good)
    val isConnected: Boolean = true
)

data class FrequencyBands(
    val deltaPower: Float, // 0.5 - 4 Hz (Deep Sleep / Healing)
    val thetaPower: Float, // 4 - 8 Hz (Meditation / Intuition)
    val alphaPower: Float, // 8 - 12 Hz (Relaxed Alertness / Calm)
    val betaPower: Float,  // 12 - 30 Hz (Active Concentration / Logic)
    val gammaPower: Float  // 30 - 100 Hz (Peak Cognition / Insight)
)

data class NeuralState(
    val mainWaveform: List<Float>,
    val channels: List<NeuralChannel>,
    val frequencyBands: FrequencyBands,
    val confidence: Float,
    val latencyMs: Int,
    val primaryDecoding: String,
    val focusIndex: Float, // 0.0 to 1.0
    val relaxationIndex: Float, // 0.0 to 1.0
    val isArtifactDetected: Boolean, // Blink or muscle spasm artifact
    val timestamp: Long = System.currentTimeMillis()
)

class NeuralSimulator {

    private val decodings = listOf(
        "Focused Concentration",
        "Deep Meditation",
        "Motor Intent: Left Hand",
        "Motor Intent: Right Hand",
        "Alpha Wave Spike",
        "Visual Spatial Processing",
        "Subvocal Speech Pattern",
        "Working Memory Retrieval",
        "Auditory Tone Processing",
        "Cognitive Relaxation"
    )

    private val channelNames = listOf("Fp1", "Fp2", "C3", "C4", "O1", "O2", "T3", "T4")
    private val channelRegions = listOf(
        "Prefrontal", "Prefrontal", "Motor Cortex", "Motor Cortex",
        "Occipital", "Occipital", "Temporal", "Temporal"
    )

    fun getNeuralStream(
        gain: Float = 1.0f,
        isFilterEnabled: Boolean = true
    ): Flow<NeuralState> = flow {
        val mainHistory = mutableListOf<Float>()
        val channelHistories = List(8) { mutableListOf<Float>() }
        var stepCount = 0

        while (true) {
            stepCount++
            val time = System.currentTimeMillis() / 80.0

            // Generate main waveform with composite Sine + Noise
            val baseSine = (sin(time) * 0.5f + sin(time * 2.3) * 0.3f + sin(time * 0.7) * 0.4f).toFloat()
            val noise = ((Math.random().toFloat() - 0.5f) * (if (isFilterEnabled) 0.12f else 0.4f))
            val rawValue = (baseSine + noise) * gain

            mainHistory.add(rawValue)
            if (mainHistory.size > 120) mainHistory.removeAt(0)

            // Generate 8-channel EEG signals with phase shifts
            val channels = channelNames.mapIndexed { idx, name ->
                val phaseShift = idx * 0.4
                val chanVal = (sin(time + phaseShift) * 0.4f + noise).toFloat() * gain
                val history = channelHistories[idx]
                history.add(chanVal)
                if (history.size > 60) history.removeAt(0)

                NeuralChannel(
                    name = name,
                    region = channelRegions[idx],
                    signal = history.toList(),
                    impedanceOhm = 3000 + (sin(stepCount * 0.1 + idx) * 1200).toInt(),
                    isConnected = true
                )
            }

            // Calculate Frequency Bands
            val alpha = (0.35f + sin(time * 0.15).toFloat() * 0.25f).coerceIn(0.1f, 0.95f)
            val beta = (0.40f + sin(time * 0.22).toFloat() * 0.30f).coerceIn(0.1f, 0.95f)
            val theta = (0.25f + sin(time * 0.08).toFloat() * 0.20f).coerceIn(0.05f, 0.8f)
            val delta = (0.15f + sin(time * 0.05).toFloat() * 0.10f).coerceIn(0.05f, 0.6f)
            val gamma = (0.20f + sin(time * 0.35).toFloat() * 0.25f).coerceIn(0.05f, 0.9f)

            val bands = FrequencyBands(
                deltaPower = delta,
                thetaPower = theta,
                alphaPower = alpha,
                betaPower = beta,
                gammaPower = gamma
            )

            // Current decoding state selection
            val currentDecoding = decodings[(stepCount / 15) % decodings.size]
            val confidence = (0.84f + (sin(stepCount * 0.05) * 0.12).toFloat()).coerceIn(0.75f, 0.99f)
            val latency = (12..32).random()
            val artifact = (stepCount % 45 == 0)

            emit(
                NeuralState(
                    mainWaveform = mainHistory.toList(),
                    channels = channels,
                    frequencyBands = bands,
                    confidence = confidence,
                    latencyMs = latency,
                    primaryDecoding = currentDecoding,
                    focusIndex = beta / (beta + alpha + 0.01f),
                    relaxationIndex = alpha / (beta + alpha + 0.01f),
                    isArtifactDetected = artifact
                )
            )

            delay(40) // ~25 FPS live UI telemetry stream
        }
    }
}
