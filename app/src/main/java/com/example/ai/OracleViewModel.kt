package com.example.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.EnemyEncounter
import com.example.domain.model.Skill
import com.example.ui.viewmodel.AeldrinUiState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "OracleViewModel"

data class StructuredComboPlan(
    val comboTitle: String,
    val synergySteps: List<String>,
    val paradoxTip: String,
    val secretTrialHint: String
)

sealed interface OracleUiState {
    data object Idle : OracleUiState
    data object Loading : OracleUiState
    data class Streaming(val text: String) : OracleUiState
    data class SuccessText(val text: String) : OracleUiState
    data class SuccessCombo(val plan: StructuredComboPlan) : OracleUiState
    data class Error(val message: String) : OracleUiState
}

class OracleViewModel : ViewModel() {

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "Você é o Oráculo dos Sete Pilares do RPG Aeldrin. Responda sempre em português do Brasil, de forma objetiva, imersiva e estratégica. " +
                        "Você domina as 5 classes clássicas (Guerreiro, Mago, Ladino, Arqueiro, Clérigo), 30 subclasses (21 padrão + 9 secretas: Tirano de Sangue, Quebrador de Céus, Arquimago do Caos Primordial, Escriba das Estrelas, Caminhante do Véu, Ceifador Carmesim, Caçador de Dragões, Arqueiro Fantasma, Apóstolo do Crepúsculo), " +
                        "as 300 habilidades (001-300, incluindo 40 evolutivas e 10 paradoxais) e os chefes (Vaelen, Lyraen, Sera do Vento Branco, O Zero, Malakor e O Arquiteto do Vazio)."
                )
            }
        )
    }

    private val comboSchema = Schema.obj(
        properties = mapOf(
            "comboTitle" to Schema.string(description = "Nome épico da sinergia de combate"),
            "synergySteps" to Schema.array(
                items = Schema.string(),
                description = "3 a 4 passos táticos em ordem de execução usando habilidades e parry/sobrecarga"
            ),
            "paradoxTip" to Schema.string(description = "Como aproveitar uma habilidade Quebra-Jogo (291-300) contra o chefe atual"),
            "secretTrialHint" to Schema.string(description = "Dica prática para avançar no desbloqueio de uma classe secreta")
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = comboSchema
            }
        )
    }

    private val _uiState = MutableStateFlow<OracleUiState>(OracleUiState.Idle)
    val uiState: StateFlow<OracleUiState> = _uiState.asStateFlow()

    fun askOracleStream(question: String, gameState: AeldrinUiState) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = OracleUiState.Loading
            val prompt = buildContextPrompt(cleanQuestion, gameState)
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    chatModel.generateContentStream(prompt).collect { chunk ->
                        accumulated.append(chunk.text.orEmpty())
                        _uiState.value = OracleUiState.Streaming(accumulated.toString())
                    }
                }
                if (accumulated.isNotBlank()) {
                    _uiState.value = OracleUiState.SuccessText(accumulated.toString())
                } else {
                    _uiState.value = OracleUiState.Error("O Oráculo retornou uma resposta vazia.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Oracle streaming timed out", e)
                _uiState.value = OracleUiState.Error("A consulta ao Oráculo expirou após 60 segundos. Tente novamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Oracle streaming failed", e)
                _uiState.value = OracleUiState.Error(e.localizedMessage ?: "Falha ao consultar o Gemini via Firebase AI Logic.")
            }
        }
    }

    fun generateStructuredCombo(gameState: AeldrinUiState) {
        viewModelScope.launch {
            _uiState.value = OracleUiState.Loading
            val prompt = buildContextPrompt(
                "Crie um plano tático estruturado em JSON para derrotar o chefe atual usando minha classe, subclasse e habilidades equipadas em Aeldrin.",
                gameState
            )
            try {
                val response = withTimeout(60_000L) {
                    structuredModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("Resposta JSON vazia.")
                val json = JSONObject(jsonText)
                val title = json.getString("comboTitle")
                val stepsArray = json.getJSONArray("synergySteps")
                val steps = (0 until stepsArray.length()).map { stepsArray.getString(it) }
                val paradoxTip = json.getString("paradoxTip")
                val secretHint = json.getString("secretTrialHint")

                _uiState.value = OracleUiState.SuccessCombo(
                    StructuredComboPlan(
                        comboTitle = title,
                        synergySteps = steps,
                        paradoxTip = paradoxTip,
                        secretTrialHint = secretHint
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Structured combo generation timed out", e)
                _uiState.value = OracleUiState.Error("Tempo esgotado ao gerar plano tático (60s). Tente novamente.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Structured combo generation failed", e)
                _uiState.value = OracleUiState.Error(e.localizedMessage ?: "Erro ao gerar combo estruturado.")
            }
        }
    }

    private fun buildContextPrompt(userPrompt: String, state: AeldrinUiState): String {
        val stats = state.stats
        val encounter: EnemyEncounter = state.combat.encounter
        val skillsSummary = state.equippedSkills.joinToString("; ") { skill: Skill ->
            "#${skill.code} ${skill.name} (${skill.category.label}, ${skill.manaCost} Mana, CD ${skill.cooldownTurns}t)"
        }
        return """
            Contexto atual do jogador em Aeldrin:
            - Classe: ${stats.heroClass.displayName} | Subclasse: ${stats.activeSubclass.name} | Nível: ${state.save.level}
            - Atributos: HP ${stats.maxHp}, Mana ${stats.maxMana}, ATK ${stats.atk}, DEF ${stats.def}, Crit ${stats.critPercent}%
            - Habilidades equipadas: $skillsSummary
            - Chefe selecionado na arena: ${encounter.name} (${encounter.tier.label}, Região ${encounter.regionId}, HP ${encounter.maxHp}, Dica: ${encounter.mechanicHint})
            - Secretas desbloqueadas: ${state.save.unlockedSecretIds().size}/9

            Pedido do jogador:
            $userPrompt
        """.trimIndent()
    }
}
