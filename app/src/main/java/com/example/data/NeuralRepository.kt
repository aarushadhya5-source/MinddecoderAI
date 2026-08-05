package com.example.data

import com.example.data.db.DecodedThoughtEntity
import com.example.data.db.NeuralDao
import com.example.data.db.NeuralSessionEntity
import kotlinx.coroutines.flow.Flow

class NeuralRepository(
    private val dao: NeuralDao,
    private val simulator: NeuralSimulator = NeuralSimulator(),
    private val thoughtDecoder: GeminiThoughtDecoder = GeminiThoughtDecoder()
) {

    fun getNeuralStream(gain: Float = 1.0f, isFilterEnabled: Boolean = true): Flow<NeuralState> {
        return simulator.getNeuralStream(gain = gain, isFilterEnabled = isFilterEnabled)
    }

    val savedSessions: Flow<List<NeuralSessionEntity>> = dao.getAllSessions()
    val decodedThoughts: Flow<List<DecodedThoughtEntity>> = dao.getAllDecodedThoughts()

    suspend fun saveSession(session: NeuralSessionEntity): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        dao.deleteSessionById(id)
    }

    suspend fun saveThought(thought: DecodedThoughtEntity): Long {
        return dao.insertThought(thought)
    }

    suspend fun deleteThought(id: Long) {
        dao.deleteThoughtById(id)
    }

    suspend fun clearThoughts() {
        dao.clearAllThoughts()
    }

    suspend fun decodeThoughtWithAi(
        state: NeuralState,
        userPrompt: String = ""
    ): String {
        return thoughtDecoder.analyzeNeuralTelemetry(
            primaryState = state.primaryDecoding,
            confidencePct = (state.confidence * 100).toInt(),
            frequencyBands = state.frequencyBands,
            focusIndex = state.focusIndex,
            relaxationIndex = state.relaxationIndex,
            userPrompt = userPrompt
        )
    }
}
