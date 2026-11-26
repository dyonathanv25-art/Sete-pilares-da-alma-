package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PlayerSaveEntity::class], version = 2, exportSchema = false)
abstract class AeldrinDatabase : RoomDatabase() {
    abstract fun playerSaveDao(): PlayerSaveDao

    companion object {
        @Volatile
        private var INSTANCE: AeldrinDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player_save ADD COLUMN activeHeroSkinId TEXT NOT NULL DEFAULT 'CLASS_DEFAULT'"
                )
            }
        }

        fun getInstance(context: Context): AeldrinDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AeldrinDatabase::class.java,
                    "aeldrin_prototype.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
