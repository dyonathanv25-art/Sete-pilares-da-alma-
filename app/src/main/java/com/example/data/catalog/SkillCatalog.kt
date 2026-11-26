package com.example.data.catalog

import com.example.domain.model.HeroClass
import com.example.domain.model.Skill
import com.example.domain.model.SkillCategory

object SkillCatalog {
    val allSkills: List<Skill> by lazy {
        (SkillCatalogBasic.buildCommonAndRareAndLegendary() +
            SkillCatalogAdvanced.buildMythicSecretEvolvingParadox())
            .sortedBy { it.id }
    }

    fun getById(id: Int): Skill? = allSkills.find { it.id == id }

    fun defaultLoadoutFor(heroClass: HeroClass): List<Int> {
        return when (heroClass) {
            HeroClass.GUERREIRO -> listOf(1, 4, 251, 101, 291)
            HeroClass.MAGO -> listOf(11, 14, 259, 102, 293)
            HeroClass.LADINO -> listOf(22, 23, 267, 103, 298)
            HeroClass.ARQUEIRO -> listOf(31, 33, 275, 104, 297)
            HeroClass.CLERIGO -> listOf(41, 44, 283, 106, 295)
        }
    }

    fun countByCategory(category: SkillCategory): Int = allSkills.count { it.category == category }
}
