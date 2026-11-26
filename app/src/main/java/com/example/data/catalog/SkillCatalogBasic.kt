package com.example.data.catalog

import com.example.domain.model.HeroClass
import com.example.domain.model.Skill
import com.example.domain.model.SkillCategory
import com.example.domain.model.SkillEffectType

object SkillCatalogBasic {

    fun buildCommonAndRareAndLegendary(): List<Skill> {
        val list = mutableListOf<Skill>()

        // 001–050 (50 Comuns): golpes básicos, defesas, projéteis e curas iniciais (10 por classe)
        val commonSpecs = listOf(
            // 001-010 Guerreiro
            Triple("Corte de Ferro", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            Triple("Postura de Escudo", HeroClass.GUERREIRO, SkillEffectType.SHIELD),
            Triple("Investida Curta", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            Triple("Aparar de Treino", HeroClass.GUERREIRO, SkillEffectType.PARRY_COUNTER),
            Triple("Golpe de Punho Pesado", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            Triple("Fôlego da Guarda", HeroClass.GUERREIRO, SkillEffectType.HEAL),
            Triple("Talho Diagonal", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            Triple("Muralha de Bronze", HeroClass.GUERREIRO, SkillEffectType.SHIELD),
            Triple("Quebra-Passo", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            Triple("Estocada Leal", HeroClass.GUERREIRO, SkillEffectType.DAMAGE),
            // 011-020 Mago
            Triple("Seta Arcana", HeroClass.MAGO, SkillEffectType.DAMAGE),
            Triple("Faísca de Éter", HeroClass.MAGO, SkillEffectType.DAMAGE),
            Triple("Vela Protetora", HeroClass.MAGO, SkillEffectType.SHIELD),
            Triple("Meditação Breve", HeroClass.MAGO, SkillEffectType.MANA_SURGE),
            Triple("Lança Gélida", HeroClass.MAGO, SkillEffectType.DAMAGE),
            Triple("Sopro de Brasas", HeroClass.MAGO, SkillEffectType.DAMAGE),
            Triple("Barreira de Papel Rúnico", HeroClass.MAGO, SkillEffectType.SHIELD),
            Triple("Pulso Menor", HeroClass.MAGO, SkillEffectType.DAMAGE),
            Triple("Fluxo de Mana", HeroClass.MAGO, SkillEffectType.MANA_SURGE),
            Triple("Orbe Instável", HeroClass.MAGO, SkillEffectType.DAMAGE),
            // 021-030 Ladino
            Triple("Corte Furtivo", HeroClass.LADINO, SkillEffectType.DAMAGE),
            Triple("Lâmina Envenenada", HeroClass.LADINO, SkillEffectType.DOT_POISON),
            Triple("Rasgo Arterial", HeroClass.LADINO, SkillEffectType.DOT_BLEED),
            Triple("Passo de Neblina", HeroClass.LADINO, SkillEffectType.PARRY_COUNTER),
            Triple("Arremesso de Adaga", HeroClass.LADINO, SkillEffectType.DAMAGE),
            Triple("Pó Cegante", HeroClass.LADINO, SkillEffectType.SHIELD),
            Triple("Golpe nas Costelas", HeroClass.LADINO, SkillEffectType.DAMAGE),
            Triple("Agulha Tóxica", HeroClass.LADINO, SkillEffectType.DOT_POISON),
            Triple("Estocada Dupla", HeroClass.LADINO, SkillEffectType.DAMAGE),
            Triple("Recuo Sombrio", HeroClass.LADINO, SkillEffectType.PARRY_COUNTER),
            // 031-040 Arqueiro
            Triple("Flecha Certeira", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            Triple("Disparo Duplo", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            Triple("Flecha Serrilhada", HeroClass.ARQUEIRO, SkillEffectType.DOT_BLEED),
            Triple("Salto Ágil", HeroClass.ARQUEIRO, SkillEffectType.PARRY_COUNTER),
            Triple("Seta de Vento", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            Triple("Tiro de Caça", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            Triple("Flecha de Resina", HeroClass.ARQUEIRO, SkillEffectType.DOT_POISON),
            Triple("Visão de Falcão", HeroClass.ARQUEIRO, SkillEffectType.SHIELD),
            Triple("Disparo Rápido", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            Triple("Chuva de Setas", HeroClass.ARQUEIRO, SkillEffectType.DAMAGE),
            // 041-050 Clérigo
            Triple("Cura Menor", HeroClass.CLERIGO, SkillEffectType.HEAL),
            Triple("Centelha Sagrada", HeroClass.CLERIGO, SkillEffectType.DAMAGE),
            Triple("Bênção do Selo", HeroClass.CLERIGO, SkillEffectType.SHIELD),
            Triple("Prece Purificadora", HeroClass.CLERIGO, SkillEffectType.PURIFY),
            Triple("Martelo de Luz", HeroClass.CLERIGO, SkillEffectType.DAMAGE),
            Triple("Voto de Alívio", HeroClass.CLERIGO, SkillEffectType.HEAL),
            Triple("Égide Simples", HeroClass.CLERIGO, SkillEffectType.SHIELD),
            Triple("Chama Branca", HeroClass.CLERIGO, SkillEffectType.DAMAGE),
            Triple("Sussurro de Fé", HeroClass.CLERIGO, SkillEffectType.MANA_SURGE),
            Triple("Oração da Manhã", HeroClass.CLERIGO, SkillEffectType.HEAL)
        )

        commonSpecs.forEachIndexed { idx, (name, cls, eff) ->
            val id = idx + 1
            list += Skill(
                id = id,
                name = name,
                category = SkillCategory.COMUM,
                ownerClass = cls,
                manaCost = when (eff) {
                    SkillEffectType.MANA_SURGE -> 0
                    SkillEffectType.PARRY_COUNTER -> 6
                    else -> 8 + (idx % 4) * 2
                },
                cooldownTurns = if (eff == SkillEffectType.DAMAGE && idx % 3 == 0) 0 else 1,
                powerMultiplier = when (eff) {
                    SkillEffectType.HEAL -> 1.15f
                    SkillEffectType.SHIELD -> 1.20f
                    SkillEffectType.PARRY_COUNTER -> 1.35f
                    else -> 1.15f + (idx % 3) * 0.1f
                },
                effectType = eff,
                description = "Habilidade comum de ${cls.displayName} para pressão constante e abertura de combate."
            )
        }

        // 051–100 (50 Raras): combos, controle de grupo, DoTs e habilidades táticas
        val rarePrefixes = listOf(
            "Combo", "Ruptura", "Selo Tático", "Toxina", "Hemorragia",
            "Contragolpe", "Vórtice", "Perfuração", "Prisão", "Cadência"
        )
        val rareSuffixes = listOf(
            "de Kael-Dorn", "das Marés", "de Nyx", "do Vento", "de Solenis"
        )
        for (i in 0 until 50) {
            val id = 51 + i
            val cls = HeroClass.entries[i % HeroClass.entries.size]
            val eff = when (i % 7) {
                0 -> SkillEffectType.DOT_BLEED
                1 -> SkillEffectType.DOT_POISON
                2 -> SkillEffectType.PARRY_COUNTER
                3 -> SkillEffectType.SHIELD
                4 -> SkillEffectType.PURIFY
                5 -> SkillEffectType.DUAL_DOT
                else -> SkillEffectType.DAMAGE
            }
            val name = "${rarePrefixes[i % rarePrefixes.size]} ${rareSuffixes[(i / 10) % rareSuffixes.size]} #${i + 1}"
            list += Skill(
                id = id,
                name = name,
                category = SkillCategory.RARA,
                ownerClass = cls,
                manaCost = 14 + (i % 5) * 3,
                cooldownTurns = 2 + (i % 2),
                powerMultiplier = 1.45f + (i % 4) * 0.12f,
                effectType = eff,
                description = "Técnica tática rara que aplica controle, sangramento, veneno ou contra-ataque."
            )
        }

        // 101–150 (50 Lendárias): habilidades de alto impacto para raids e chefes
        val legNames = listOf(
            "Quebra-Bastilha", "Fúria da Cicatriz", "Meteoro de Mirath", "Dança das Mil Lâminas", "Chuva de Titãs",
            "Julgamento de Solenis", "Corte do Horizonte", "Colapso de Éter", "Sentença Carmesim", "Baluarte Imortal",
            "Impacto Tectônico", "Tempestade Arcana", "Execução Silenciosa", "Seta do Trovão", "Catedral de Luz",
            "Cicatriz Ardente", "Maré Esmagadora", "Sombra Perfurante", "Vento Cortante", "Selo da Aurora",
            "Martelo dos Cinco Heróis", "Coroa de Chamas", "Veneno Real", "Arpão Celeste", "Purgação Sagrada",
            "Lâmina do Juramento", "Fenda de Mana", "Passo Fantasma", "Tiro Perfura-Escamas", "Chama do Sacrifício",
            "Escudo da Guarda de Ferro", "Orbe do Relojoeiro", "Lamento da Hidra", "Olho do Vendaval", "Hino do Amanhecer",
            "Corte do Crepúsculo", "Supernova Rúnica", "Mordida da Víbora", "Lança de Aethel", "Milagre da Cinza",
            "Forte de Obsidiana", "Fluxo Astral", "Sinfonia Letal", "Flecha Solar", "Égide dos Mártires",
            "Quebra-Correntes", "Explosão de Ciclo", "Lâmina Zero", "Tempestade Branca", "Veredito do Viajante"
        )
        legNames.forEachIndexed { i, title ->
            val id = 101 + i
            val cls = HeroClass.entries[i % HeroClass.entries.size]
            val eff = when (i % 6) {
                0 -> SkillEffectType.DAMAGE
                1 -> SkillEffectType.SHIELD
                2 -> SkillEffectType.HEAL
                3 -> SkillEffectType.DUAL_DOT
                4 -> SkillEffectType.PARRY_COUNTER
                else -> SkillEffectType.DAMAGE
            }
            list += Skill(
                id = id,
                name = title,
                category = SkillCategory.LENDARIA,
                ownerClass = cls,
                manaCost = 24 + (i % 4) * 4,
                cooldownTurns = 3,
                powerMultiplier = 1.95f + (i % 5) * 0.1f,
                effectType = eff,
                description = "Habilidade lendária de alto impacto projetada para raides e chefes principais de Aeldrin."
            )
        }

        return list
    }
}
