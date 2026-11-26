package com.example.data.catalog

import com.example.domain.model.EnemyEncounter
import com.example.domain.model.EnemyTier
import com.example.domain.model.HeroClass
import com.example.domain.model.RegionInfo
import com.example.domain.model.SecretClassId
import com.example.domain.model.SubclassInfo
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.AstralViolet
import com.example.ui.theme.CrimsonBlood
import com.example.ui.theme.GildedAmber
import com.example.ui.theme.ParadoxMagenta
import com.example.ui.theme.SoulEmerald

object ClassAndWorldCatalog {

    val mainLoreHeadline = "Os Sete Pilares da Alma & A Cicatriz de Cinzas"
    val mainLoreBody = "Há mil anos, o continente de Aeldrin e os Sete Pilares da Alma foram partidos quando os Cinco Heróis Originais selaram O Arquiteto do Vazio na Cicatriz de Cinzas. O tempo tornou-se instável, fendas surgiram e você desperta como um Viajante Marcado entre a Ordem do Amanhecer, os Filhos do Ciclo e o Sindicato das Cinzas."

    val factions = listOf(
        "Ordem do Amanhecer" to "Busca restaurar os Sete Pilares pela luz rígida e disciplina.",
        "Filhos do Ciclo" to "Aceitam o fluxo temporal partido e estudam estrelas e eclipses.",
        "Sindicato das Cinzas" to "Comerciam relíquias de fendas, memórias apagadas e pactos proibidos."
    )

