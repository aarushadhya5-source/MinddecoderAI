package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NeuralDao {
    @Query("SELECT * FROM neural_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<NeuralSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: NeuralSessionEntity): Long

    @Query("DELETE FROM neural_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM decoded_thoughts ORDER BY timestamp DESC")
    fun getAllDecodedThoughts(): Flow<List<DecodedThoughtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: DecodedThoughtEntity): Long

    @Query("DELETE FROM decoded_thoughts WHERE id = :id")
    suspend fun deleteThoughtById(id: Long)

    @Query("DELETE FROM decoded_thoughts")
    suspend fun clearAllThoughts()
}
