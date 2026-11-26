package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerSaveDao {
    @Query("SELECT * FROM player_save WHERE id = 1")
    fun observeSave(): Flow<PlayerSaveEntity?>

    @Query("SELECT * FROM player_save WHERE id = 1")
    suspend fun getSaveOnce(): PlayerSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSave(save: PlayerSaveEntity)
}
