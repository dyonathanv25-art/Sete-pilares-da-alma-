package com.example.data.repository

import com.example.data.local.PlayerSaveDao
import com.example.data.local.PlayerSaveEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: PlayerSaveDao) {
    val playerSave: Flow<PlayerSaveEntity?> = dao.observeSave()

    suspend fun ensureInitialized(): PlayerSaveEntity {
        val existing = dao.getSaveOnce()
        if (existing != null) return existing
        val initial = PlayerSaveEntity()
        dao.upsertSave(initial)
        return initial
    }

    suspend fun updateSave(transform: (PlayerSaveEntity) -> PlayerSaveEntity) {
        val current = ensureInitialized()
        dao.upsertSave(transform(current))
    }

    suspend fun resetSave() {
        dao.upsertSave(PlayerSaveEntity())
    }
}
