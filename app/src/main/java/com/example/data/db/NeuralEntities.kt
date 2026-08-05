package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neural_sessions")
data class NeuralSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val avgConfidence: Float = 0.90f,
    val peakState: String = "Focused",
    val primaryFrequencyBand: String = "Alpha",
    val sampleCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "decoded_thoughts")
data class DecodedThoughtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val decodedText: String,
    val stateCategory: String, // Focus, Relaxation, Motor Intent, Subvocal Speech, Memory Retrieval
    val confidence: Float,
    val aiInsight: String = ""
)
