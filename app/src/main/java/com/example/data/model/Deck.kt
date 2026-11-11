package com.example.data.model

data class CharadesCard(
  val id: String,
  val term: String,
  val category: String,
  val hints: List<String>,
  val culturalEra: String = "Early 2000s",
  val culturalTrivia: String = "",
  val difficultyTier: Int = 1, // 1 = Easy (Universal Mzansi), 2 = Medium (Timeline Regulars), 3 = Hard (Chronically Online)
  val recommendedMode: String = "act_it_out", // act_it_out, hit_the_beat, mzansi_30_seconds, my_reaction
  val eraTag: String = "2000s-2010s Heritage", // 2000s-2010s Heritage, 2020 Lockdown, 2021-2024 Piano Boom, 2025-2026 Current
  val isFestiveGroove: Boolean = false,
  val forbiddenWords: List<String> = emptyList()
) {
  fun effectiveForbiddenWords(): List<String> {
    if (forbiddenWords.isNotEmpty()) return forbiddenWords
    val stopWords = setOf("the", "and", "for", "with", "from", "into", "vs", "of", "in", "on", "to", "a")
    return term
      .replace("(", " ")
      .replace(")", " ")
      .replace("\"", "")
      .replace("'", "")
      .split(" ", "-", "/")
      .map { it.trim() }
      .filter { it.length >= 3 && it.lowercase() !in stopWords }
      .take(4)
  }

  fun tierLabel(): String = when (difficultyTier) {
    1 -> "Tier 1: Easy"
    2 -> "Tier 2: Medium"
    else -> "Tier 3: Hard"
  }
}

data class SignaturePhysicalCue(
  val id: String,
  val number: Int,
  val title: String,
  val eraOrContext: String,
  val instruction: String
)

data class Deck(
  val id: String,
  val name: String,
  val tagLine: String,
  val iconEmoji: String,
  val isFreeTier: Boolean,
  val priceCoins: Long = 0L,
  val cards: List<CharadesCard>
)

object PredefinedDecks {

  // Section 8 Blueprint: 6 Signature Mzansi Physical Cues for Onboarding & Tutorial
  val SIGNATURE_PHYSICAL_CUES = listOf(
    SignaturePhysicalCue(
      id = "cue_bacardi",
      number = 1,
      title = "The Bacardi Dance",
      eraOrContext = "Tyla 'Water' Challenge",
      instruction = "Perform the rhythmic Pretoria Bacardi hip move while miming pouring water down your back."
    ),
    SignaturePhysicalCue(
      id = "cue_sasl_sharp",
      number = 2,
      title = "SASL 'Sharp Sharp' & 'Vibe'",
      eraOrContext = "2023 SASL Bill Youth Signs",
      instruction = "Flash a rhythmic doubled-up thumbs-up for 'Sharp sharp', then a loose flick-of-the-wrist for 'Vibe'."
    ),
    SignaturePhysicalCue(
      id = "cue_shoe_kick",
      number = 3,
      title = "The 'Shoe Kick Transition'",
      eraOrContext = "2025/2026 TikTok Transition",
      instruction = "Balance an imaginary sneaker on your foot in a karate side-kick, kick forward, and freeze in a fresh outfit pose."
    ),
    SignaturePhysicalCue(
      id = "cue_sa_time",
      number = 4,
      title = "'Now, Just Now, Now Now'",
      eraOrContext = "Three Tiers of SA Time",
      instruction = "Point to your wristwatch while yawning for 'Just now', then tap your wrist urgently while grabbing keys for 'Now now'."
    ),
    SignaturePhysicalCue(
      id = "cue_dricus",
      number = 5,
      title = "Dricus 'Stillknocks' du Plessis",
      eraOrContext = "UFC Champion & 8% Oxygen",
      instruction = "Pinch your nostrils for '8% oxygen intake', then mime strapping the UFC Middleweight belt around your waist."
    ),
    SignaturePhysicalCue(
      id = "cue_bunny_chow",
      number = 6,
      title = "Durban Bunny Chow",
      eraOrContext = "Everyday Mzansi Culinary Icon",
      instruction = "Hop like a rabbit, tear the soft centre out of an imaginary loaf of bread, and scoop up spicy curry."
    )
  )

  // Section 9.1 Blueprint: Skip Penalty / Roast Audio & Visual Overlay Pack
  val SKIP_ROAST_LINES = listOf(
    "Is not make sure! Skipping a classic like that?",
    "O jewa ke eng? Your crew is watching in disbelief!",
    "Ngiyakhathala! Even Oubaas would have acted that out.",
    "You are being roasted in 5 languages in the X comment section!",
    "Hayibo! Please don't voke the timeline with that pass.",
    "Msunu Voetsek! Hand the phone to a real Grootman."
  )

