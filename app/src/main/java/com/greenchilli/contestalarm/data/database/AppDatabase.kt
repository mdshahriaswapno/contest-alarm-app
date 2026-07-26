package com.greenchilli.contestalarm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ContestEntity::class, CustomAlarmEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contestDao(): ContestDao
    abstract fun customAlarmDao(): CustomAlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contest_database"
                )
                .fallbackToDestructiveMigration() // Wipes old DB on version change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
