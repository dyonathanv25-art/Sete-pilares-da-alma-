package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_save")
data class PlayerSaveEntity(
    @PrimaryKey val id: Int = 1,
    val heroName: String = "Viajante Marcado",
    val heroClassName: String = "GUERREIRO",
    val activeSubclassId: String = "war_guardiao",
    val level: Int = 1,
    val xp: Int = 0,
    val shards: Int = 50,
    val equippedSkillIdsCsv: String = "1,4,251,101,291",
    val unlockedSecretClassIdsCsv: String = "",
    val defeatedBossIdsCsv: String = "",
    val purifiedBossIdsCsv: String = "",
    val perfectParriesCount: Int = 0,
    val manaOverloadsSurvived: Int = 0,
    val dualDotStacksCount: Int = 0,
    val lowHpWinCount: Int = 0,
    val lightArmorChallengeEnabled: Boolean = false,
    val lunarEclipseModeEnabled: Boolean = false,
    val lunetasAlignedDone: Boolean = false,
    val nyxStealthDone: Boolean = false
) {
    fun equippedSkillIds(): List<Int> =
        equippedSkillIdsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.take(5)

    fun unlockedSecretIds(): Set<String> =
        unlockedSecretClassIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun defeatedBossIds(): Set<String> =
        defeatedBossIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun purifiedBossIds(): Set<String> =
        purifiedBossIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
