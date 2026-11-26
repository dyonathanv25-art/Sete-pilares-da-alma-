package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayerSaveEntity::class], version = 1, exportSchema = false)
abstract class AeldrinDatabase : RoomDatabase() {
    abstract fun playerSaveDao(): PlayerSaveDao

    companion object {
        @Volatile
        private var INSTANCE: AeldrinDatabase? = null

        fun getInstance(context: Context): AeldrinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AeldrinDatabase::class.java,
                    "aeldrin_prototype.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