    // 21 Subclasses Padrão + 9 Subclasses Secretas = 30 Subclasses
    val subclasses: List<SubclassInfo> = listOf(
        // GUERREIRO (4 padrão + 2 secretas)
        SubclassInfo("war_guardiao", "Guardião de Ferro", HeroClass.GUERREIRO, false, 1, "Kael-Dorn", "Evolução padrão de Guerreiro (Nível 1+)", "Aumenta bloqueio e reduz dano de golpes pesados.", 80, 10, 6, 14, 0),
        SubclassInfo("war_berserker", "Berserker", HeroClass.GUERREIRO, false, 3, "Kael-Dorn", "Evolução padrão de Guerreiro (Nível 3+)", "Ataques ganham bônus quando seu HP está abaixo de 60%.", 45, 0, 16, 4, 8),
        SubclassInfo("war_paladino", "Paladino da Aurora", HeroClass.GUERREIRO, false, 5, "Solenis", "Evolução padrão de Guerreiro (Nível 5+)", "Concede cura leve ao executar bloqueios e habilidades sagradas.", 65, 25, 10, 10, 4),
        SubclassInfo("war_runico", "Cavaleiro Rúnico", HeroClass.GUERREIRO, false, 8, "Mirath", "Evolução padrão de Guerreiro (Nível 8+)", "Combina golpes físicos com explosões de mana rúnica.", 50, 35, 14, 8, 6),
        SubclassInfo(
            id = SecretClassId.TIRANO_DE_SANGUE.id,
            name = "Tirano de Sangue",
            parentClass = HeroClass.GUERREIRO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Kael-Dorn",
            unlockCondition = "Sobreviver e vencer com menos de 5% de HP sem cura recente ou concluir a Provação de Sangue em Kael-Dorn.",
            passiveDescription = "Drena 25% do dano causado como vitalidade quando abaixo de 35% HP.",
            hpBonus = 110, manaBonus = 20, atkBonus = 24, defBonus = 10, critBonus = 12
        ),
        SubclassInfo(
            id = SecretClassId.QUEBRADOR_DE_CEUS.id,
            name = "Quebrador de Céus (Santo da Espada)",
            parentClass = HeroClass.GUERREIRO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Aethel",
            unlockCondition = "Acumular 10 Parries Perfeitos usando Espada de Madeira / Sem Armadura Pesada no desafio de Aethel.",
            passiveDescription = "Parries Perfeitos devolvem 180% do ATK e ignoram 50% da defesa.",
            hpBonus = 70, manaBonus = 30, atkBonus = 28, defBonus = 12, critBonus = 18
        ),

        // MAGO (4 padrão + 2 secretas)
        SubclassInfo("mag_piromante", "Piromante", HeroClass.MAGO, false, 1, "Cicatriz de Cinzas", "Evolução padrão de Mago (Nível 1+)", "Chamas de cinzas causam queimadura contínua.", 20, 40, 16, 2, 6),
        SubclassInfo("mag_cronomante", "Cronomante", HeroClass.MAGO, false, 3, "Mirath", "Evolução padrão de Mago (Nível 3+)", "Reduz recarga das habilidades em combates prolongados.", 25, 55, 12, 5, 6),
        SubclassInfo("mag_arcanista", "Arcanista Rúnico", HeroClass.MAGO, false, 5, "Mirath", "Evolução padrão de Mago (Nível 5+)", "Amplifica selos arcanos e escudos de mana.", 30, 65, 14, 6, 8),
        SubclassInfo("mag_tecelao", "Tecelão Elemental", HeroClass.MAGO, false, 8, "Aethel", "Evolução padrão de Mago (Nível 8+)", "Alterna feitiços para gerar bônus de penetração mágica.", 25, 70, 18, 4, 10),
        SubclassInfo(
            id = SecretClassId.ARQUIMAGO_CAOS.id,
            name = "Arquimago do Caos Primordial",
            parentClass = HeroClass.MAGO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Mirath",
            unlockCondition = "Sobreviver a 3 Sobrecargas de Mana em combate perto da Fenda Temporal de Mirath.",
            passiveDescription = "Sobrecarga de Mana passa a conceder +35% de poder mágico em vez de punir.",
            hpBonus = 45, manaBonus = 110, atkBonus = 30, defBonus = 6, critBonus = 14
        ),
        SubclassInfo(
            id = SecretClassId.ESCRIBA_ESTRELAS.id,
            name = "Escriba das Estrelas",
            parentClass = HeroClass.MAGO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Aethel",
            unlockCondition = "Alinhar as 5 lunetas antigas na Provação Astral e derrotar o Guardião Astral.",
            passiveDescription = "Lê padrões estelares: cada 3ª habilidade lançada é crítica garantida.",
            hpBonus = 50, manaBonus = 95, atkBonus = 26, defBonus = 10, critBonus = 20
        ),

        // LADINO (4 padrão + 2 secretas)
        SubclassInfo("lad_sombria", "Lâmina Sombria", HeroClass.LADINO, false, 1, "Nyx", "Evolução padrão de Ladino (Nível 1+)", "Aumenta chance crítica em golpes iniciais.", 25, 25, 15, 3, 10),
        SubclassInfo("lad_adagas", "Dançarino das Adagas", HeroClass.LADINO, false, 3, "Nyx", "Evolução padrão de Ladino (Nível 3+)", "Combo rápido reduz cooldowns em 1 turno ao acertar crítico.", 30, 30, 16, 4, 12),
        SubclassInfo("lad_alquimista", "Alquimista Tóxico", HeroClass.LADINO, false, 5, "Kael-Dorn", "Evolução padrão de Ladino (Nível 5+)", "Potencializa acúmulos de Veneno nos inimigos.", 35, 35, 13, 5, 8),
        SubclassInfo("lad_espiao", "Espião das Cinzas", HeroClass.LADINO, false, 8, "Cicatriz de Cinzas", "Evolução padrão de Ladino (Nível 8+)", "Aumenta esquiva e penetração contra alvos com escudo.", 30, 40, 17, 6, 12),
        SubclassInfo(
            id = SecretClassId.CAMINHANTE_VEU.id,
            name = "Caminhante do Véu (Sem-Rosto)",
            parentClass = HeroClass.LADINO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Nyx",
            unlockCondition = "Concluir a infiltração de Nyx sem ser detectado e sem eliminar guardas.",
            passiveDescription = "Ignora 40% da armadura e ganha 25% de evasão passiva.",
            hpBonus = 55, manaBonus = 50, atkBonus = 25, defBonus = 10, critBonus = 22
        ),
        SubclassInfo(
            id = SecretClassId.CEIFADOR_CARMESIM.id,
            name = "Ceifador Carmesim",
            parentClass = HeroClass.LADINO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Kael-Dorn",
            unlockCondition = "Aplicar Veneno e Sangramento simultaneamente em inimigos (10 acúmulos no protótipo) e vencer a Sombra nas Catacumbas.",
            passiveDescription = "Inimigos sob Veneno + Sangramento sofrem +40% de dano letal.",
            hpBonus = 60, manaBonus = 40, atkBonus = 29, defBonus = 8, critBonus = 20
        ),

        // ARQUEIRO (4 padrão + 2 secretas)
        SubclassInfo("arq_elite", "Atirador de Elite", HeroClass.ARQUEIRO, false, 1, "Aethel", "Evolução padrão de Arqueiro (Nível 1+)", "Disparos precisos com alto dano em alvo único.", 20, 25, 17, 3, 12),
        SubclassInfo("arq_patrulheiro", "Patrulheiro Selvagem", HeroClass.ARQUEIRO, false, 3, "Kael-Dorn", "Evolução padrão de Arqueiro (Nível 3+)", "Equilibra mobilidade, armadilhas e sangramento.", 40, 30, 14, 6, 8),
        SubclassInfo("arq_relampago", "Caçador de Relâmpagos", HeroClass.ARQUEIRO, false, 5, "Aethel", "Evolução padrão de Arqueiro (Nível 5+)", "Flechas carregadas perfuram barreiras arcanas.", 25, 45, 18, 4, 10),
        SubclassInfo("arq_vento", "Sentinela do Vento", HeroClass.ARQUEIRO, false, 8, "Aethel", "Evolução padrão de Arqueiro (Nível 8+)", "Aumenta velocidade de recarga e chance de esquiva.", 30, 40, 16, 7, 12),
        SubclassInfo(
            id = SecretClassId.CACADOR_DRAGOES.id,
            name = "Caçador de Dragões (Titãs)",
            parentClass = HeroClass.ARQUEIRO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Aethel",
            unlockCondition = "Desferir golpe final no Ponto Fraco Aéreo de um chefe gigante/alado em Aethel.",
            passiveDescription = "Causa +50% de dano em chefes e destrói pontos fracos aéreos.",
            hpBonus = 65, manaBonus = 45, atkBonus = 30, defBonus = 9, critBonus = 18
        ),
        SubclassInfo(
            id = SecretClassId.ARQUEIRO_FANTASMA.id,
            name = "Arqueiro Fantasma (Espectro Lunar)",
            parentClass = HeroClass.ARQUEIRO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Mirath",
            unlockCondition = "Ativar o Modo Eclipse Lunar (Sem Encantamentos) e vencer no Santuário Submerso.",
            passiveDescription = "Flechas espectrais atravessam defesas sem depender de encantamentos.",
            hpBonus = 50, manaBonus = 65, atkBonus = 26, defBonus = 8, critBonus = 24
        ),

        // CLÉRIGO (5 padrão + 1 secreta -> total 21 padrão + 9 secretas = 30)
        SubclassInfo("cle_amanhecer", "Sacerdote do Amanhecer", HeroClass.CLERIGO, false, 1, "Solenis", "Evolução padrão de Clérigo (Nível 1+)", "Potencializa curas e regeneração de mana.", 45, 50, 8, 8, 4),
        SubclassInfo("cle_inquisidor", "Inquisidor Solar", HeroClass.CLERIGO, false, 3, "Solenis", "Evolução padrão de Clérigo (Nível 3+)", "Converte fé em dano radiante contra seres das fendas.", 40, 40, 16, 7, 8),
        SubclassInfo("cle_oraculo", "Oráculo do Ciclo", HeroClass.CLERIGO, false, 5, "Mirath", "Evolução padrão de Clérigo (Nível 5+)", "Prevê ataques dos chefes e fortalece parries.", 50, 60, 11, 9, 6),
        SubclassInfo("cle_egide", "Guardião de Égide", HeroClass.CLERIGO, false, 7, "Kael-Dorn", "Evolução padrão de Clérigo (Nível 7+)", "Gera escudos divinos de alta durabilidade.", 75, 45, 9, 14, 2),
        SubclassInfo("cle_arauto", "Arauto dos Pilares", HeroClass.CLERIGO, false, 9, "Solenis", "Evolução padrão de Clérigo (Nível 9+)", "Canaliza bênçãos dos Sete Pilares da Alma.", 60, 70, 14, 10, 8),
        SubclassInfo(
            id = SecretClassId.APOSTOLO_CREPUSCULO.id,
            name = "Apóstolo do Crepúsculo (Herege)",
            parentClass = HeroClass.CLERIGO,
            isSecret = true,
            requiredLevel = 1,
            trialRegion = "Solenis",
            unlockCondition = "Purificar e salvar um chefe ou arauto amaldiçoado em Solenis abaixo de 30% HP em vez de matá-lo.",
            passiveDescription = "Funde Luz e Trevas: curas também aplicam dano sombrio igual a 60% do valor.",
            hpBonus = 80, manaBonus = 85, atkBonus = 24, defBonus = 14, critBonus = 12
        )
    )

