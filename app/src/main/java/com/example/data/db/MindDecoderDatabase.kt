package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NeuralSessionEntity::class, DecodedThoughtEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MindDecoderDatabase : RoomDatabase() {
    abstract fun neuralDao(): NeuralDao

    companion object {
        @Volatile
        private var INSTANCE: MindDecoderDatabase? = null

        fun getDatabase(context: Context): MindDecoderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindDecoderDatabase::class.java,
                    "mind_decoder_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
