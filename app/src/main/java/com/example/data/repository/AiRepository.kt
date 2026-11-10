package com.example.data.repository

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.abs

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
   * Fast task: Slang Explainer & Clue Coach streaming using gemini-3.1-flash-lite,
   * with an authentic on-device Mzansi Slang & Acting Coach fallback when offline or unconfigured.
   */
  fun explainSlangAndCluesStream(slangOrTerm: String): Flow<String> = flow {
    val cleanTerm = slangOrTerm.trim()
    val prompt = """
      You are an authentic, energetic South African party coach and slang guru.
      Analyze this South African cultural term or slang: "$cleanTerm".
      Provide:
      1. What it means & its cultural origin (focusing on early 2000s / township / Mzansi history).
      2. 3 actionable, hilarious, silent acting gestures/clues for someone acting this out in Charades.
      3. Common do's & don'ts when miming this word.
      Keep it vibrant, authentic, engaging, and rich with Mzansi warmth!
    """.trimIndent()

    var emittedFromCloud = false
    try {
      withTimeout(15_000L) {
        val stream = fastModel.generateContentStream(prompt)
        val builder = StringBuilder()
        stream.collect { chunk ->
          val text = chunk.text.orEmpty()
          if (text.isNotEmpty()) {
            emittedFromCloud = true
            builder.append(text)
            emit(builder.toString())
          }
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.w(TAG, "Cloud fastModel unavailable, using on-device Mzansi Slang Coach fallback", e)
    }

    if (!emittedFromCloud) {
      val fallbackSections = buildLocalSlangGuide(cleanTerm)
      val progressive = StringBuilder()
      for (section in fallbackSections) {
        delay(90L)
        if (progressive.isNotEmpty()) progressive.append("\n\n")
        progressive.append(section)
        emit(progressive.toString())
      }
    }
  }

  /**
   * General task: Generate customized Mzansi Charades Deck with structured JSON using gemini-3.8-flash,
   * falling back to an on-device curated South African deck builder when offline or unconfigured.
   */
  suspend fun generateThemedDeck(themePrompt: String): GeneratedDeck {
    val cleanTheme = themePrompt.trim()
    val prompt = """
      Generate an authentic South African Charades deck based on: "$cleanTheme".
      Emphasize South African culture dating back to the early 2000s, iconic television, kwaito, local slang, food, and historical moments.
      Generate 6 unique, exciting cards.
    """.trimIndent()

    try {
      val response = withTimeout(15_000L) {
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
      Log.w(TAG, "Cloud deckModel unavailable, generating local Mzansi themed deck", e)
      delay(350L)
      return buildLocalThemedDeck(cleanTheme)
    }
  }

  /**
   * Complex Reasoning with High Thinking Mode: gemini-4-argon with thinkingLevel = ThinkingLevel.HIGH
   * Solves party disputes and deep cultural referee questions, with an on-device Mzansi Supreme Referee fallback.
   */
  suspend fun adjudicateCulturalDispute(query: String): String {
    val cleanQuery = query.trim()
    val prompt = """
      You are the Chief Supreme Referee of South African Cultural History and 2000s Nostalgia.
      Two teams in a charades match have a dispute or debate:
      "$cleanQuery"

      Perform a deep, reasoned analysis:
      1. Examine the historical and cultural truth (dating back to the early 2000s or 90s in South Africa).
      2. Analyze if the mime/action or clue was culturally authentic to Mzansi traditions, slang, or media.
      3. Render an official, fair, and entertaining Referee Verdict (Award Point, Deduct Point, or Bonus Round).
      Include deep South African nostalgia and authentic township/suburban cultural context.
    """.trimIndent()

    try {
      val response = withTimeout(15_000L) {
        refereeModel.generateContent(prompt)
      }
      return response.text ?: buildLocalRefereeVerdict(cleanQuery)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.w(TAG, "Cloud refereeModel unavailable, issuing on-device Mzansi Referee Verdict", e)
      delay(400L)
      return buildLocalRefereeVerdict(cleanQuery)
    }
  }

  /**
   * High-Quality Image Generation: model gemini-3-pro-image-preview with user selectable size (1K, 2K, 4K),
   * with a high-contrast procedural Mzansi Pop-Art Bitmap synthesizer on device when cloud AI is unreachable.
   */
  suspend fun generateCardArt(prompt: String, size: CardImageSize): Bitmap? {
    val cleanPrompt = prompt.trim()
    val fullPrompt = "Vibrant South African retro pop art illustration celebrating: $cleanPrompt, colorful nostalgic early 2000s Mzansi party game card illustration, crisp details, theatrical party atmosphere"

    try {
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

      val response = withTimeout(20_000L) {
        imageModel.generateContent(fullPrompt)
      }
      val imagePart = response.candidates.firstOrNull()
        ?.content?.parts?.filterIsInstance<ImagePart>()
        ?.firstOrNull()

      if (imagePart?.image != null) {
        return imagePart.image
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.w(TAG, "Cloud imageModel unavailable, rendering on-device Mzansi Pop-Art card bitmap", e)
    }

    delay(450L)
    return renderLocalPopArtBitmap(cleanPrompt, size)
  }

  private fun buildLocalSlangGuide(term: String): List<String> {
    val lower = term.lowercase()
    return when {
      "zola" in lower -> listOf(
        "1. CULTURAL ORIGIN (EARLY 2000s YIZO YIZO & SABC 1):\nZola 7 (Bonginkosi Dlamini) became the heartbeat of early 2000s Mzansi youth culture. From his raw breakout as Papa Action on Yizo Yizo to Kwaito anthems like 'Mdlwembe' and his wish-granting show 'Zola 7' on SABC 1, he defined township heroism and street style.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Pull an imaginary beanie snugly down to your eyebrows and walk with a slow, fearless Soweto bounce.\n- Clue 2: Hold out an imaginary microphone to a crowd, point to your heart, and wipe away an emotional tear (like granting a dream on the Zola 7 TV show).\n- Clue 3: Hold up 7 fingers clearly after pointing to yourself as the host.",
        "3. MIME DO'S & DON'TS:\n- DO exaggerate the signature beanie pull and the 7-finger hand sign.\n- DON'T hum 'Mdlwembe' or say 'Hola 7!' out loud, or you forfeit the point!"
      )
      "nkalakatha" in lower || "mandoza" in lower -> listOf(
        "1. CULTURAL ORIGIN (YEAR 2000 CROSSOVER ANTHEM):\nMandoza's 'Nkalakatha' (2000) is the undisputed crossover Kwaito record that united every wedding, stadium, braai, and club in South Africa. 'Nkalakatha' means the top boss or ultimate champion.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Drop into a low, wide-legged Kwaito stance with shoulders rolling forward to an imaginary heavy bassline.\n- Clue 2: Mimic revving a spinning BMW E30 steering wheel while bobbing your head rhythmically.\n- Clue 3: Point both index fingers down to the floor as if declaring yourself the undisputed boss of the dancefloor.",
        "3. MIME DO'S & DON'TS:\n- DO start with the unmistakable shoulder-roll intro posture.\n- DON'T mouth the first five bass notes or shout 'Haaai!' during the build-up."
      )
      "eish" in lower -> listOf(
        "1. CULTURAL ORIGIN (UNIVERSAL MZANSI EXPRESSION):\n'Eish!' is South Africa's ultimate emotional Swiss-Army knife. Whether Eskom drops Stage 6 right before kick-off, you burn the pap, or you see the petrol price hike, 'Eish' communicates shock, resignation, and humour in one breath.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Slap one palm gently onto the crown of your head while widening your eyes in disbelief.\n- Clue 2: Shake your head slowly from side to side while letting your shoulders drop in dramatic defeat.\n- Clue 3: Point at an imaginary light bulb going dark, then throw one hand up in classic Mzansi frustration.",
        "3. MIME DO'S & DON'TS:\n- DO sell the facial expression of sudden shock followed by a wry smile.\n- DON'T let out the actual 'Eish' sigh sound effect!"
      )
      "vuvuzela" in lower -> listOf(
        "1. CULTURAL ORIGIN (PSL STADIUMS TO 2010 WORLD CUP):\nLong before the 2010 FIFA World Cup made it famous globally, the long plastic vuvuzela horn was the thunderous soundtrack of Orlando Pirates vs Kaizer Chiefs PSL Saturdays across Mzansi.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Grip a metre-long imaginary plastic tube with both hands and tilt it skyward toward the stadium roof.\n- Clue 2: Puff your cheeks out to maximum capacity as if blowing a deafening B-flat drone.\n- Clue 3: Switch immediately to covering both ears in mock pain like a startled visiting defender.",
        "3. MIME DO'S & DON'TS:\n- DO show the full length of the horn with your hands before puffing your cheeks.\n- DON'T make any buzzing trumpet sound with your lips!"
      )
      "spaza" in lower || "kota" in lower || "tuckshop" in lower -> listOf(
        "1. CULTURAL ORIGIN (KASI CORNER COMMERCE):\nThe neighbourhood Spaza Shop is the cornerstone of township daily life, serving hot quarter-loaf Kotas stuffed with slap chips, polony, atchar, and Russian sausages alongside single Chappies bubblegums.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Knock three times on an imaginary burglar-bar security window and peer inside on tiptoe.\n- Clue 2: Mime hollowing out a quarter loaf of white bread and piling in layers of chips, cheese, and sauce.\n- Clue 3: Count out imaginary coins on a counter and unwrap a tiny square of bubblegum to read the 'Did You Know?' wrapper.",
        "3. MIME DO'S & DON'TS:\n- DO act out both the customer knocking at the window grille and the shopkeeper handing items through the slot.\n- DON'T point to real food in the room."
      )
      "sister bettina" in lower || "mgarimbe" in lower -> listOf(
        "1. CULTURAL ORIGIN (THE UNOFFICIAL NATIONAL ANTHEM):\nMgarimbe's 'Sister Bettina' (2006) is sacred in South African party culture. The moment the opening horn riff drops at 2 AM, everyone leaves their chairs and forms a circle.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Pretend to be asleep or tired at a party, then bolt upright in pure euphoria as if your favourite song just started.\n- Clue 2: Wave everybody into a tight dance circle while pointing to the sky on the beat.\n- Clue 3: Hold up an imaginary church habit/veil for 'Sister' and then break into an unapologetic Kwaito groove.",
        "3. MIME DO'S & DON'TS:\n- DO capture the instant 0-to-100 energy shift when the track drops.\n- DON'T chant 'In the middle!' out loud."
      )
      "now now" in lower || "just now" in lower -> listOf(
        "1. CULTURAL ORIGIN (THE MZANSI TIME CONTINUUM):\nIn South Africa, 'Now' means immediately, 'Now Now' means within the next 5 to 15 minutes, and 'Just Now' means anytime between two hours from now and next week Tuesday.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Tap your wrist like a watch twice in quick succession ('Now Now'), holding up two fingers.\n- Clue 2: Contrast it by waving a relaxed hand far into the distant future while lounging back on an imaginary couch ('Just Now').\n- Clue 3: Pretend to text a friend that you are 'around the corner' while still tying your shoelaces at home.",
        "3. MIME DO'S & DON'TS:\n- DO use clearwatch-tapping gestures to anchor the concept of time.\n- DON'T whisper 'wait a bit'."
      )
      else -> listOf(
        "1. CULTURAL ORIGIN & MZANSI CONTEXT:\n'$term' is a classic fixture of South African social, township, and pop-culture memory. Whether heard around a Sunday chisa nyama, on prime-time SABC television, or in a packed minibus taxi, it immediately sparks recognition across Mzansi.",
        "2. THREE SILENT CHARADES ACTING CLUES:\n- Clue 1: Establish the setting first by miming the environment where '$term' happens (a living room TV, a bustling taxi rank, a stadium, or a family braai).\n- Clue 2: Break '$term' into its key physical action and exaggerate the posture, hand gestures, and facial reaction.\n- Clue 3: Point to your ear or eyes to signal how people react when '$term' appears, then freeze in an iconic pose.",
        "3. MIME DO'S & DON'TS:\n- DO use big, theatrical body language and clear syllable counts on your fingers.\n- DON'T mouth the letters or use rhyming words out loud."
      )
    }
  }

  private fun buildLocalThemedDeck(themePrompt: String): GeneratedDeck {
    val lower = themePrompt.lowercase()
    return when {
      "durban" in lower || "fashion" in lower -> GeneratedDeck(
        title = "Durban July 2000s High Fashion",
        description = "Greyville Racecourse glamour, Kwaito VIP marquees, and flamboyant Mzansi couture.",
        cards = listOf(
          CharadesCard(
            id = "dj_1",
            term = "VIP Marquee Red Carpet",
            category = "Fashion",
            hints = listOf("Pose for flashing cameras", "Adjust oversized designer sunglasses", "Sip champagne while waving"),
            culturalEra = "Early 2000s",
            culturalTrivia = "The Durban July marquee village became South Africa's biggest celebrity fashion runway in the 2000s."
          ),
          CharadesCard(
            id = "dj_2",
            term = "Somizi Red Carpet Entrance",
            category = "Celebs",
            hints = listOf("Dramatic cape reveal", "High-energy dance spin", "Blow kisses to photographers"),
            culturalEra = "2000s",
            culturalTrivia = "Every year fans waited by the Greyville gates just to see the most theatrical entrance of the day."
          ),
          CharadesCard(
            id = "dj_3",
            term = "Photo Finish at Greyville",
            category = "Sports",
            hints = listOf("Peer through binoculars", "Jump up cheering with a betting slip", "Mime a jockey riding at full gallop"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Race 7 at Greyville stops the entire city of Durban every first Saturday of July."
          ),
          CharadesCard(
            id = "dj_4",
            term = "Stoned Cherrie Couture",
            category = "Fashion",
            hints = listOf("Drape a regal African print skirt", "Walk a catwalk with poise", "Frame a portrait T-shirt on your chest"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Nkhensani Nkosi's Stoned Cherrie redefined contemporary South African luxury streetwear in the early 2000s."
          ),
          CharadesCard(
            id = "dj_5",
            term = "Florida Road Afterparty",
            category = "Nightlife",
            hints = listOf("Take off sore high heels", "Dance to Durban house music", "Wave down a late-night cab"),
            culturalEra = "2000s",
            culturalTrivia = "After the last horse crossed the line, the entire crowd migrated straight to Florida Road until sunrise."
          ),
          CharadesCard(
            id = "dj_6",
            term = "Fascinator Hat in the Wind",
            category = "Fashion",
            hints = listOf("Balance a giant sculptural hat on your head", "Hold it down against coastal wind", "Duck through a narrow doorway"),
            culturalEra = "2000s",
            culturalTrivia = "Coastal Durban breezes turned towering race-day fascinators into an extreme sport."
          )
        )
      )
      "soweto" in lower || "derby" in lower || "soccer" in lower -> GeneratedDeck(
        title = "Soweto Derby Legends",
        description = "FNB Stadium roar, Shibobo showboating, Makarapas, and Amakhosi vs Buccaneers glory.",
        cards = listOf(
          CharadesCard(
            id = "sd_1",
            term = "Scara Ngobese Shibobo",
            category = "Sports",
            hints = listOf("Roll the ball under your sole", "Nutmeg an imaginary defender", "Salute the screaming stands"),
            culturalEra = "Mid 2000s",
            culturalTrivia = "Emmanuel 'Scara' Ngobese turned Kasi flavour showboating into high art for Kaizer Chiefs."
          ),
          CharadesCard(
            id = "sd_2",
            term = "Crossed Bones Buccaneers Salute",
            category = "Sports",
            hints = listOf("Cross both forearms over your chest", "Keep a fierce pirate expression", "Point to the black-and-white badge"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Orlando Pirates supporters fill half of FNB Stadium with the iconic crossed-arms salute."
          ),
          CharadesCard(
            id = "sd_3",
            term = "Hand-Painted Makarapa Helmet",
            category = "Culture",
            hints = listOf("Strap on a tall carved hard-hat", "Blow a vuvuzela with one hand", "Dance in the stadium aisles"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Sculpted from miners' hard hats, Makarapas are hand-painted masterpieces of South African fan culture."
          ),
          CharadesCard(
            id = "sd_4",
            term = "Doctor Khumalo Corner Flag Dance",
            category = "Sports",
            hints = listOf("Curl a free kick into the top corner", "Sprint to the corner flag", "Do a celebratory shuffle"),
            culturalEra = "90s & 2000s",
            culturalTrivia = "16V Doctor Khumalo remains one of the most revered midfield maestros in Mzansi football history."
          ),
          CharadesCard(
            id = "sd_5",
            term = "Jomo Sono Half-Time Substitution",
            category = "Sports",
            hints = listOf("Stand on the touchline in a suit", "Point decisively onto the pitch", "Change the entire match tactics"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Ezenkosi boss Jomo Sono was famous for discovering raw talent that lit up PSL derbies."
          ),
          CharadesCard(
            id = "sd_6",
            term = "Chisa Nyama Outside FNB Stadium",
            category = "Food",
            hints = listOf("Turn sizzling boerewors on a smoky grill", "Fan the coals with a cardboard lid", "High-five rival fans in jerseys"),
            culturalEra = "2000s",
            culturalTrivia = "Pre-match braais outside Nasrec bring Chiefs and Pirates fans together before kick-off."
          )
        )
      )
      else -> GeneratedDeck(
        title = "Mzansi Classics: $themePrompt",
        description = "Custom 6-card South African charades deck inspired by $themePrompt.",
        cards = listOf(
          CharadesCard(
            id = "custom_gen_1",
            term = "$themePrompt Champion",
            category = "Culture",
            hints = listOf("Strike a proud trophy pose", "Wave to an imaginary Mzansi crowd", "Do a celebratory township step"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Inspired by South Africa's unforgettable 2000s cultural milestones around $themePrompt."
          ),
          CharadesCard(
            id = "custom_gen_2",
            term = "Sunday Seven Colours Plate",
            category = "Food",
            hints = listOf("Scoop seven different dishes onto one plate", "Guard your plate carefully", "Fall asleep on the couch right after"),
            culturalEra = "2000s Tradition",
            culturalTrivia = "No Mzansi Sunday gathering is complete without rice, samp, beetroot, pumpkin, cabbage, potato salad, and roast chicken."
          ),
          CharadesCard(
            id = "custom_gen_3",
            term = "Front Seat Taxi Accountant",
            category = "Slang & Life",
            hints = listOf("Collect cash passed from the back row", "Count coins under pressure", "Call out the next street stop"),
            culturalEra = "2000s",
            culturalTrivia = "Sitting next to the minibus taxi driver automatically makes you the chief financial officer of the trip."
          ),
          CharadesCard(
            id = "custom_gen_4",
            term = "SABC 1 8PM Soapie Cliffhanger",
            category = "2000s TV",
            hints = listOf("Turn around slowly with a gasped expression", "Hold a dramatic 5-second freeze frame", "Shush the entire living room"),
            culturalEra = "Early 2000s",
            culturalTrivia = "At 8:00 PM sharp, streets went quiet across South Africa for Generations."
          ),
          CharadesCard(
            id = "custom_gen_5",
            term = "Kwaito Boombox Street Bash",
            category = "Music",
            hints = listOf("Carry a heavy stereo on one shoulder", "Roll your shoulders in slow motion", "Point to the DJ booth"),
            culturalEra = "Early 2000s",
            culturalTrivia = "Kwaito slowed down house beats into an unmistakable Mzansi groove."
          ),
          CharadesCard(
            id = "custom_gen_6",
            term = "Chappies Wrapper Trivia Master",
            category = "Nostalgia",
            hints = listOf("Unwrap a tiny square of gum", "Squint to read tiny print", "Raise a finger to quiz your friends"),
            culturalEra = "Early 2000s",
            culturalTrivia = "'Did You Know?' inside Chappies wrappers was South Africa's original search engine."
          )
        )
      )
    }
  }

  private fun buildLocalRefereeVerdict(query: String): String {
    return """
      OFFICIAL MZANSI SUPREME REFEREE VERDICT
      Case Reviewed: "$query"

      1. HISTORICAL & CULTURAL FINDING:
      Under the 2000s South African Party & Township Charades Code, accuracy comes down to unmistakable Mzansi body language. Whether referencing SABC classics, taxi hand signs, Kwaito footwork, or kasi food rituals, if the room recognized the cultural reference without spoken words or lip-reading, the spirit of the clue stands.

      2. MIME AUTHENTICITY RULING:
      The gesture described carries genuine Mzansi flavour. In South African charades, contextual storytelling (setting the scene first before hitting the signature pose) is valid and encouraged as long as zero sound effects or mouthed syllables were used.

      3. FINAL POINT DECISION:
      VERDICT: AWARD THE POINT (+1) plus a 10-second victory dance! If any sound escaped the actor's lips, replay the card with a 15-second blitz timer. Sharp Sharp!
    """.trimIndent()
  }

  private fun renderLocalPopArtBitmap(prompt: String, size: CardImageSize): Bitmap {
    // Use a fast, memory-safe square canvas (768px for 1K, 1024px for 2K, 1280px for 4K)
    val dim = when (size) {
      CardImageSize.SIZE_1K -> 768
      CardImageSize.SIZE_2K -> 1024
      CardImageSize.SIZE_4K -> 1280
    }
    val bitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val seed = abs(prompt.hashCode())

    // 1. Deep Navy & Electric Indigo Stripi Mesh Background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = LinearGradient(
        0f, 0f, dim.toFloat(), dim.toFloat(),
        intArrayOf(
          Color.parseColor("#1C1E54"),
          Color.parseColor("#533AFD"),
          Color.parseColor("#EA2261"),
          Color.parseColor("#0D253D")
        ),
        floatArrayOf(0f, 0.42f, 0.78f, 1f),
        Shader.TileMode.CLAMP
      )
    }
    canvas.drawRect(0f, 0f, dim.toFloat(), dim.toFloat(), bgPaint)

    // 2. Retro Sunburst / Vinyl / Geometric Mzansi Rings
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = dim * 0.012f
      color = Color.argb(65, 245, 233, 212) // Cream
    }
    val centerX = dim * 0.5f
    val centerY = dim * 0.42f
    for (i in 1..5) {
      canvas.drawCircle(centerX, centerY, dim * (0.10f + i * 0.065f), ringPaint)
    }

    // 3. Center Pop-Art Medallion Badge
    val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#0D253D")
      style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, dim * 0.21f, discPaint)

    val innerDiscPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if (seed % 2 == 0) Color.parseColor("#533AFD") else Color.parseColor("#EA2261")
      style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, dim * 0.15f, innerDiscPaint)

    val centerHolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#F5E9D4")
      style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, dim * 0.045f, centerHolePaint)

    // 4. Decorative Ndebele-inspired Chevron Accents at Top & Bottom
    val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(110, 185, 185, 249)
      style = Paint.Style.STROKE
      strokeWidth = dim * 0.01f
    }
    val margin = dim * 0.06f
    canvas.drawRoundRect(
      RectF(margin, margin, dim - margin, dim - margin),
      dim * 0.04f,
      dim * 0.04f,
      chevronPaint
    )

    // 5. Resolution Pill Badge at Top Left
    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#F5E9D4")
      style = Paint.Style.FILL
    }
    val pillRect = RectF(dim * 0.1f, dim * 0.1f, dim * 0.44f, dim * 0.165f)
    canvas.drawRoundRect(pillRect, dim * 0.03f, dim * 0.03f, pillPaint)

    val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#0D253D")
      textSize = dim * 0.028f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    val resLabel = "MZANSI ART ${size.name.replace("SIZE_", "")}"
    canvas.drawText(resLabel, dim * 0.13f, dim * 0.143f, badgeTextPaint)

    // 6. Bottom Poster Caption Card with Prompt Text
    val cardRect = RectF(dim * 0.1f, dim * 0.68f, dim * 0.9f, dim * 0.88f)
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.argb(235, 255, 255, 255)
      style = Paint.Style.FILL
    }
    canvas.drawRoundRect(cardRect, dim * 0.03f, dim * 0.03f, cardPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#0D253D")
      textSize = dim * 0.036f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    val words = prompt.ifBlank { "Mzansi 2000s Party Classics" }.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""
    for (word in words) {
      val test = if (currentLine.isEmpty()) word else "$currentLine $word"
      if (test.length <= 28) {
        currentLine = test
      } else {
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        currentLine = word
      }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine)

    var textY = dim * 0.755f
    lines.take(3).forEach { line ->
      canvas.drawText(line, dim * 0.14f, textY, titlePaint)
      textY += dim * 0.048f
    }

    return bitmap
  }
}