    val regions: List<RegionInfo> = listOf(
        RegionInfo(
            id = "kael_dorn",
            name = "Kael-Dorn",
            subtitle = "Vaelen e a Guarda de Ferro",
            factionFocus = "Ordem do Amanhecer",
            loreSummary = "Fortaleza fronteiriça marcada pelo recuo de Vaelen para salvar civis — vista pelos fanáticos como mancha de desonra. Sob suas muralhas ocultam-se as Catacumbas de Sangue.",
            accentColor = CrimsonBlood
        ),
        RegionInfo(
            id = "mirath",
            name = "Mirath",
            subtitle = "Biblioteca Submersa (Marés Presas)",
            factionFocus = "Filhos do Ciclo",
            loreSummary = "O experimento temporal de Lyraen prendeu as marés e o Santuário Submerso em um loop perpétuo de sete minutos onde fendas de mana colapsam.",
            accentColor = ArcaneCyan
        ),
        RegionInfo(
            id = "nyx",
            name = "Nyx",
            subtitle = "Guilda dos Nomes Apagados",
            factionFocus = "Sindicato das Cinzas",
            loreSummary = "Metrópole subterrânea de neblina onde memórias e identidades reais são negociadas sob a liderança enigmática de O Zero.",
            accentColor = ParadoxMagenta
        ),
        RegionInfo(
            id = "aethel",
            name = "Aethel",
            subtitle = "O Último Canto do Eclipse",
            factionFocus = "Filhos do Ciclo",
            loreSummary = "Picos tempestuosos e observatórios estelares antigos onde Sera do Vento Branco caça colossos alados durante eclipses.",
            accentColor = SoulEmerald
        ),
        RegionInfo(
            id = "solenis",
            name = "Solenis",
            subtitle = "Santuário do Crepúsculo",
            factionFocus = "Ordem do Amanhecer",
            loreSummary = "Templo dividido onde Malakor uniu luz e trevas para salvar almas que a luz pura queimava, desafiando o dogma da Ordem.",
            accentColor = GildedAmber
        ),
        RegionInfo(
            id = "cicatriz",
            name = "Cicatriz de Cinzas",
            subtitle = "O Grande Selo Partido",
            factionFocus = "Confronto Final",
            loreSummary = "Epicentro onde os Cinco Heróis Originais selaram O Arquiteto do Vazio há mil anos. Agora o selo racha e ameaça devorar o tempo.",
            accentColor = AstralViolet
        )
    )

