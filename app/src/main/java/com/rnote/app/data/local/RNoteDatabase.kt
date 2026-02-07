package com.rnote.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class RNoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: RNoteDatabase? = null

        fun getInstance(context: Context): RNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RNoteDatabase::class.java,
                    "rnote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