  // =========================================================================
  // DECK 1: TRENDING APPS, STREAMING PLATFORMS & DIGITAL MILESTONES (2020-2026)
  // =========================================================================
  val CORE_TRENDING_APPS = Deck(
    id = "deck_trending_apps",
    name = "Trending Apps & Digital SA",
    tagLine = "TikTok FYP, Showmax 2026 shutdown, DStv Stream, X roasts & Takealot",
    iconEmoji = "📱",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "app_1",
        term = "TikTok (FYP)",
        category = "Digital Platform",
        hints = listOf(
          "Hold an imaginary phone vertically and scroll rapidly with your thumb",
          "Immediately burst into a 3-second Amapiano footwork routine",
          "Point to the camera as if filming a POV skit"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Jumped from 34.0% (2023) to 38.6% (2024) SA penetration per Ornico and World Wide Worx.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "app_2",
        term = "Showmax Standalone Shutdown",
        category = "Streaming Milestone",
        hints = listOf(
          "Mime binge-watching a tense telenovela like The Wife",
          "Suddenly mime the standalone app vanishing into thin air",
          "Merge both hands together into the DStv Stream logo"
        ),
        culturalEra = "30 April 2026",
        culturalTrivia = "Local content drove 57% of viewing in FY2023; the standalone app ended on 30 April 2026.",
        difficultyTier = 2,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_3",
        term = "DStv Stream",
        category = "Streaming Platform",
        hints = listOf(
          "Mime flipping between a royal historical drama with a spear (Shaka iLembe)",
          "Switch to a private-security action stance (Empini)",
          "Finish with a dramatic courtroom objection"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Absorbed the Showmax Originals catalogue in 2026 alongside Mzansi Magic hits Inimba and Empini.",
        difficultyTier = 2,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_4",
        term = "Netflix South Africa",
        category = "Streaming Platform",
        hints = listOf(
          "Perform the iconic two-knock 'Tudum' intro on a table",
          "Mime a high-stakes Johannesburg gold heist (iNumber Number: Jozi Gold)",
          "Pose like a glamorous influencer on the run (Bad Influencer)"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Home to global breakouts Bad Influencer (#3 global English series in 2025), Kings of Jo'Burg, Marked, and Umjolo.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_5",
        term = "X Comment Section Roast",
        category = "Social Media Trope",
        hints = listOf(
          "Mime furious two-thumb typing with a wicked grin",
          "Count to five on your fingers for the 5+ local languages used in the roast",
          "Wave someone away dismissively ('Tsek!')"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "The digital battlefield where South Africans unite to roast international accounts in 5+ local languages.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_6",
        term = "WhatsApp Family Voice Note",
        category = "Digital Habit",
        hints = listOf(
          "Hold the bottom edge of a phone close to your mouth like a walkie-talkie",
          "Deliver a dramatic 5-minute reaction to family gossip",
          "Hold the phone to your ear and nod approvingly as replies roll in"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Primary sharing channel for viral lockdown memes, comedy clips, and daily Mzansi family updates.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2020 Lockdown"
      ),
      CharadesCard(
        id = "app_7",
        term = "Viu South Africa",
        category = "Streaming App",
        hints = listOf(
          "Mime plugging earphones into your phone on a taxi commute",
          "Watch daily soapies on your screen",
          "Gasp dramatically at a Skeem Saam or Uzalo plot twist every 10 seconds"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Free-to-watch local streaming app hosting daily catch-ups of Skeem Saam, Uzalo, Muvhango, Generations, and 7de Laan.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "app_8",
        term = "CapCut & Clipchamp Transitions",
        category = "Creator Tool",
        hints = listOf(
          "Clap your hands or kick a shoe in mid-air and freeze",
          "Pretend to scrub and trim a video timeline with your index finger",
          "Tap in animated captions floating in front of your chest"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Video editing apps used by Mzansi creators to stitch outfit transitions and 'Cheer the Camera' drink swaps.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_9",
        term = "Takealot & The Jenny Ruyter Saga",
        category = "E-Commerce & Viral Ad",
        hints = listOf(
          "Mime clicking 'Buy Now' on your phone",
          "Peek impatiently through the living room curtains",
          "Sign for a blue delivery box and unbox it excitedly"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Featured in the 2023 DStv Content Creator Awards nominee 'Takealot | The Jenny Ruyter Saga' by The Cavalry.",
        difficultyTier = 3,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "app_10",
        term = "Cars.co.za Car Review",
        category = "Automotive Platform",
        hints = listOf(
          "Mime revving a steering wheel and checking the dashboard",
          "Walk around inspecting imaginary rims and boot space",
          "Film a smartphone car review like 2026 winner Tebogo Moropa"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Title sponsor of the Cars.co.za Automotive Content Award, won by Tebogo Moropa in October 2026.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_11",
        term = "eVOD Purple Carpet",
        category = "Broadcast Partner",
        hints = listOf(
          "Mime watching a glamorous purple-carpet awards show on your screen",
          "Pose for paparazzi flashes in Sandton",
          "Frantically type an SMS vote to 33622"
        ),
        culturalEra = "October 2026",
        culturalTrivia = "Official broadcast partner of the 2026 Samsung Galaxy Content Creator Awards.",
        difficultyTier = 3,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "app_12",
        term = "Fakaza & Afro House King",
        category = "Music Portal",
        hints = listOf(
          "Mime frantically searching your phone on a Friday afternoon",
          "Hit download on a freshly dropped Amapiano or 3-Step track",
          "Connect to the Bluetooth speaker and start the weekend groove"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "South African music portals tracking Amapiano, 3-Step, Gqom, and Afro House releases through 2026.",
        difficultyTier = 2,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      )
    )
  )

  // =========================================================================
  // DECK 2: TIKTOK CHALLENGES, VIRAL FORMATS & 2020 LOCKDOWN MEMES
  // =========================================================================
  val CORE_TIKTOK_LOCKDOWN = Deck(
    id = "deck_tiktok_lockdown",
    name = "TikTok Trends & Lockdown Vault",
    tagLine = "Bacardi dance, Oct 2026 TikTok formats, Day-50 Lockdown memes & timeline drama",
    iconEmoji = "🔥",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "tt_1",
        term = "Bacardi Dance (Tyla 'Water')",
        category = "Dance Challenge",
        hints = listOf(
          "Perform the viral rhythmic Pretoria Bacardi hip isolations",
          "Mime pouring a bottle of water down your back",
          "Point to the floor and insist it is called the Bacardi dance, not the Tyla dance"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Popularized globally alongside Tyla's 'Water'; Bona Magazine notes Tyla credits its original Pretoria name, the Bacardi dance.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "tt_2",
        term = "'Kwenzekalani' Amapiano Challenge",
        category = "Dance Challenge",
        hints = listOf(
          "Shrug both shoulders asking 'What is happening?'",
          "Hit synchronized Amapiano footwork with your partner",
          "Pose like Cassper Nyovest, Thobeka Majozi, Kabza De Small, and DJ Maphorisa"
        ),
        culturalEra = "2024",
        culturalTrivia = "GQ South Africa highlighted how Cassper Nyovest, Thobeka Majozi, Kabza De Small, and DJ Maphorisa propelled the challenge.",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "tt_3",
        term = "Traditional Zulu Dance Challenge",
        category = "Cultural Dance",
        hints = listOf(
          "First perform a high-energy ukugida kick raising your leg above your waist",
          "Then immediately switch to the ultra-graceful version barely lifting your feet off the floor",
          "Hold an imaginary shield and ihawu with regal composure"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Bona Magazine highlighted two viral TikTok versions: the energetic high-kick original and the ultra-graceful low-glide version.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "tt_4",
        term = "'My Reaction' Facial Ranking",
        category = "Oct 2026 TikTok Trend",
        hints = listOf(
          "Point to invisible ranks 1 through 5 floating vertically beside your head",
          "Shift your facial expression from pure disgust to ecstatic joy",
          "Keep your lips completely sealed with zero words"
        ),
        culturalEra = "October 2026",
        culturalTrivia = "October 2026 TikTok trend using only facial expressions as the ranking system to judge a niche topic.",
        difficultyTier = 1,
        recommendedMode = "my_reaction",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_5",
        term = "'Getting Robbed, But First...'",
        category = "Oct 2026 TikTok Trend",
        hints = listOf(
          "Stare obsessively at your phone screen in the foreground fixing your hair",
          "Wave casually behind your back for a teammate to 'steal' an imaginary handbag",
          "Keep posing unbothered while chaos happens behind you"
        ),
        culturalEra = "October 2026",
        culturalTrivia = "One-shot comedic TikTok format where a creator stays glued to their phone while a bag gets stolen in the background.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_6",
        term = "'Shoe Kick Transition'",
        category = "TikTok Transition",
        hints = listOf(
          "Balance on one leg in a karate-style side kick",
          "Rest an imaginary sneaker on top of your raised foot",
          "Kick forward into the air and freeze in a brand-new outfit pose"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Creators balance a sneaker, heel, or boot in a karate side-kick and kick into the shoe to transition outfits.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_7",
        term = "'Sorry If I Was a Bit Dramatic Today'",
        category = "TikTok Skit Format",
        hints = listOf(
          "Clasp your hands apologetically and look sheepish for being hangry",
          "Pause as if listening to your friend say 'It is okay, I am used to it'",
          "Gasp in deep offence, roll your eyes, and storm out of the room"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Viral skit where apologizing for being dramatic backfires when the friend replies 'I'm used to it'.",
        difficultyTier = 2,
        recommendedMode = "my_reaction",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_8",
        term = "'Wait, Let Me Wipe the Camera'",
        category = "2026 TikTok Trend",
        hints = listOf(
          "Demonstrate terrible gym lifting form or a messy room",
          "Lean forward, breathe on the imaginary camera lens, and wipe it clean with your sleeve",
          "Step back revealing a bodybuilder pose or spotless masterpiece"
        ),
        culturalEra = "2026",
        culturalTrivia = "Wiping the lens to transition from a rough sketch, bad lifting form, or messy room to a polished result.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_9",
        term = "'Cheer the Camera' Drink Swap",
        category = "2026 TikTok Trend",
        hints = listOf(
          "Clink an imaginary glass toward the camera lens with a friend",
          "Sweep both hands in a wide loose circle across each other",
          "Take a sip from your partner's swapped drink"
        ),
        culturalEra = "2026",
        culturalTrivia = "Two friends cheers the lens and simultaneously sweep their drinks in a circle to swap glasses.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "tt_10",
        term = "'6-7' / '67' Teen Chant",
        category = "Word of the Year",
        hints = listOf(
          "Hold up six fingers on one hand and two on the other (for seven)",
          "Bounce your hands up and down like a teenager chanting in a school corridor",
          "Mime driving and 'bipping right on the highway'"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Crowned Dictionary.com's Word of the Year for 2025 after originating in Skrilla's 'Doot Doot (6 7)'.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_11",
        term = "'Very Demure, Very Mindful'",
        category = "Viral Catchphrase",
        hints = listOf(
          "Smooth down your collar and dab tiny invisible makeup on your cheeks",
          "Fold your hands primly on an office desk with a modest smile",
          "Shake your head at anyone acting loud or flashy"
        ),
        culturalEra = "2024",
        culturalTrivia = "Viral workplace and lifestyle format where creators explain how their behaviour is modest and cutesy.",
        difficultyTier = 1,
        recommendedMode = "my_reaction",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "tt_12",
        term = "'Charger for My Phone, Charger for Me'",
        category = "2026 TikTok Trend",
        hints = listOf(
          "Mime plugging a charging cable into the bottom of your smartphone",
          "Hold up an imaginary kota, iced coffee, or braai chop",
          "Pretend to plug the snack directly into your chest and power up"
        ),
        culturalEra = "2026",
        culturalTrivia = "Showing a phone cable first, then revealing a favourite snack or hobby item as the 'human charger'.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_13",
        term = "The 'Zol Song' (When People Zol)",
        category = "2020 Lockdown Meme",
        hints = listOf(
          "Adjust your spectacles at a ministerial podium with a stern finger",
          "Mime rolling a zol and putting saliva on the paper",
          "Suddenly break into Max Hurrell's infectious electronic beat and sprint after the DJ"
        ),
        culturalEra = "May 2020 Lockdown",
        culturalTrivia = "Immortalized in IOL's Day-50 Lockdown vault alongside the 'Zol Minister running after Zol song maker' meme.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2020 Lockdown"
      ),
      CharadesCard(
        id = "tt_14",
        term = "Lockdown DIY Haircuts & Missing Dishcloths",
        category = "2020 Lockdown Meme",
        hints = listOf(
          "Buzz electric clippers across your own head in a mirror and gasp at the botched chiskop",
          "Scrub your hands vigorously with soap for 20 seconds",
          "Search under every kitchen cushion for the missing dishcloth"
        ),
        culturalEra = "May 2020 Lockdown",
        culturalTrivia = "Two of the top 20 memes documented on Day 50 of South Africa's national lockdown (15 May 2020).",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2020 Lockdown"
      ),
      CharadesCard(
        id = "tt_15",
        term = "Pallbearer's Titanic Dance",
        category = "2020 Lockdown Meme",
        hints = listOf(
          "Hoist an imaginary heavy load onto your shoulder with three teammates",
          "Perform synchronized high-stepping footwork in sunglasses",
          "Hum the dramatic flute melody of Titanic"
        ),
        culturalEra = "May 2020 Lockdown",
        culturalTrivia = "Viral lockdown mashup pairing the dancing pallbearers with the Titanic theme.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2020 Lockdown"
      ),
      CharadesCard(
        id = "tt_16",
        term = "Grace Mondlana's Dog Kai's 1st Birthday",
        category = "Timeline Drama",
        hints = listOf(
          "Set up a luxury balloon arch and birthday cake on a lawn",
          "Place a tiny party hat on an imaginary puppy",
          "Film a glamorous vlog while checking a pricey party bar menu"
        ),
        culturalEra = "25 July 2026",
        culturalTrivia = "Grace Mondlana went viral on 25 July 2026 for hosting a lavish first birthday party for her dog, Kai.",
        difficultyTier = 3,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_17",
        term = "Chidimma Adetshina Pageant & Border Saga",
        category = "Timeline Headlines",
        hints = listOf(
          "Place a beauty pageant sash across your shoulder and wave to the crowd",
          "Switch crowns from South Africa to Nigeria to Miss Universe 1st Runner-Up in Mexico",
          "Mime a dramatic passport inspection at the Lebombo border post"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Withdrew from Miss SA 2024 Top 16, won Miss Universe Nigeria, placed 1st Runner-Up at Miss Universe 2024, and made 2026 Lebombo border headlines.",
        difficultyTier = 3,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tt_18",
        term = "Mia le Roux (Miss SA 2024)",
        category = "Pageant History",
        hints = listOf(
          "Point proudly to a cochlear implant behind your ear",
          "Receive the Miss South Africa 2024 crown with tears of joy",
          "Sign applause with both hands raised and shaking"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Crowned Miss South Africa 2024 and championed disability and cochlear-implant inclusion via the B4i Academy.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      )
    )
  )

  // =========================================================================
  // DECK 3: SOCCER STARS, COACHING LEGENDS & SA SPORTING ICONS (2000-2026)
  // =========================================================================
  val CORE_SPORTS_LEGENDS = Deck(
    id = "core_sports",
    name = "Bafana, UFC, Bokke & Proteas",
    tagLine = "Dricus 8% oxygen, Pitso Mosimane, Temba Bavuma WTC, Benni & Rassie",
    iconEmoji = "🏆",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "sp_dricus",
        term = "Dricus 'Stillknocks' du Plessis (8% Oxygen)",
        category = "UFC Champion",
        hints = listOf(
          "Pinch your nostrils almost completely shut for 'only 8% oxygen intake'",
          "Step into an octagon and lock in a fourth-round face crank submission",
          "Strap the gleaming UFC Middleweight belt around your waist"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "First SA UFC champion (UFC 297 vs Strickland, UFC 305 vs Adesanya, UFC 312 rematch, and July 2026 UFC Fight Night 281 win over Kamaru Usman).",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_pitso",
        term = "Coach Pitso Mosimane",
        category = "Football Legend",
        hints = listOf(
          "Pace the touchline in a tracksuit pointing furiously at a tactical clipboard",
          "Hold up a squad sheet for the November 2026 Bafana AFCON qualifiers against Kenya",
          "Tap your wrist and signal 'Jingles' substitutions"
        ),
        culturalEra = "2000-2026",
        culturalTrivia = "Named his Bafana Bafana squad in November 2026 for back-to-back AFCON qualifiers against Kenya in Johannesburg.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_mokwana",
        term = "Elias Mokwana (Kaizer Chiefs Signing)",
        category = "PSL Transfer",
        hints = listOf(
          "Sprint down the right wing with blistering pace",
          "Mime a stethoscope on your chest for a club medical",
          "Flash the two-finger Kaizer Chiefs 'Amakhosi 4 Life' peace salute"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Bafana winger who played against Egypt at AFCON 2025 and made headlines with his R11 million transfer move to Kaizer Chiefs.",
        difficultyTier = 3,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_sundowns",
        term = "Mamelodi Sundowns ('Shoe Shine & Piano')",
        category = "PSL Dynasty",
        hints = listOf(
          "Mime shining your soccer boot on your knee ('Shoe Shine and Piano')",
          "String together 15 rapid one-touch passes in a circle",
          "Point to the sky with seven fingers ('The Sky is the Limit')"
        ),
        culturalEra = "2000-2026",
        culturalTrivia = "Immortalized in the 2024 Content Creator Awards nominated feature 'Overthinking Football: This Is What Makes Mamelodi Sundowns Successful'.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "sp_senzo",
        term = "Senzo Meyiwa",
        category = "Soccer Icon",
        hints = listOf(
          "Pull on goalkeeper gloves and the captain's armband",
          "Make a diving top-corner penalty save",
          "Cross both wrists above your head in the Orlando Pirates skull-and-crossbones salute"
        ),
        culturalEra = "2000s-2026",
        culturalTrivia = "Beloved Bafana and Pirates captain honoured in Netflix's 'Senzo: Murder of a Soccer Star'.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "sp_banyana",
        term = "Banyana Banyana (WAFCON & World Cup)",
        category = "Women's Football",
        hints = listOf(
          "Form a singing tunnel dance before kickoff",
          "Score a dramatic last-minute winner against Italy in Wellington",
          "Slot home 4-1 quarter-final penalties against Senegal"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Reached the 2023 FIFA Women's World Cup Round of 16 and competed fiercely across the 2025 and 2026 WAFCON tournaments.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_rassie",
        term = "Rassie Erasmus & Eben Etzebeth",
        category = "Springboks Dynasty",
        hints = listOf(
          "Hold up coloured traffic-light LED paddles from the coaching box",
          "Jump three metres in the air to steal a lineout like a towering #4 lock",
          "Lift back-to-back 2024 and 2025 Castle Lager Rugby Championship trophies"
        ),
        culturalEra = "2019-2026",
        culturalTrivia = "Led the Springboks to back-to-back Castle Lager Rugby Championship titles in 2024 and 2025.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_bavuma",
        term = "Temba Bavuma & 2025 WTC Final Win",
        category = "Proteas Cricket",
        hints = listOf(
          "Tuck your bat under your arm and raise your helmet after a match-winning Test innings",
          "Lift the ICC World Test Championship mace after beating Australia",
          "Celebrate a dominant home Test victory in Gqeberha"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "The Men's Proteas ended their wait for a senior world title in June 2025 by defeating Australia in the World Test Championship Final.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_wolvaardt",
        term = "Laura Wolvaardt (#14) & Women's Proteas",
        category = "Proteas Cricket",
        hints = listOf(
          "Lean into a textbook silky cover drive to the boundary",
          "Point to jersey number 14 on your back",
          "Lead your team out at The Kia Oval for the July 2026 T20 World Cup Semi-Final"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Captained the Women's Proteas to the 2026 ICC Women's T20 World Cup Semi-Final at The Kia Oval alongside Marizanne Kapp and Tazmin Brits.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sp_1",
        term = "Benni McCarthy",
        category = "Soccer Icon",
        hints = listOf(
          "Tap your ear and roll your arms for the 'Shibobo' rap verse",
          "Score four goals in a single AFCON match in 1998",
          "Lift the 2004 UEFA Champions League trophy"
        ),
        culturalEra = "1998-2006",
        culturalTrivia = "Bafana Bafana's all-time top scorer and the only South African player to win the UEFA Champions League.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "sp_3",
        term = "2010 World Cup Vuvuzela",
        category = "Sporting Cultural Icon",
        hints = listOf(
          "Hold a metre-long plastic horn to your lips with both hands",
          "Puff your cheeks out and blow a deafening 'BZZZZZZ'",
          "Do the Diski dance at Soccer City"
        ),
        culturalEra = "2010",
        culturalTrivia = "The unmistakable soundtrack of the 2010 FIFA World Cup in South Africa.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "sp_4",
        term = "Lucas Radebe ('The Chief')",
        category = "Soccer Icon",
        hints = listOf(
          "Point to a captain's armband on your bicep",
          "Slide-tackle an attacker cleanly and head the ball away",
          "Salute both Kaizer Chiefs and Leeds United fans"
        ),
        culturalEra = "1994-2005",
        culturalTrivia = "Nelson Mandela famously declared inside Leeds: 'Lucas Radebe is my hero.'",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  // =========================================================================
  // DECK 4: MZANSI MUSIC ERAS: AMAPIANO, 3-STEP, LEKOMPO & KWAITO (2000-2026)
  // =========================================================================
  val CORE_KWAITO = Deck(
    id = "core_kwaito",
    name = "Amapiano, 3-Step, Lekompo & Kwaito",
    tagLine = "Tyla, Tshwala Bam, Dlala Thukzin 3-Step, Limpopo Lekompo & 2000s Kwaito",
    iconEmoji = "🎵",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "mus_tshwala",
        term = "Tshwala Bam (TitoM & Yuppe)",
        category = "Amapiano Anthem",
        hints = listOf(
          "Hum the deep rolling bassline hook",
          "Lock elbows in a line and perform the synchronized shoulder-roll dance",
          "Glide sideways smoothly without spilling your drink"
        ),
        culturalEra = "2024",
        culturalTrivia = "2024 Song of the Year nominee whose synchronized shoulder-roll dance swept global timelines.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_mnike",
        term = "Mnike (Tyler ICU)",
        category = "Sgija Amapiano",
        hints = listOf(
          "Mime a DJ twisting the EQ knobs and slamming the fader down",
          "Jump back as the thunderous log drum shakes the entire room",
          "Sing 'Mnike!' with one hand raised to the ceiling"
        ),
        culturalEra = "2023-2024",
        culturalTrivia = "Heavy log-drum Amapiano anthem by Tyler ICU that dominated festivals from Soweto to London.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_iplan",
        term = "iPLAN (Dlala Thukzin)",
        category = "3-Step Anthem",
        hints = listOf(
          "Tap your temple twice as if hatching a master plan ('iPlan')",
          "Step to a driving, percussive 3-Step Durban groove",
          "Check your phone calendar for the weekend groove"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Flagship track of the 3-Step Afro-house/Amapiano crossover wave championed by Dlala Thukzin.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_uvalo",
        term = "uValo (JAZZWRLD, Thukuthela & Babalwa M)",
        category = "2026 3-Step Smash",
        hints = listOf(
          "Mime rolling down your car window at a traffic light",
          "Turn the car stereo volume knob all the way up",
          "Sway to Babalwa M's vocals over a 3-Step beat"
        ),
        culturalEra = "Late 2025-2026",
        culturalTrivia = "2026 3-Step and Amapiano crossover smash featuring Dlala Thukzin.",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_bengicela",
        term = "Bengicela (MaWhoo, GL Ceejay & Thukuthela)",
        category = "2026 Amapiano / 3-Step",
        hints = listOf(
          "Clasp your hands together in an emotional pleading gesture ('Bengicela' = 'I am asking nicely')",
          "Sway gracefully while belting out MaWhoo's high notes",
          "Drop into a smooth 3-Step footwork sequence"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Chart-topping collaboration featuring JAZZWRLD that bridged vocal Amapiano and 3-Step in 2025-2026.",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_shela",
        term = "Shela (Sam Deep, Nia Pearl & Boohle)",
        category = "2026 Amapiano",
        hints = listOf(
          "Make smooth eye contact across a crowded groove",
          "Lean against an imaginary wall shooting your shot ('ukushela')",
          "Harmonize with a partner like Nia Pearl and Boohle"
        ),
        culturalEra = "Dec 2025-2026",
        culturalTrivia = "Standout early-2026 Amapiano chart-topper highlighted by Music In Africa.",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_amanxeba",
        term = "Amanxeba (Sami Kay, Rox Roberson & Misokuhle)",
        category = "2026 Amapiano",
        hints = listOf(
          "Clutch your chest over your heart for 'wounds of love' ('Amanxeba')",
          "Wipe away an imaginary tear, then immediately break into energetic choreography",
          "Hold up five fingers for 5 million+ views"
        ),
        culturalEra = "Dec 2025-2026",
        culturalTrivia = "Amassed over 5 million YouTube views by February 2026.",
        difficultyTier = 3,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_lekompo",
        term = "Lekompo Wave (Kharishma & Shandesh)",
        category = "Limpopo Sound",
        hints = listOf(
          "Perform high-speed Limpopo street-party footwork",
          "Wave a sweat-towel in the air hyping up a Polokwane crowd",
          "Point to the stage for Kharishma, Shandesh, and Queen Jenny"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Limpopo's fastest-rising musical wave led by Kharishma, Shandesh, DJ Sunco, and Queen Jenny ('Oska Ba Dumelela').",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2025-2026 Current",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_waffles",
        term = "Uncle Waffles & Balcony Mix Africa",
        category = "Global Amapiano DJ",
        hints = listOf(
          "Put on oversized sunglasses and headphones behind twin turntables",
          "Cue a track, throw both hands up, and hit Uncle Waffles' signature booth dance",
          "Point out over an imaginary sunset balcony with Major League DJz"
        ),
        culturalEra = "2021-2026",
        culturalTrivia = "Major League DJz's Balcony Mix Africa and Uncle Waffles took Amapiano DJ sets to global festival mainstages.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "mus_aka",
        term = "AKA: 'Lemons (Lemonade)' & 'Company'",
        category = "SA Hip Hop Tribute",
        hints = listOf(
          "Mime slicing and squeezing fresh lemons into a tall glass",
          "Rap into a microphone with swagger alongside Nasty C",
          "Point your microphone skyward in tribute to the Supa Mega"
        ),
        culturalEra = "2023",
        culturalTrivia = "Dual 2023 Song of the Year nominees from AKA's Mass Country era.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "kw_1",
        term = "Mandoza - Nkalakatha",
        category = "Kwaito Anthem",
        hints = listOf(
          "Roll your shoulders low like a tough Zola township boss",
          "Cross your arms and nod heavily to the iconic 2000 intro",
          "Mime a whole wedding dancefloor rushing to the middle at the first beat"
        ),
        culturalEra = "2000",
        culturalTrivia = "Released in 2000, Mandoza's 'Nkalakatha' united every dancefloor, stadium, and braai across South Africa.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "kw_2",
        term = "Brenda Fassie - Vulindlela",
        category = "Afro-Pop Royalty",
        hints = listOf(
          "Open an imaginary path with both arms ('Vulindlela')",
          "Dance joyfully like a proud mother at her son's traditional wedding",
          "Kick your leg high like MaBrrr on stage"
        ),
        culturalEra = "Late 90s / Early 2000s",
        culturalTrivia = "MaBrrr's timeless wedding anthem remains mandatory at every South African celebration.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "kw_3",
        term = "TKZee - Shibobo",
        category = "Kwaito Classic",
        hints = listOf(
          "Mime kicking a soccer ball right between an opponent's legs (nutmeg)",
          "Sample Europe's 'The Final Countdown' horn fanfare with your hands",
          "Hit Benni McCarthy's fast rap verse"
        ),
        culturalEra = "1998-2002",
        culturalTrivia = "Recorded with Benni McCarthy for the 1998 World Cup, remaining a Kwaito staple through the 2000s.",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      )
    )
  )

  // =========================================================================
  // DECK 5: SOUTH AFRICAN TV SHOWS, SOAPS & MOVIES (2000-2026)
  // =========================================================================
  val CORE_2000S_TV = Deck(
    id = "core_2000s_tv",
    name = "SA TV, Soaps & Streaming (2000-2026)",
    tagLine = "Generations, 7de Laan, The Wife, Shaka iLembe, Spinners & Bad Influencer",
    iconEmoji = "📺",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "tv_thewife",
        term = "The Wife (Hlomu & Mqhele)",
        category = "Showmax Telenovela",
        hints = listOf(
          "Drive a minibus taxi with one hand and count stacks of cash with the other",
          "Wrap a bridal blanket around your shoulders as the new Zulu wife",
          "Gasp at a 5am Thursday episode drop"
        ),
        culturalEra = "2021-2026",
        culturalTrivia = "Accumulated more than 50 million hours watched to become Showmax's most-watched series.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "tv_shaka",
        term = "Shaka iLembe",
        category = "Epic Historical Drama",
        hints = listOf(
          "Stamp your bare feet rhythmically on the ground",
          "Raise a short stabbing spear (iklwa) and cowhide shield toward the sky",
          "Place a royal headband across your brow as Queen Nandi or King Shaka"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "MultiChoice's biggest production ever on Mzansi Magic and DStv Stream, with Season 3 confirmed for 2026.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tv_spinners",
        term = "Spinners (Cape Flats Motorsport)",
        category = "Award-Winning Drama",
        hints = listOf(
          "Spin a BMW steering wheel in tight donuts while smoke fills the air",
          "Climb out of the open car window while the vehicle is still spinning",
          "Accept an international TV trophy beating HBO's Succession"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Won Best Foreign TV Series at the 2024 Shanghai TV Festival Magnolia Awards ahead of its Season 2 global release in 2026.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tv_bad_influencer",
        term = "Bad Influencer (Netflix SA)",
        category = "Crime Comedy-Thriller",
        hints = listOf(
          "Snap a glamorous ring-light selfie holding a luxury designer handbag",
          "Suddenly look over your shoulder for police sirens and stuff counterfeit bags in a boot",
          "High-five your co-star like Jo-Anne Reyneke and Cindy Mahlangu"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Reached #1 in South Africa and #3 on the global Netflix English TV Top 10 in 2025.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tv_ruin_xmas",
        term = "How to Ruin Christmas",
        category = "Festive Netflix Hit",
        hints = listOf(
          "Arrive late at a family holiday dinner holding a glass of champagne",
          "Accidentally knock over the Christmas tree and ruin a wedding",
          "Fan yourself dramatically like Tumi Sello in the middle of family chaos"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Beloved South African Netflix festive franchise alongside How to Ruin Love and How to Ruin Diwali.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "tv_bester",
        term = "Beauty and the Bester / The People vs. VBS",
        category = "SA True Crime Docuseries",
        hints = listOf(
          "Pull a hoodie low over your face while sneaking through an airport",
          "Count disappearing bank vault millions behind a desk",
          "Sit glued to your couch jaws dropped at a South African true-crime documentary"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Blockbuster South African true-crime documentaries alongside Devilsdorp and Steinheist.",
        difficultyTier = 2,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "tv_sarafina_tsotsi",
        term = "Sarafina! & Tsotsi",
        category = "Heritage Cinema",
        hints = listOf(
          "March in a school uniform raising a fist and singing 'Freedom is Coming Tomorrow'",
          "Then cradle a crying baby found in the backseat of a hijacked car in Soweto",
          "Hold up a golden Oscar statuette"
        ),
        culturalEra = "Heritage Classics",
        culturalTrivia = "Cornerstones of South African cinema alongside Yesterday, Mapantsula, and Faith Like Potatoes.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tv_1",
        term = "Generations (Karabo & Tau)",
        category = "8 PM Soapie Classic",
        hints = listOf(
          "Point to your watch at 8:00 PM sharp",
          "Swirl a crystal brandy glass in a New Horizons boardroom like Tau Mogale",
          "Dramatically fire someone in front of Queen Moroka"
        ),
        culturalEra = "2000-2010",
        culturalTrivia = "Every South African household stopped at 20:00 on SABC 1 when the Generations theme song played.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tv_3",
        term = "7de Laan (Oubaas & Hilda)",
        category = "Soapie Classic",
        hints = listOf(
          "Stroke a bushy imaginary moustache like Oubaas",
          "Clasp your hands and gasp 'O гене, Matrone!' like Hilda",
          "Sip a cappuccino at Oppiekoffie in Hillside"
        ),
        culturalEra = "2000-2023",
        culturalTrivia = "SABC 2's beloved Afrikaans soapie made Oubaas, Hilda, and Paula household names.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tv_10",
        term = "Tata Ma Chance (Lotto Ad)",
        category = "2000s Commercial",
        hints = listOf(
          "Watch six numbered ping-pong balls pop out of a spinning machine",
          "Point one finger in the air and dance across the living room",
          "Mime becoming an overnight multi-millionaire on Saturday night"
        ),
        culturalEra = "2000",
        culturalTrivia = "Launched in 2000, 'Tata Ma Chance, Tata Ma Millions' became an instant national catchphrase.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  // =========================================================================
  // DECK 6: DIGITAL CREATORS, COMEDIANS, ALTER-EGOS & PODCASTS (2022-2026)
  // =========================================================================
  val CORE_CREATORS_PODCASTS = Deck(
    id = "deck_creators_podcasts",
    name = "Creators, Alter-Egos & Podcasts",
    tagLine = "Grace Mondlana, Lasizwe, Vafa Naraghi, Noko Mashaba, Coconut Kelz & MacG",
    iconEmoji = "🎙️",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "cr_grace",
        term = "Grace Mondlana",
        category = "2026 Creator of the Year",
        hints = listOf(
          "Mime writing a lesson on a classroom chalkboard as a teacher",
          "Transition smoothly into holding a smartphone for a lifestyle home vlog",
          "Hold TWO trophies from the October 2026 Samsung Galaxy Content Creator Awards"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Won both Samsung Galaxy Creator of the Year and the Trendsetter Award at The Galleria in Sandton in October 2026.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_lasizwe",
        term = "Lasizwe Dambuza (Awkward Dates)",
        category = "2026 YouTube Creator Winner",
        hints = listOf(
          "Sit across a restaurant table holding a wine glass on an awkward blind date",
          "Pull a hilarious shocked side-eye at the camera after a wild question",
          "Put a towel on your head like a dramatic township mother"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Won 2026 YouTube Creator of the Year; creator of hit online series Awkward Dates by Lasizwe.",
        difficultyTier = 1,
        recommendedMode = "my_reaction",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_vafa",
        term = "Vafa Naraghi",
        category = "2026 Double Award Winner",
        hints = listOf(
          "Deliver a panicked line as one character facing left",
          "Jump 180 degrees to face right as a completely different South African character",
          "Hold up both the Best Social Series and Thumbstopping trophies"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Won both the Best Social Series Award and Thumbstopping Award at the 2026 Samsung Galaxy Content Creator Awards.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_noko",
        term = "Noko Mashaba (The Adventures of Noko Mashaba)",
        category = "Animated / Online Comedy",
        hints = listOf(
          "Pull a beanie low over your ears",
          "Passionately pitch a wild township hustler scheme to escape unemployment",
          "Do the 'rekere' dance after winning the 2026 Telkom Best Online Series Award"
        ),
        culturalEra = "2016-2026",
        culturalTrivia = "Won the 2026 Telkom Best Online Series Award for his beloved township comedy chronicles.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_kelz",
        term = "Coconut Kelz & Constantia Mom",
        category = "Satirical Alter-Egos",
        hints = listOf(
          "Flip imaginary long hair over your shoulder and perch sunglasses on your head",
          "Hold an oat-milk iced latte after a morning pilates class",
          "Deliver satirical suburban commentary with exaggerated hand gestures"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "Lesego Tlabi (Coconut Kelz) hosted the 2026 Nominee Announcement; Cassidy Nicholson (Constantia Mom) joined KFM radio in Feb 2026.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "cr_mologadi",
        term = "Mologadi & Tums the Narrator",
        category = "Creator Alter-Egos",
        hints = listOf(
          "Wrap a doek (headscarf) tightly around your head like a strict African mother",
          "Then step aside and mime speaking into a studio mic narrating someone's bad decisions in real time"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Celebrated Content Creator Awards Alter Ego nominees alongside That Mozambican Dude and Neth Thunder.",
        difficultyTier = 3,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "cr_macg_smwx",
        term = "Podcast and Chill With MacG & SMWX",
        category = "SA Podcast Giants",
        hints = listOf(
          "Put on large studio headphones and lean back clapping and laughing at spicy celebrity gossip",
          "Then adjust your glasses and map out serious South African political analysis on a board"
        ),
        culturalEra = "2020-2026",
        culturalTrivia = "MacG (2023 nominee) and Sizwe Mpofu-Walsh's SMWX (2026 Podcast of the Year winner).",
        difficultyTier = 2,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_shandor",
        term = "Shandor Larenty & @Joshwideawake",
        category = "Thumbstopping & Impact Creators",
        hints = listOf(
          "Mime brushing and hugging the mane of a giant full-grown lion",
          "Then hold a clip-on mic walking calmly through a gritty inner-city street interviewing locals"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Shandor Larenty (multiple Thumbstopping nominee) and Joshua Rubin (@Joshwideawake, 2026 Impact Creator winner).",
        difficultyTier = 3,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "cr_sifo",
        term = "Sifo The Cooking Husband & The Lazy Makoti",
        category = "Mzansi Foodie Creators",
        hints = listOf(
          "Tie an apron around your waist with a warm welcoming smile",
          "Chop onions, stir a steaming pot of dombolo, and plate a colourful Sunday seven-colours feast",
          "Hold the dish up to the camera lens"
        ),
        culturalEra = "2022-2026",
        culturalTrivia = "Beloved Foodie Award nominees bringing South African home cooking to millions.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "cr_2026_winners",
        term = "@Lindosasmr, Miss Angler & Nabilah Kariem",
        category = "2026 Specialist Winners",
        hints = listOf(
          "Tap your fingernails gently against a sensitive microphone for ASMR",
          "Point to a biology diagram for Matric exam prep",
          "Lift the heavy lid off a gleaming Le Creuset pot"
        ),
        culturalEra = "October 2026",
        culturalTrivia = "2026 Samsung Galaxy Content Creator Awards winners in Wildcard (@Lindosasmr), Expert Voices (Miss Angler), and Brand Collab (Nabilah Kariem).",
        difficultyTier = 3,
        recommendedMode = "mzansi_30_seconds",
        eraTag = "2025-2026 Current"
      )
    )
  )

  // =========================================================================
  // DECK 7: MZANSI SLANG, SASL GESTURES & EVERYDAY CULTURAL TROPES (2000-2026)
  // =========================================================================
  val CORE_SLANG = Deck(
    id = "core_slang",
    name = "Mzansi Slang, SASL & Tropes",
    tagLine = "Now Now, Robots, Nikhona?, Is Not Make Sure, Bone Thug, SASL Vibe & Bunny Chow",
    iconEmoji = "🗣️",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "sl_1",
        term = "'Now' vs 'Just Now' vs 'Now Now'",
        category = "SA Time Tiers",
        hints = listOf(
          "Tap your wrist once while yawning and stretching on the couch ('Just now' = hours later)",
          "Then tap your wrist twice urgently while grabbing your car keys ('Now now' = immediately)"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "South Africa's famous three-tier temporal system that confuses every visitor.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "sl_robots",
        term = "Robots (Traffic Lights)",
        category = "SA Lexicon",
        hints = listOf(
          "Walk like a stiff mechanical robot",
          "Then stand at an intersection holding up three fingers for Red, Amber, and Green",
          "Wave cars through when the light turns green"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Derived from the original English description of automated 'robot traffic policemen' in the 1930s.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "sl_nikhona",
        term = "Nikhona?",
        category = "Comment Section Call",
        hints = listOf(
          "Cup a hand over your brow peering into an imaginary international comment section",
          "Wave a miniature South African flag",
          "Motion to your fellow countrymen asking 'Are y'all here?'"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Nguni roll-call meaning 'Are y'all here?' used whenever South Africans assemble in global comment sections.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_not_make_sure",
        term = "Is Not Make Sure",
        category = "2025 Township / TikTok Slang",
        hints = listOf(
          "Watch someone perform an imaginary stunt or outfit reveal",
          "Shake your head slowly with a unimpressed frown",
          "Wave both hands flat in dismissal ('That is not convincing at all')"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "2025 township and TikTok slang meaning something is unconvincing, questionable, or delusional.",
        difficultyTier = 2,
        recommendedMode = "my_reaction",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_bone_thug",
        term = "Bone Thug (Sangoma)",
        category = "Youth Slang",
        hints = listOf(
          "Do a 'Skrr Skrr' hip-hop steering-wheel swerve",
          "Then kneel respectfully on a reed mat, shake a woven basket, and cast divination bones",
          "Clap twice and say 'Thokoza!'"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "Playful youth slang term for a Sangoma (traditional healer) documented on Africtionary.",
        difficultyTier = 3,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_dyan",
        term = "Dyan ('Ayicatchi iDyan')",
        category = "isiXhosa Slang",
        hints = listOf(
          "Stand with arms folded like a stoic, unshakeable grootman",
          "Keep a completely stone-faced expression while someone tries to flirt with you",
          "Brush your shoulder off showing zero romantic feelings"
        ),
        culturalEra = "2024-2026",
        culturalTrivia = "isiXhosa slang for a stoic man ('Ayicatchi idyan' = a dyan never catches feelings).",
        difficultyTier = 2,
        recommendedMode = "my_reaction",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_iqaza_voke",
        term = "Iqaza & Voke",
        category = "2025 Sneaker & Teasing Slang",
        hints = listOf(
          "Point down to your feet and polish an imaginary fresh pair of Sportscene sneakers ('Iqaza')",
          "Then point and laugh playfully at a friend's outfit until they cover their face ('Please don't voke me')"
        ),
        culturalEra = "2025-2026",
        culturalTrivia = "Iqaza = fresh sneakers; Voke = 2025 slang meaning to tease or roast someone.",
        difficultyTier = 3,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_sasl",
        term = "SASL 'Vibe', 'Lit', 'Selfie' & 'Sharp Sharp'",
        category = "Official 12th Language",
        hints = listOf(
          "Flick your wrist loosely like an Amapiano dancer ('Vibe')",
          "Spark your fingers outward ('Lit') and tilt a thumb-and-pinky phone to your face ('Selfie')",
          "Finish with a rhythmic doubled-up thumbs-up ('Sharp sharp')"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Youth South African Sign Language (SASL) slang signs highlighted during the 2023 SASL Bill milestone.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "sl_bunny_braai",
        term = "Bunny Chow & Shisa Nyama Braai",
        category = "Everyday Mzansi Icons",
        hints = listOf(
          "Hop like a rabbit, hollow out a half-loaf of bread, and scoop curry inside",
          "Then hold braai tongs in one hand and a cold drink in the other over hot coals",
          "Hit a two-step Amapiano dance while turning the boerewors"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Durban's famous hollowed-out curry loaf and the universal South African Shisa Nyama / Braai.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "sl_ngiyakhathala",
        term = "Eish, Shem, Thula & Ngiyakhathala",
        category = "4 Iconic SA Reactions",
        hints = listOf(
          "Slap your forehead in disbelief ('Eish')",
          "Place both hands on your heart in sympathy ('Shem') and press a finger to your lips ('Thula')",
          "Finally slump completely onto a chair exhausted ('Ngiyakhathala' = I am tired, I no longer care)"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "The four universal emotional reactions of everyday South African life.",
        difficultyTier = 1,
        recommendedMode = "my_reaction",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "sl_situationship",
        term = "Situationship (Umjolo)",
        category = "Modern Dating",
        hints = listOf(
          "Walk holding hands romantically with someone in private",
          "Suddenly drop their hand and pretend you barely know them when friends walk past",
          "Shrug when asked 'What are we?'"
        ),
        culturalEra = "2023-2026",
        culturalTrivia = "Central theme of the four-part Netflix South Africa Umjolo movie series.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2025-2026 Current"
      ),
      CharadesCard(
        id = "sl_10",
        term = "Izikhothane",
        category = "Township Subculture",
        hints = listOf(
          "Show off five layers of colourful Italian designer shirts",
          "Pour a carton of Custard Ultramel onto the ground in a dance battle",
          "Point to price tags left intentionally on your shoes"
        ),
        culturalEra = "Mid-Late 2000s",
        culturalTrivia = "East Rand township crew battles defined by flamboyant fashion and Ultramel Custard stunts.",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  // =========================================================================
  // SPAZA & TUCKSHOP CLASSICS (FREE TIER HERITAGE DECK)
  // =========================================================================
  val CORE_TUCKSHOP = Deck(
    id = "core_tuckshop",
    name = "Spaza & Tuckshop Classics",
    tagLine = "Chappies trivia, Spoekies,Quarter-loaf Kota, NikNaks & Stoney",
    iconEmoji = "🍬",
    isFreeTier = true,
    cards = listOf(
      CharadesCard(
        id = "tk_1",
        term = "Chappies Bubblegum ('Did You Know?')",
        category = "Tuckshop Legend",
        hints = listOf(
          "Unwrap a tiny striped square of bubblegum",
          "Squint closely at the tiny wax wrapper to read 'Did You Know?' facts",
          "Blow a giant pink bubble until it pops on your nose"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Both currency for 50c change at the Spaza shop and an encyclopedia on the inside wrapper.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tk_2",
        term = "Quarter-Loaf Kota (Spatlo)",
        category = "Township Feast",
        hints = listOf(
          "Slice a loaf of white bread into four quarters and hollow out the middle",
          "Stack slap chips, polony, Vienna, Russian, cheese, and atchar sky-high",
          "Open your jaw as wide as humanly possible to take a bite"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Known as a Kota in Soweto and Spatlo in Pretoria, the ultimate after-school feast.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tk_3",
        term = "NikNaks (Cheesy Fingers)",
        category = "Maize Snack",
        hints = listOf(
          "Tear open a packet and tilt the last crumbs straight into your mouth",
          "Show off bright orange cheese-dust stained fingertips",
          "Lick each finger loudly one by one"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Featuring Nik the Nak on the packet, leaving unmistakable orange dust on every school shirt.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tk_4",
        term = "Stoney Ginger Beer Throat Burn",
        category = "Iconic Cooldrink",
        hints = listOf(
          "Crack open a brown bottle and take a big gulp",
          "Grab your throat, cough dramatically, and stomp your foot as the fiery ginger hits",
          "Wipe a tear from your eye and give a thumbs-up"
        ),
        culturalEra = "2000s",
        culturalTrivia = "Famous for its 'Bheka mina ngedwa' commercials and unmistakable ginger bite.",
        difficultyTier = 1,
        recommendedMode = "my_reaction",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "tk_5",
        term = "10c Ice Lolly (Bompie / Ice-Block)",
        category = "Summer Treat",
        hints = listOf(
          "Bite the bottom corner off a long narrow plastic tube",
          "Squeeze frozen neon-red or green syrup up from the bottom",
          "Shiver from sudden brain-freeze"
        ),
        culturalEra = "2000s",
        culturalTrivia = "Sold from neighbourhood deep-freezers across South Africa on 35-degree afternoons.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "tk_8",
        term = "Vetkoek & Mince (Amagwinya)",
        category = "Morning Staple",
        hints = listOf(
          "Drop balls of dough into bubbling hot oil",
          "Slice the golden fried bun open and spoon savoury curry mince or polony inside",
          "Blow on it because it is piping hot at the taxi rank"
        ),
        culturalEra = "All Eras",
        culturalTrivia = "Whether with mince, polony, or cheese, fresh amagwinya power every morning commute.",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  // =========================================================================
  // EXPANSION PACKS (UNLOCKABLE WITH COINS OR ZAR CHECKOUT)
  // =========================================================================
  val EXP_PANTSULA_LIFE = Deck(
    id = "exp_pantsula",
    name = "Pantsula & Zola 7 Era",
    tagLine = "Dickies chinos, Converse All-Stars, Spaza whistles & township style",
    iconEmoji = "👟",
    isFreeTier = false,
    priceCoins = 100L,
    cards = listOf(
      CharadesCard(
        id = "ep_1",
        term = "Converse All-Stars & Dickies",
        category = "Pantsula Uniform",
        hints = listOf(
          "Lace up high-top canvas sneakers and iron razor-sharp creases into khaki trousers",
          "Pull a spotless white Spoti bucket hat low over your eyes",
          "Execute lightning-fast ankle-twisting footwork"
        ),
        culturalEra = "Early 2000s",
        difficultyTier = 2,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "ep_2",
        term = "Taxi Hand Signals",
        category = "Commuter Skill",
        hints = listOf(
          "Stand on the pavement peering down the road",
          "Point one index finger straight up for CBD / Town, or point down for Local",
          "Count passengers' change in the front seat ('Sho't left!')"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "ep_3",
        term = "Nokia 3310 Snake II",
        category = "2000s Tech",
        hints = listOf(
          "Hold an indestructible brick phone and mash the 2-4-6-8 keypad buttons",
          "Drop the phone on the concrete floor and mime the floor cracking instead of the phone",
          "Compose a monophonic Kwaito ringtone"
        ),
        culturalEra = "2000-2004",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "ep_4",
        term = "Sunday Seven Colours Plate",
        category = "Culinary Tradition",
        hints = listOf(
          "Spoon rice, samp, beetroot, pumpkin, creamed spinach, potato salad, and roast chicken onto one plate",
          "Try to stop the beetroot juice from touching the yellow rice",
          "Fall asleep on the couch right after eating"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "ep_5",
        term = "Stoep Cobra Polish",
        category = "Saturday Chores",
        hints = listOf(
          "Get down on your knees with a tin of red wax polish and a cloth",
          "Buff the front veranda until it shines like a mirror",
          "Chase away anyone trying to step on the wet stoep with muddy shoes"
        ),
        culturalEra = "2000s",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  val EXP_SCHOOL_DAYS = Deck(
    id = "exp_school_days",
    name = "2000s Mzansi School Days",
    tagLine = "Bata Toughees, Brown paper book covers, Space Cases & Inter-High Athletics",
    iconEmoji = "🎒",
    isFreeTier = false,
    priceCoins = 120L,
    cards = listOf(
      CharadesCard(
        id = "es_1",
        term = "Bata Toughees School Shoes",
        category = "Back to School",
        hints = listOf(
          "Rub black Kiwi shoe polish vigorously with a horsehair brush",
          "Stomp your rock-hard new leather school shoes in the corridor",
          "Compare the tyre-tread soles with your classmates"
        ),
        culturalEra = "2000s",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "es_2",
        term = "Covering Books in Brown Paper & Plastic",
        category = "January Ritual",
        hints = listOf(
          "Unroll giant sheets of brown kraft paper and sticky plastic on the dining table",
          "Snip the corners with scissors while your parent yells about air bubbles",
          "Press a 'Name & Grade' sticker onto the front cover"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "es_3",
        term = "Plastic Space Case & Monami Crayons",
        category = "Stationery Flex",
        hints = listOf(
          "Unclip a hard plastic pencil box on your school desk",
          "Show off a brand-new set of 24 twistable Monami crayons",
          "Guard the gold and silver crayons with your life"
        ),
        culturalEra = "2000s",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "es_4",
        term = "Inter-High Athletics Gwijo Stands",
        category = "School Spirit",
        hints = listOf(
          "Sit in packed stadium bleachers in full school blazer",
          "Stomp and clap in unison leading a booming school war-cry",
          "Cheer wildly as the 100m relay baton is passed"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "hit_the_beat",
        eraTag = "2000s-2010s Heritage"
      )
    )
  )

  val EXP_ROAD_TRIP = Deck(
    id = "exp_road_trip",
    name = "December Padkos & Dezemba Groove",
    tagLine = "Tinfoil boiled eggs, Ultra City stops, N1/N3 traffic & Festive 3-Step anthems",
    iconEmoji = "🚗",
    isFreeTier = false,
    priceCoins = 120L,
    cards = listOf(
      CharadesCard(
        id = "er_1",
        term = "Tinfoil Boiled Eggs & Fried Chicken Padkos",
        category = "Road Trip Classic",
        hints = listOf(
          "Open an ice-cold coolerbox wedged in the backseat before hitting the first tollgate",
          "Unwrap crinkly silver tinfoil revealing hard-boiled eggs and cold drumsticks",
          "Wave the car windows open when the egg smell fills the cab"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "er_2",
        term = "Engen 1-Stop / Wimpy Ultra City",
        category = "Highway Pitstop",
        hints = listOf(
          "Step out of the car after 4 hours and stretch your stiff lower back",
          "Order a Wimpy coffee and farmhouse breakfast",
          "Wipe the windscreen while the petrol attendant checks oil and water"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      ),
      CharadesCard(
        id = "er_3",
        term = "Blue-Light Brigade & Speed Trap Warning",
        category = "SA Highway Habit",
        hints = listOf(
          "Spot a traffic officer hiding behind a bush with a speed camera",
          "Flash your high-beam headlights twice at oncoming cars to warn them",
          "Receive a grateful four-way hazard-light blink in return"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "er_4",
        term = "Venter Trailer Packed to the Roof",
        category = "Dezemba Migration",
        hints = listOf(
          "Hitch a small white luggage trailer to the back of a sedan",
          "Tie down suitcases, a coolerbox, and groceries with bungee cords",
          "Pat the lid twice and say 'That is going nowhere'"
        ),
        culturalEra = "2000s",
        difficultyTier = 2,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage",
        isFestiveGroove = true
      )
    )
  )

  val EXP_MEMES = Deck(
    id = "exp_memes",
    name = "Iconic Mzansi Viral Moments",
    tagLine = "Loadshedding Stage 6, Cremora on top, Checkers Sixty60 & Woolies Rotisserie",
    iconEmoji = "⚡",
    isFreeTier = false,
    priceCoins = 150L,
    cards = listOf(
      CharadesCard(
        id = "em_1",
        term = "Stage 6 Loadshedding at 19:59",
        category = "National Crisis Comedy",
        hints = listOf(
          "Stir a pot on the stove while watching the 8 PM news",
          "Freeze in pitch darkness as the electricity cuts out with a loud sigh ('Eish!')",
          "Wave your phone flashlight around looking for the rechargeable bulb and inverter"
        ),
        culturalEra = "2008-2026",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "em_2",
        term = "Checkers Sixty60 Delivery Bike",
        category = "Modern SA Icon",
        hints = listOf(
          "Zip around corners on a teal-and-black delivery scooter",
          "Carry six heavy teal grocery bags in one trip to the front gate",
          "Press the intercom buzzer and speed off to the next order"
        ),
        culturalEra = "2020-2026",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom"
      ),
      CharadesCard(
        id = "em_3",
        term = "Cremora ('It's Not Inside, It's On Top!')",
        category = "TV Ad Legend",
        hints = listOf(
          "Open a fridge door and peer inside every shelf looking for coffee creamer",
          "Tap the top of the fridge and shout with realization",
          "Spoon powder into your mug with a satisfied grin"
        ),
        culturalEra = "Classic SA Ad",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2000s-2010s Heritage"
      ),
      CharadesCard(
        id = "em_4",
        term = "Woolies Rotisserie Chicken & Pap",
        category = "Payday Treat",
        hints = listOf(
          "Walk through sliding doors carrying a shiny black Woolworths shopping bag",
          "Place a warm golden roast chicken in the middle of the dinner table",
          "Guard the crispy skin from everyone else"
        ),
        culturalEra = "All Eras",
        difficultyTier = 1,
        recommendedMode = "act_it_out",
        eraTag = "2021-2024 Piano Boom",
        isFestiveGroove = true
      )
    )
  )

  val ALL_DECKS = listOf(
    CORE_TRENDING_APPS,
    CORE_TIKTOK_LOCKDOWN,
    CORE_SPORTS_LEGENDS,
    CORE_KWAITO,
    CORE_2000S_TV,
    CORE_CREATORS_PODCASTS,
    CORE_SLANG,
    CORE_TUCKSHOP,
    EXP_PANTSULA_LIFE,
    EXP_SCHOOL_DAYS,
    EXP_ROAD_TRIP,
    EXP_MEMES
  )

  val FREE_DECK_IDS = ALL_DECKS.filter { it.isFreeTier }.map { it.id }

  fun buildFestiveGrooveDeck(): Deck {
    val festiveCards = ALL_DECKS.flatMap { it.cards }.filter { it.isFestiveGroove }.distinctBy { it.id }
    return Deck(
      id = "deck_festive_groove",
      name = "Dezemba Festive Groove Pack",
      tagLine = "Amapiano, 3-Step (uValo, Bengicela, iPLAN), Lekompo, Braai & How to Ruin Christmas",
      iconEmoji = "🎉",
      isFreeTier = true,
      cards = festiveCards
    )
  }
}
