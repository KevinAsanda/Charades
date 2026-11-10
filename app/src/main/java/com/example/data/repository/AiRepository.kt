package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.CharadesCard
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiRepository"

enum class CardImageSize(val label: String, val imageSize: ImageSize) {
  SIZE_1K("1K Standard (1024)", ImageSize.SIZE_1K),
  SIZE_2K("2K High-Res (2048)", ImageSize.SIZE_2K),
  SIZE_4K("4K Ultra-Res (4096)", ImageSize.SIZE_4K)
}

data class GeneratedDeck(
  val title: String,
  val description: String,
  val cards: List<CharadesCard>
)

class AiRepository {

  // 1. Ultra-fast model: gemini-3.1-flash-lite for Slang Explainer & Clue Coach
  private val fastModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.1-flash-lite"
    )
  }

  // 2. General-use model: gemini-3.8-flash for Structured Deck Generation
  private val deckCardSchema = Schema.obj(
    properties = mapOf(
      "term" to Schema.string(description = "The South African charades word, title, or slang phrase"),
      "category" to Schema.string(description = "Category such as 2000s TV, Kwaito, Slang, Food, Sports"),
      "hints" to Schema.array(items = Schema.string(), description = "List of 3 silent acting clues"),
      "culturalEra" to Schema.string(description = "Cultural era e.g. Early 2000s"),
      "culturalTrivia" to Schema.string(description = "A 1-sentence authentic Mzansi trivia fact")
    )
  )

  private val deckJsonSchema = Schema.obj(
    properties = mapOf(
      "deckTitle" to Schema.string(description = "Name of the customized charades deck"),
      "deckDescription" to Schema.string(description = "Catchy description of the deck"),
      "cards" to Schema.array(items = deckCardSchema, description = "Array of charades cards")
    )
  )

  private val deckModel by lazy {
    val config = generationConfig {
      responseMimeType = "application/json"
      responseSchema = deckJsonSchema
    }
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      generationConfig = config
    )
  }

  // 3. Complex Reasoning with High Thinking Mode: gemini-4-argon with ThinkingLevel.HIGH
  // Note: maxOutputTokens is deliberately NOT set per feature requirement.
  private val refereeModel by lazy {
    val config = generationConfig {
      thinkingConfig = thinkingConfig {
        thinkingLevel = ThinkingLevel.HIGH
      }
    }
    Firebase.ai.generativeModel(
      modelName = "gemini-4-argon",
      generationConfig = config
    )
  }

  /**
   * Fast task: Slang Explainer & Clue Coach streaming using gemini-3.1-flash-lite
   */
  fun explainSlangAndCluesStream(slangOrTerm: String): Flow<String> = flow {
    val prompt = """
      You are an authentic, energetic South African party coach and slang guru.
      Analyze this South African cultural term or slang: "$slangOrTerm".
      Provide:
      1. What it means & its cultural origin (focusing on early 2000s / township / Mzansi history).
      2. 3 actionable, hilarious, silent acting gestures/clues for someone acting this out in Charades.
      3. Common do's & don'ts when miming this word.
      Keep it vibrant, authentic, engaging, and rich with Mzansi warmth!
    """.trimIndent()

    try {
      withTimeout(60_000L) {
        val stream = fastModel.generateContentStream(prompt)
        val builder = java.lang.StringBuilder()
        stream.collect { chunk ->
          val text = chunk.text.orEmpty()
          builder.append(text)
          emit(builder.toString())
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Fast slang explainer failed", e)
      throw e
    }
  }

  /**
   * General task: Generate customized Mzansi Charades Deck with structured JSON using gemini-3.8-flash
   */
  suspend fun generateThemedDeck(themePrompt: String): GeneratedDeck {
    val prompt = """
      Generate an authentic South African Charades deck based on: "$themePrompt".
      Emphasize South African culture dating back to the early 2000s, iconic television, kwaito, local slang, food, and historical moments.
      Generate 6 unique, exciting cards.
    """.trimIndent()

    try {
      val response = withTimeout(60_000L) {
        deckModel.generateContent(prompt)
      }
      val jsonString = response.text ?: throw IllegalStateException("Model returned empty text.")
      val json = JSONObject(jsonString)
      val title = json.optString("deckTitle", "Custom Mzansi Deck")
      val desc = json.optString("deckDescription", "AI Generated South African Deck")
      val cardsArray = json.getJSONArray("cards")

      val cards = (0 until cardsArray.length()).map { idx ->
        val item = cardsArray.getJSONObject(idx)
        val hintsArr = item.optJSONArray("hints")
        val hints = if (hintsArr != null) {
          (0 until hintsArr.length()).map { hintsArr.getString(it) }
        } else listOf("Act out naturally")

        CharadesCard(
          id = "ai_card_$idx",
          term = item.optString("term", "Mzansi Card"),
          category = item.optString("category", "Culture"),
          hints = hints,
          culturalEra = item.optString("culturalEra", "Early 2000s"),
          culturalTrivia = item.optString("culturalTrivia", "")
        )
      }

      return GeneratedDeck(title = title, description = desc, cards = cards)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Deck generation failed", e)
      throw e
    }
  }

  /**
   * Complex Reasoning with High Thinking Mode: gemini-4-argon with thinkingLevel = ThinkingLevel.HIGH
   * Solves party disputes and deep cultural referee questions
   */
  suspend fun adjudicateCulturalDispute(query: String): String {
    val prompt = """
      You are the Chief Supreme Referee of South African Cultural History and 2000s Nostalgia.
      Two teams in a charades match have a dispute or debate:
      "$query"

      Perform a deep, reasoned analysis:
      1. Examine the historical and cultural truth (dating back to the early 2000s or 90s in South Africa).
      2. Analyze if the mime/action or clue was culturally authentic to Mzansi traditions, slang, or media.
      3. Render an official, fair, and entertaining Referee Verdict (Award Point, Deduct Point, or Bonus Round).
      Include deep South African nostalgia and authentic township/suburban cultural context.
    """.trimIndent()

    try {
      val response = withTimeout(60_000L) {
        refereeModel.generateContent(prompt)
      }
      return response.text ?: "The referee could not reach a verdict."
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Referee dispute adjudication failed", e)
      throw e
    }
  }

  /**
   * High-Quality Image Generation: model gemini-3-pro-image-preview with user selectable size (1K, 2K, 4K)
   */
  suspend fun generateCardArt(prompt: String, size: CardImageSize): Bitmap? {
    val fullPrompt = "Vibrant South African retro pop art illustration celebrating: $prompt, colorful nostalgic early 2000s Mzansi party game card illustration, crisp details, theatrical party atmosphere"

    val config = generationConfig {
      responseModalities = listOf(ResponseModality.IMAGE)
      imageConfig = ImageConfig.builder()
        .setImageSize(size.imageSize)
        .build()
    }

    val imageModel = Firebase.ai.generativeModel(
      modelName = "gemini-3-pro-image-preview",
      generationConfig = config
    )

    try {
      val response = withTimeout(60_000L) {
        imageModel.generateContent(fullPrompt)
      }
      val imagePart = response.candidates.firstOrNull()
        ?.content?.parts?.filterIsInstance<ImagePart>()
        ?.firstOrNull()

      return imagePart?.image
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Image generation with gemini-3-pro-image-preview failed", e)
      throw e
    }
  }
}