    val encounters: List<EnemyEncounter> = listOf(
        // 5 SUBCHEFES
        EnemyEncounter(
            id = "sub_kael7",
            name = "Sentinela Kael-7",
            title = "Autômato da Guarda Antiga",
            regionId = "kael_dorn",
            tier = EnemyTier.SUB_BOSS,
            recommendedLevel = 1,
            maxHp = 260,
            attackPower = 24,
            defense = 8,
            xpReward = 90,
            shardReward = 40,
            loreDescription = "Construto de ferro rúnico deixado para vigiar os portões externos de Kael-Dorn.",
            mechanicHint = "Anuncia golpes pesados perfeitos para treinar Parry Perfeito e sobrevivência em HP crítico."
        ),
        EnemyEncounter(
            id = "sub_sussurro_nyx",
            name = "Sussurro de Nyx",
            title = "Coletor de Nomes",
            regionId = "nyx",
            tier = EnemyTier.SUB_BOSS,
            recommendedLevel = 3,
            maxHp = 330,
            attackPower = 29,
            defense = 10,
            xpReward = 130,
            shardReward = 55,
            loreDescription = "Agente sem rosto do Sindicato das Cinzas que apaga memórias de combate.",
            mechanicHint = "Vulnerável a dano contínuo de Veneno + Sangramento simultâneos."
        ),
        EnemyEncounter(
            id = "sub_hidra_cinzas",
            name = "Hidra de Cinzas",
            title = "Fera da Fenda Flamejante",
            regionId = "cicatriz",
            tier = EnemyTier.SUB_BOSS,
            recommendedLevel = 5,
            maxHp = 430,
            attackPower = 33,
            defense = 11,
            xpReward = 175,
            shardReward = 70,
            loreDescription = "Besta de múltiplas cabeças nascida das brasas do Grande Selo.",
            mechanicHint = "Regenera quando não sofre dano contínuo ou explosões de alto impacto."
        ),
        EnemyEncounter(
            id = "sub_relojoeiro",
            name = "Relojoeiro Quebrado",
            title = "Guardião do Loop de 7 Minutos",
            regionId = "mirath",
            tier = EnemyTier.SUB_BOSS,
            recommendedLevel = 4,
            maxHp = 380,
            attackPower = 31,
            defense = 12,
            xpReward = 155,
            shardReward = 65,
            loreDescription = "Mago mecânico preso no colapso cronológico da Biblioteca de Mirath.",
            mechanicHint = "Emite pulsos temporais que testam Sobrecarga de Mana e reset de cooldowns."
        ),
        EnemyEncounter(
            id = "sub_arauto_solenis",
            name = "Arauto Solenis Caído",
            title = "Alma Queimada pela Luz Pura",
            regionId = "solenis",
            tier = EnemyTier.SUB_BOSS,
            recommendedLevel = 5,
            maxHp = 400,
            attackPower = 32,
            defense = 12,
            xpReward = 180,
            shardReward = 80,
            isCursed = true,
            loreDescription = "Ex-paladino corrompido pela maldição da luz intolerante.",
            mechanicHint = "Amaldiçoado! Reduza o HP abaixo de 30% e use 'Purificar Alma' para salvá-lo e desbloquear Apóstolo do Crepúsculo."
        ),

        // 6 CHEFES PRINCIPAIS
        EnemyEncounter(
            id = "boss_vaelen",
            name = "Vaelen, o Desonrado",
            title = "Comandante da Guarda de Ferro",
            regionId = "kael_dorn",
            tier = EnemyTier.MAIN_BOSS,
            recommendedLevel = 6,
            maxHp = 580,
            attackPower = 40,
            defense = 16,
            xpReward = 260,
            shardReward = 120,
            loreDescription = "Recuou para salvar civis em vez de morrer pelo orgulho da Ordem. Testa a verdadeira honra do Viajante.",
            mechanicHint = "Executa Golpes Sísmicos anunciados — aparar com Parry reduz sua guarda."
        ),
        EnemyEncounter(
            id = "boss_lyraen",
            name = "Lyraen das Marés Presas",
            title = "Arquivista do Loop Temporal",
            regionId = "mirath",
            tier = EnemyTier.MAIN_BOSS,
            recommendedLevel = 8,
            maxHp = 690,
            attackPower = 45,
            defense = 15,
            xpReward = 320,
            shardReward = 150,
            loreDescription = "Presa no próprio experimento de sete minutos na Biblioteca Submersa de Mirath.",
            mechanicHint = "Reverte turnos com fendas temporais; habilidades Paradoxais brilham aqui."
        ),
        EnemyEncounter(
            id = "boss_zero",
            name = "O Zero",
            title = "Mestre dos Nomes Apagados",
            regionId = "nyx",
            tier = EnemyTier.MAIN_BOSS,
            recommendedLevel = 9,
            maxHp = 740,
            attackPower = 48,
            defense = 16,
            xpReward = 360,
            shardReward = 170,
            loreDescription = "Líder do Sindicato em Nyx. Ninguém sabe seu verdadeiro rosto ou sua classe original.",
            mechanicHint = "Golpes críticos furtivos exigem escudo ativo ou parry no momento exato."
        ),
        EnemyEncounter(
            id = "boss_sera",
            name = "Sera do Vento Branco",
            title = "Caçadora dos Colossos Alados",
            regionId = "aethel",
            tier = EnemyTier.MAIN_BOSS,
            recommendedLevel = 10,
            maxHp = 820,
            attackPower = 52,
            defense = 17,
            xpReward = 410,
            shardReward = 195,
            hasAerialWeakPoint = true,
            loreDescription = "Guardiã dos céus de Aethel acompanhada pelo eco de um Draco das Tempestades.",
            mechanicHint = "Expõe um Ponto Fraco Aéreo quando alça voo! Finalizar nesse estado libera Caçador de Dragões."
        ),
        EnemyEncounter(
            id = "boss_malakor",
            name = "Malakor do Crepúsculo",
            title = "O Herege Compassivo",
            regionId = "solenis",
            tier = EnemyTier.MAIN_BOSS,
            recommendedLevel = 12,
            maxHp = 920,
            attackPower = 55,
            defense = 19,
            xpReward = 480,
            shardReward = 230,
            isCursed = true,
            loreDescription = "Fundiu luz e trevas no Santuário do Crepúsculo para abrigar almas rejeitadas.",
            mechanicHint = "Pode ser derrotado em combate ou purificado abaixo de 30% HP para redenção."
        ),
        EnemyEncounter(
            id = "boss_arquiteto",
            name = "O Arquiteto do Vazio",
            title = "Entidade da Cicatriz de Cinzas",
            regionId = "cicatriz",
            tier = EnemyTier.FINAL_RAID,
            recommendedLevel = 15,
            maxHp = 1450,
            attackPower = 68,
            defense = 24,
            xpReward = 900,
            shardReward = 500,
            hasAerialWeakPoint = true,
            loreDescription = "O ser primordial aprisionado há mil anos pelos Cinco Heróis Originais. Distorce as regras do combate.",
            mechanicHint = "Use habilidades Evolutivas (Forma III) e Quebra-Jogo Paradoxais (291–300) para romper seu selo!"
        ),

        // PROVAÇÕES SECRETAS DEDICADAS
        EnemyEncounter(
            id = "trial_sombra_catacumbas",
            name = "Sombra das Catacumbas",
            title = "Reflexo Carmesim",
            regionId = "kael_dorn",
            tier = EnemyTier.SECRET_TRIAL,
            recommendedLevel = 4,
            maxHp = 350,
            attackPower = 30,
            defense = 10,
            xpReward = 180,
            shardReward = 100,
            loreDescription = "Seu próprio reflexo corrompido nas profundezas de Kael-Dorn.",
            mechanicHint = "Vença aplicando Veneno e Sangramento simultâneos para despertar o Ceifador Carmesim.",
            secretRewardId = SecretClassId.CEIFADOR_CARMESIM.id
        ),
        EnemyEncounter(
            id = "trial_guardiao_astral",
            name = "Guardião Astral",
            title = "Sentinela das 5 Lunetas",
            regionId = "aethel",
            tier = EnemyTier.SECRET_TRIAL,
            recommendedLevel = 5,
            maxHp = 410,
            attackPower = 34,
            defense = 12,
            xpReward = 210,
            shardReward = 115,
            loreDescription = "Desperta apenas quando as cinco lunetas antigas são alinhadas sob o céu noturno.",
            mechanicHint = "Alinhe as 5 lunetas e vença-o para despertar Escriba das Estrelas.",
            secretRewardId = SecretClassId.ESCRIBA_ESTRELAS.id
        ),
        EnemyEncounter(
            id = "trial_draco_tempestades",
            name = "Draco das Tempestades",
            title = "Colosso Alado de Aethel",
            regionId = "aethel",
            tier = EnemyTier.SECRET_TRIAL,
            recommendedLevel = 7,
            maxHp = 540,
            attackPower = 38,
            defense = 14,
            xpReward = 250,
            shardReward = 140,
            hasAerialWeakPoint = true,
            loreDescription = "Dragão ancestral dos vendavais de Aethel.",
            mechanicHint = "Finalize o Draco enquanto seu Ponto Fraco Aéreo estiver aberto!",
            secretRewardId = SecretClassId.CACADOR_DRAGOES.id
        ),
        EnemyEncounter(
            id = "trial_espectro_submerso",
            name = "Espectro do Santuário Submerso",
            title = "Eco do Eclipse Lunar",
            regionId = "mirath",
            tier = EnemyTier.SECRET_TRIAL,
            recommendedLevel = 6,
            maxHp = 460,
            attackPower = 36,
            defense = 13,
            xpReward = 230,
            shardReward = 130,
            loreDescription = "Guardião cego que sente apenas encantamentos arcanos.",
            mechanicHint = "Ative o modo 'Sem Encantamentos (Eclipse)' e derrote-o para liberar Arqueiro Fantasma.",
            secretRewardId = SecretClassId.ARQUEIRO_FANTASMA.id
        )
    )
}
