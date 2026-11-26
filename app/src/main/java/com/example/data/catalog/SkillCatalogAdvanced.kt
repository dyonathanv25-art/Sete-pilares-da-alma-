package com.example.data.catalog

import com.example.domain.model.HeroClass
import com.example.domain.model.SecretClassId
import com.example.domain.model.Skill
import com.example.domain.model.SkillCategory
import com.example.domain.model.SkillEffectType

object SkillCatalogAdvanced {

    fun buildMythicSecretEvolvingParadox(): List<Skill> {
        val list = mutableListOf<Skill>()

        // 151–200 (50 Míticas): poderes ligados aos Pilares, Astros e ao Grande Selo
        val mythicThemes = listOf(
            "Primeiro Pilar", "Segundo Pilar", "Terceiro Pilar", "Quarto Pilar", "Quinto Pilar",
            "Sexto Pilar", "Sétimo Pilar", "Grande Selo", "Constelação Partida", "Coroa do Ciclo"
        )
        val mythicActions = listOf("Despertar", "Ressonância", "Ruptura", "Convergência", "Litania")
        for (i in 0 until 50) {
            val id = 151 + i
            val cls = HeroClass.entries[i % HeroClass.entries.size]
            val eff = when (i % 5) {
                0 -> SkillEffectType.DAMAGE
                1 -> SkillEffectType.MANA_SURGE
                2 -> SkillEffectType.PURIFY
                3 -> SkillEffectType.SHIELD
                else -> SkillEffectType.HEAL
            }
            val name = "${mythicActions[i % mythicActions.size]} do ${mythicThemes[i / 5]}"
            list += Skill(
                id = id,
                name = name,
                category = SkillCategory.MITICA,
                ownerClass = cls,
                manaCost = 32 + (i % 4) * 4,
                cooldownTurns = 4,
                powerMultiplier = 2.35f + (i % 4) * 0.15f,
                effectType = eff,
                description = "Poder mítico canalizado diretamente dos Sete Pilares da Alma e do Grande Selo."
            )
        }

        // 201–250 (50 Secretas): exclusivas das 9 classes secretas e híbridas
        val secretEntries = SecretClassId.entries
        val secretMoves = listOf(
            SecretClassId.TIRANO_DE_SANGUE to listOf("Pacto de Hemorragia", "Trono Carmesim", "Dreno de Kael-Dorn", "Fúria dos 5%", "Sede do Tirano", "Aura Sanguinária"),
            SecretClassId.QUEBRADOR_DE_CEUS to listOf("Corte da Espada de Madeira", "Décimo Parry", "Fenda Celeste", "Postura Sem Armadura", "Lâmina do Santo", "Divisor de Nuvens"),
            SecretClassId.ARQUIMAGO_CAOS to listOf("Sobrecarga Controlada", "Núcleo Caótico", "Colapso de Mirath", "Chama do Tempo Partido", "Tempestade de Mana", "Entropia Arcana"),
            SecretClassId.ESCRIBA_ESTRELAS to listOf("Alinhamento das 5 Lunetas", "Decreto Estelar", "Mapa do Guardião Astral", "Chuva de Constelações", "Luz do Observatório", "Astrolábio Vivo"),
            SecretClassId.CAMINHANTE_VEU to listOf("Passo Sem Rosto", "Infiltração Perfeita", "Lâmina da Memória Apagada", "Véu de Nyx", "Sussurro Silencioso", "Eco da Identidade"),
            SecretClassId.CEIFADOR_CARMESIM to listOf("Foice Dupla Tóxico-Sangrenta", "Duelo da Sombra", "Colheita das Catacumbas", "Veneno Carmesim", "Dança da Hemorragia", "Ceifa Final"),
            SecretClassId.CACADOR_DRAGOES to listOf("Disparo no Ponto Fraco", "Arpão Matador de Titãs", "Chuva de Aethel", "Perfura-Escamas Aéreo", "Voo Partido", "Lança do Caçador"),
            SecretClassId.ARQUEIRO_FANTASMA to listOf("Tiro do Eclipse Lunar", "Flecha Sem Encantamento", "Visão Espectral", "Santuário Submerso", "Seta Transparente", "Caça às Cegas"),
            SecretClassId.APOSTOLO_CREPUSCULO to listOf("Purificação do Herege", "Equilíbrio Luz e Trevas", "Abraço de Solenis", "Redenção da Maldição", "Chama Crepuscular", "Cântico de Malakor")
        )
        var secretIdCounter = 201
        for (round in 0 until 6) {
            for ((secretId, names) in secretEntries.zip(secretMoves.map { it.second })) {
                if (secretIdCounter > 250) break
                val subclass = ClassAndWorldCatalog.subclasses.first { it.id == secretId.id }
                val baseName = names[round % names.size]
                val finalName = if (round < names.size - 1) baseName else "$baseName Supremo"
                val eff = when (secretId) {
                    SecretClassId.TIRANO_DE_SANGUE -> SkillEffectType.DOT_BLEED
                    SecretClassId.QUEBRADOR_DE_CEUS -> SkillEffectType.PARRY_COUNTER
                    SecretClassId.ARQUIMAGO_CAOS -> SkillEffectType.MANA_SURGE
                    SecretClassId.ESCRIBA_ESTRELAS -> SkillEffectType.DAMAGE
                    SecretClassId.CAMINHANTE_VEU -> SkillEffectType.PARRY_COUNTER
                    SecretClassId.CEIFADOR_CARMESIM -> SkillEffectType.DUAL_DOT
                    SecretClassId.CACADOR_DRAGOES -> SkillEffectType.DAMAGE
                    SecretClassId.ARQUEIRO_FANTASMA -> SkillEffectType.DAMAGE
                    SecretClassId.APOSTOLO_CREPUSCULO -> SkillEffectType.PURIFY
                }
                list += Skill(
                    id = secretIdCounter,
                    name = finalName,
                    category = SkillCategory.SECRETA,
                    ownerClass = subclass.parentClass,
                    requiredSubclassId = secretId.id,
                    manaCost = 20 + (secretIdCounter % 5) * 3,
                    cooldownTurns = 2 + (secretIdCounter % 2),
                    powerMultiplier = 2.45f + (round * 0.1f),
                    effectType = eff,
                    description = "Arte secreta exclusiva de ${subclass.name} (${subclass.trialRegion})."
                )
                secretIdCounter++
            }
        }

        // 251–290 (40 Evolutivas): 8 por classe que mudam de forma e efeito conforme nível/subclasse
        val evoBases = listOf(
            HeroClass.GUERREIRO to listOf(
                listOf("Golpe de Aço", "Corte Titânico", "Juízo Quebrador de Céus"),
                listOf("Escudo Firme", "Baluarte Rúnico", "Muralha dos Cinco Heróis"),
                listOf("Grito de Guerra", "Rugido de Kael-Dorn", "Comando da Guarda Imortal"),
                listOf("Postura de Aparar", "Reflexo Perfeito", "Espelho do Santo da Espada"),
                listOf("Corte Profundo", "Fenda Sangrenta", "Execução do Tirano"),
                listOf("Salto Esmagador", "Impacto da Cicatriz", "Queda do Colosso"),
                listOf("Vontade de Ferro", "Sangue Fervente", "Fôlego Inquebrável"),
                listOf("Lâmina Carregada", "Espada Astral", "Selo da Lâmina Viva")
            ),
            HeroClass.MAGO to listOf(
                listOf("Faísca Temporal", "Fenda de Mirath", "Colapso das Sete Marés"),
                listOf("Esfera Ígnea", "Estrela Cadente", "Supernova do Caos"),
                listOf("Escudo de Mana", "Domo Arcano", "Barreira do Escriba"),
                listOf("Canalizar Éter", "Sobrecarga Viva", "Fonte Primordial"),
                listOf("Raio Congelante", "Prisão Glacial", "Zero Absoluto Astral"),
                listOf("Runa Explosiva", "Círculo de Selos", "Apoteose Rúnica"),
                listOf("Olhar Estelar", "Leitura do Céu", "Decreto das Constelações"),
                listOf("Onda de Choque", "Pulso Temporal", "Ruptura do Arquimago")
            ),
            HeroClass.LADINO to listOf(
                listOf("Corte Furtivo", "Lâmina do Véu", "Golpe do Sem-Rosto"),
                listOf("Gota Venenosa", "Névoa Tóxica", "Praga das Catacumbas"),
                listOf("Talho Veloz", "Corte Arterial", "Tempestade Carmesim"),
                listOf("Passo Oculto", "Sombra Dupla", "Desaparição de Nyx"),
                listOf("Adaga Voadora", "Chuva Oculta", "Leque das Mil Sombras"),
                listOf("Marca da Guilda", "Contrato Sombrio", "Sentença de O Zero"),
                listOf("Esquiva Rápida", "Reflexo Fantasma", "Intangibilidade"),
                listOf("Perfurar Ponto Vital", "Execução Silenciosa", "Fim Sem Nome")
            ),
            HeroClass.ARQUEIRO to listOf(
                listOf("Disparo Firme", "Flecha de Vento Branco", "Seta Quebra-Céus"),
                listOf("Tiro Ascendente", "Mira de Falcão", "Perfurador de Titãs"),
                listOf("Flecha Farpada", "Saraivada Sangrenta", "Chuva de Aço Alado"),
                listOf("Tiro Espectral", "Seta do Eclipse", "Flecha do Arqueiro Fantasma"),
                listOf("Armadilha de Vinha", "Laço Rúnico", "Prisão da Floresta"),
                listOf("Disparo Relâmpago", "Arco Tempestuoso", "Raio de Aethel"),
                listOf("Recuo Acrobático", "Voo Rasante", "Dança da Ventania"),
                listOf("Olho da Tempestade", "Foco Absoluto", "Último Canto do Eclipse")
            ),
            HeroClass.CLERIGO to listOf(
                listOf("Luz Renovadora", "Cura do Amanhecer", "Milagre dos Pilares"),
                listOf("Raio Purificador", "Expurgo Solar", "Redenção do Crepúsculo"),
                listOf("Manto Sagrado", "Égide de Solenis", "Santuário Inviolável"),
                listOf("Prece do Ciclo", "Coro Temporal", "Harmonia de Aeldrin"),
                listOf("Toque Sombrio-Luminoso", "Equilíbrio Herege", "Abraço de Malakor"),
                listOf("Selo de Proteção", "Juramento Vivo", "Bastião da Alma"),
                listOf("Chama Branca", "Fogo Purgador", "Sol do Grande Selo"),
                listOf("Bênção da Esperança", "Ressurgimento", "Voz da Ordem Unida")
            )
        )

        var evoId = 251
        evoBases.forEach { (cls, groups) ->
            groups.forEachIndexed { idx, forms ->
                val eff = when (idx % 6) {
                    0 -> SkillEffectType.DAMAGE
                    1 -> SkillEffectType.SHIELD
                    2 -> SkillEffectType.DOT_BLEED
                    3 -> SkillEffectType.HEAL
                    4 -> SkillEffectType.PURIFY
                    else -> SkillEffectType.PARRY_COUNTER
                }
                list += Skill(
                    id = evoId,
                    name = forms[0],
                    category = SkillCategory.EVOLUTIVA,
                    ownerClass = cls,
                    manaCost = 16 + (idx % 4) * 3,
                    cooldownTurns = 2,
                    powerMultiplier = 1.65f + (idx % 3) * 0.2f,
                    effectType = eff,
                    description = "Evolui automaticamente com seu nível e subclasse: ${forms.joinToString(" → ")}.",
                    evolutionForms = forms
                )
                evoId++
            }
        }

        // 291–300 (10 Quebra-Jogo / Paradoxais)
        list += listOf(
            Skill(291, "Inversão de Causa e Efeito", SkillCategory.PARADOXAL, null, null, 40, 6, 1.0f, SkillEffectType.PARADOX_INVERSION, "Por 2 turnos, todo dano que o chefe causar em você vira CURA e suas curas causam DANO ao inimigo."),
            Skill(292, "Troca de % de Vida", SkillCategory.PARADOXAL, null, null, 45, 7, 1.0f, SkillEffectType.PARADOX_SWAP_HP, "Troca a porcentagem de HP atual entre você e o chefe (com teto tático de dano em raids)."),
            Skill(293, "Reset Absoluto", SkillCategory.PARADOXAL, null, null, 20, 5, 1.0f, SkillEffectType.PARADOX_RESET_CD, "Zera o tempo de recarga de todas as suas habilidades e restaura 65% da Mana."),
            Skill(294, "Inversão de Atributos", SkillCategory.PARADOXAL, null, null, 35, 6, 2.4f, SkillEffectType.PARADOX_SWAP_STATS, "Converte Defesa em Poder de Ataque massivo e causa dano verdadeiro."),
            Skill(295, "Desfazer Morte", SkillCategory.PARADOXAL, null, null, 40, 8, 1.0f, SkillEffectType.PARADOX_UNDO_DEATH, "Cria uma âncora temporal: se seu HP chegar a 0, você revive com 45% de vida e purifica debuffs."),
            Skill(296, "Lacuna dos Sete Minutos", SkillCategory.PARADOXAL, null, null, 38, 6, 1.4f, SkillEffectType.PARADOX_TIME_FREEZE, "Congela o chefe no loop de Mirath por 2 turnos enquanto você age livremente."),
            Skill(297, "Selo Quebrado", SkillCategory.PARADOXAL, null, null, 42, 5, 3.1f, SkillEffectType.PARADOX_PIERCE_SEAL, "Ignora completamente defesa, barreiras e resistências do Arquiteto do Vazio."),
            Skill(298, "Eco Sem-Rosto", SkillCategory.PARADOXAL, null, null, 25, 4, 1.8f, SkillEffectType.PARADOX_ECHO, "Replica sua última habilidade usada com 180% de potência sem custo adicional."),
            Skill(299, "Eclipse do Crepúsculo", SkillCategory.PARADOXAL, null, null, 36, 5, 2.2f, SkillEffectType.PARADOX_TWILIGHT_ECLIPSE, "Funde Luz e Trevas: restaura 35% do HP e causa dano equivalente ao dobro da cura."),
            Skill(300, "Decreto do Viajante Marcado", SkillCategory.PARADOXAL, null, null, 50, 7, 3.5f, SkillEffectType.PARADOX_DECREE, "Libera a energia dos Sete Pilares da Alma causando dano massivo escalado pelo seu nível.")
        )

        return list
    }
}
