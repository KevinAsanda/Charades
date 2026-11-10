package com.example.data.model

data class CharadesCard(
  val id: String,
  val term: String,
  val category: String,
  val hints: List<String>,
  val culturalEra: String = "Early 2000s",
  val culturalTrivia: String = ""
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
  val CORE_KWAITO = Deck(
    id = "core_kwaito",
    name = "Kwaito & 2000s Grooves",
    tagLine = "TKZee, Mandoza, Brenda Fassie and pantsula township anthems",
    iconEmoji = "🎵",
    isFreeTier = true,
    priceCoins = 0,
    cards = listOf(
      CharadesCard("k1", "Mandoza - Nkalakatha", "Music & Dance", listOf("Crossed arms dance", "Shirt pull", "Massive 2000 crossover hit"), "2000", "Released in 2000, became the anthem across all South African radio stations."),
      CharadesCard("k2", "TKZee - Shibobo", "Music & Dance", listOf("Soccer dribble", "Benni McCarthy feature", "1998/2000s banger"), "Late 90s/2000s", "Collaborated with Benni McCarthy ahead of the 1998 World Cup."),
      CharadesCard("k3", "Brenda Fassie - Vul'indlela", "Music & Dance", listOf("Wedding celebration dance", "Traditional high kicks", "MaBrrr energy"), "Late 90s/2000s", "The ultimate South African wedding celebration track."),
      CharadesCard("k4", "Mapaputsi - Izinja", "Music & Dance", listOf("Barking like a dog", "Pantsula footwork", "Street swagger"), "2001", "Won Best Kwaito Artist at the Metro FM Music Awards."),
      CharadesCard("k5", "Arthur Mafokate - Oyi Oyi", "Music & Dance", listOf("King of Kwaito", "High energy hand waves", "999 Music boss"), "Early 2000s", "One of Arthur's most energetic party tracks."),
      CharadesCard("k6", "Boom Shaka - Thobela", "Music & Dance", listOf("Lebo Mathosa braids flip", "Sync female dance routine", "Kwaito pioneers"), "2000s", "Lebo Mathosa and Thembi Seete defined 2000s township youth fashion."),
      CharadesCard("k7", "Zola 7 - Ghetto Scandalous", "Music & Dance", listOf("Hope for the youth", "Car rolling gesture", "Zola TV show energy"), "2002", "Zola 7 inspired millions of South Africans with his music and TV community work."),
      CharadesCard("k8", "Pitch Black Afro - Matofotofo", "Music & Dance", listOf("Giant Afro hair pat", "Rhyming fast", "Matofotofo bounce"), "2004", "His debut album sold over 50,000 copies in South Africa."),
      CharadesCard("k9", "Malaika - Destiny", "Music & Dance", listOf("Afro-pop slow dance", "Harmonizing vocals", "Tshedi and Bongani"), "2003", "Dominated South African airwaves in 2003-2004."),
      CharadesCard("k10", "Brown Dash - Phansi Komthunzi Welanga", "Music & Dance", listOf("Holding dark sunglasses", "Smooth kwaito walk", "TS Records"), "2004", "Brown Dash became a legend of smooth kwaito storytelling."),
      CharadesCard("k11", "Brickz - Sweety MaBaby", "Music & Dance", listOf("Bouncing knees", "Catchy chorus", "Street slang delivery"), "2005", "One of the mid-2000s biggest dancefloor hits."),
      CharadesCard("k12", "Trompies - Sigiya Ngengoma", "Music & Dance", listOf("Vintage spotty caps", "Township uncle swing", "Kalawa Jazmee founders"), "Early 2000s", "Pioneered the vibrant township retro pantsula look.")
    )
  )

  val CORE_TV_SOAPIES = Deck(
    id = "core_2000s_tv",
    name = "2000s TV & Nostalgic Jingles",
    tagLine = "Generations, Yizo Yizo, Takalani Sesame & classic TV ads",
    iconEmoji = "📺",
    isFreeTier = true,
    priceCoins = 0,
    cards = listOf(
      CharadesCard("tv1", "Queen Moroka (Generations)", "TV & Soapies", listOf("Dramatic gasping", "High society diva", "Iconic hairstyle"), "Early 2000s", "Sophie Ndaba's Queen Moroka was the undisputed queen of 8pm SABC 1."),
      CharadesCard("tv2", "Chester in Yizo Yizo", "TV & Drama", listOf("Tough gangster glare", "Leather jacket", "School corridor standoff"), "2001", "Dumiso Veerem's Chester was an unforgettable dramatic character."),
      CharadesCard("tv3", "Lotto: Tata Ma Chance, Tata Ma Millions", "TV Ads & Jingles", listOf("Jumping with ticket", "Singing the lottery jingle", "Saturday night draw"), "2000", "Launched in South Africa in March 2000, turning Saturday nights into a national ritual."),
      CharadesCard("tv4", "Takalani Sesame - Kami & Moshe", "Kids TV", listOf("Big yellow puppet", "Gentle hugs", "Educational alphabet dance"), "2000s", "Brought joy, love, and education to millions of South African kids."),
      CharadesCard("tv5", "Cremora: It's not inside, it's on top!", "Classic TV Ad", listOf("Searching the fridge", "Pointing to the cupboard roof", "Husband and wife argument"), "Late 90s/2000s", "One of the most famous South African television commercials ever aired."),
      CharadesCard("tv6", "Vodacom: Yebo Gogo Baboon", "Classic TV Ad", listOf("Holding red phone", "Imitating wise baboon", "Saying Yebo Gogo"), "Early 2000s", "Bankole Omotoso starred in this legendary telecommunications series."),
      CharadesCard("tv7", "Barker Haines (Isidingo)", "TV & Soapies", listOf("Evil mastermind grin", "Whiskey glass swirl", "Horizon Deep boss"), "2000s", "Robert Whitehead portrayed the villain everyone loved to hate."),
      CharadesCard("tv8", "Jam Alley - Vinolia Mashego (V-Mash)", "TV Show", listOf("Electric energy", "Calling out contestants", "Friday night 6:30 PM vibe"), "Early 2000s", "V-Mash set the standard for high-octane South African TV presenting."),
      CharadesCard("tv9", "SABC 1: Simunye - We Are One", "TV Nostalgia", listOf("Holding index finger in air", "Singing the choir jingle", "Prime time logo"), "Early 2000s", "The legendary station identity of SABC 1 before Mzansi Fo Sho."),
      CharadesCard("tv10", "Castrol GTX: Boet and Swaer", "Classic TV Ad", listOf("Kalahari desert heat", "Car broken down", "Waving flies away"), "2000s", "A classic South African humour ad beloved across generations.")
    )
  )

  val CORE_SLANG = Deck(
    id = "core_slang",
    name = "Pure SA Slang & Phrases",
    tagLine = "Eish, Hawu, Lekker, Sharp Fede and everyday Mzansi talk",
    iconEmoji = "🗣️",
    isFreeTier = true,
    priceCoins = 0,
    cards = listOf(
      CharadesCard("s1", "Eish!", "Local Slang", listOf("Head in hands", "Expressing dismay", "Universal sigh of life"), "All eras", "The undisputed universal South African expression of shock, exhaustion, or sympathy."),
      CharadesCard("s2", "Sharp Fede!", "Local Slang", listOf("Double thumbs up", "Handshake with thumb click", "Greeting your crew"), "Township Slang", "Tsotsitaal greeting meaning 'Everything is good and cool.'"),
      CharadesCard("s3", "Lekker Braai", "Local Slang", listOf("Flipping chops on coals", "Tasting meat", "Cold drink in hand"), "SA Culture", "A braai is more than a barbecue; it's a national cultural gathering."),
      CharadesCard("s4", "Now-now vs Just-now", "Local Slang", listOf("Tapping imaginary watch", "Shrugging confusion", "Waiting indefinitely"), "SA Slang", "In SA: 'Now-now' is sooner than 'Just-now', which might mean tomorrow or never!"),
      CharadesCard("s5", "Hosh My Chommie!", "Local Slang", listOf("Cape Flats salute", "Big smile and hug", "Greeting your best mate"), "Cape Culture", "Friendly Cape Town slang greeting for a dear friend."),
      CharadesCard("s6", "Grootman", "Local Slang", listOf("Walking with respect", "Buying drinks for the table", "Township elder statesman"), "Township Culture", "Respectful term for a respected older guy or mentor in the community."),
      CharadesCard("s7", "Skedonk", "Local Slang", listOf("Driving rickety car", "Engine sputtering", "Car shaking violently"), "SA Slang", "An old, beat-up car that still somehow runs on faith and prayers."),
      CharadesCard("s8", "Voetstoots", "Local Slang", listOf("Shrugging shoulders", "Signing 'as is' contract", "No returns accepted"), "Afrikaans/SA Law", "Buying something strictly 'as is' without warranties."),
      CharadesCard("s9", "Izikhothane Custard Battle", "Street Culture", listOf("Pouring imaginary custard", "Showing off expensive Italian shoes", "Tearing clothes in battle"), "2000s Youth", "Township youth dance and fashion subculture famous for flamboyant swagger."),
      CharadesCard("s10", "Hayibo!", "Local Slang", listOf("Hands on hips", "Dramatic head shake", "Expressing disbelief"), "Zulu/Xhosa/SA", "Expression of pure disbelief or outrage.")
    )
  )

  val CORE_TUCKSHOP = Deck(
    id = "core_tuckshop",
    name = "Tuckshop & Nostalgia Snacks",
    tagLine = "Chappies, Kotas, Stoney Ginger Beer & Ouma Rusks",
    iconEmoji = "🍔",
    isFreeTier = true,
    priceCoins = 0,
    cards = listOf(
      CharadesCard("f1", "Chappies: Did You Know?", "Tuckshop Nostalgia", listOf("Blowing bubblegum", "Unwrapping wrapper", "Reading random trivia"), "All eras", "Chappies bubblegum wrappers educated generations of South Africans."),
      CharadesCard("f2", "Spatlo / Kota with Russian & Atchar", "Street Food", listOf("Carving quarter loaf of bread", "Stuffing with chips and viennas", "Eating with two hands"), "Township Classic", "The ultimate hearty South African street food meal."),
      CharadesCard("f3", "Stoney Ginger Beer 'Extra Kwaai'", "Drinks", listOf("Coughing from fiery ginger fizz", "Drinking from glass bottle", "Fiery spicy kick"), "SA Classic", "Famous for its strong fiery ginger burn."),
      CharadesCard("f4", "Ouma Rusks in Five Roses Tea", "Snacks", listOf("Dipping hard rusk into hot tea", "Catching drips before eating", "Morning family ritual"), "SA Classic", "Dunking an Ouma rusk requires supreme timing and technique."),
      CharadesCard("f5", "Bunny Chow with Extra Gravy", "Street Food", listOf("Hollowed bread loaf", "Durban curry spice", "Licking spicy fingers"), "Durban Classic", "Durban's world-famous hollowed loaf filled with delicious curry."),
      CharadesCard("f6", "Steri Stumpie Flavored Milk", "Drinks", listOf("Shaking glass bottle", "Drinking chocolate or strawberry", "Primary school lunchbox"), "2000s Nostalgia", "The iconic flavored milk treat found in every school tuckshop."),
      CharadesCard("f7", "Zoo Biscuits with Animal Icing", "Snacks", listOf("Examining icing elephant or lion", "Biting off the icing first", "Childhood birthday party"), "Childhood Treat", "Sweet crunchy biscuits topped with colorful sugar animals."),
      CharadesCard("f8", "Simba Chippies: Mrs Ball's Chutney", "Snacks", listOf("Licking spiced fingers", "Sharing bag of crisps", "Crunching loudly"), "SA Snacks", "The distinct sweet and tangy South African chutney flavor.")
    )
  )

  val CORE_SPORTS = Deck(
    id = "core_sports",
    name = "Iconic Mzansi Sports & Moments",
    tagLine = "2010 World Cup Vuvuzela, Bafana 96 & Springbok heroics",
    iconEmoji = "⚽",
    isFreeTier = true,
    priceCoins = 0,
    cards = listOf(
      CharadesCard("sp1", "Blowing the Vuvuzela with Full Lungs", "2010 World Cup", listOf("Puffing cheeks out", "Loud buzzing horn sound", "Stands trembling"), "2010", "The vuvuzela became the global acoustic symbol of the 2010 FIFA World Cup in South Africa."),
      CharadesCard("sp2", "Siphiwe Tshabalala World Cup Opening Goal", "2010 World Cup", listOf("Left foot screamer into top corner", "Iconic team celebration line dance", "Peter Drury commentary"), "2010", "Goal Bafana Bafana, goal for South Africa, goal for all Africa!"),
      CharadesCard("sp3", "Lucas Radebe 'The Chief'", "Football Legend", listOf("Towering defensive header", "Wearing Leeds United armband", "Bafana captain salute"), "90s/2000s", "Beloved captain of both Leeds United and Bafana Bafana."),
      CharadesCard("sp4", "Doctor Khumalo No-Look Pass", "Football Legend", listOf("Looking left and passing right", "Midfield maestro swagger", "Kaizer Chiefs jersey 15"), "90s/Early 2000s", "16V Doctor Khumalo was celebrated for breathtaking dribbling and vision."),
      CharadesCard("sp5", "1995 Rugby World Cup: Mandela #6 Jersey", "Historic Moments", listOf("Holding golden trophy", "Presidential wave", "Uniting a nation"), "1995", "President Nelson Mandela wearing Francois Pienaar's number 6 jersey."),
      CharadesCard("sp6", "Kaizer Chiefs vs Orlando Pirates Soweto Derby", "Football Rivalry", listOf("Black & Gold vs Black & White", "FNB Stadium roar", "Divided living room fans"), "All eras", "One of the greatest and fiercest football derbies in world sport.")
    )
  )

  // EXPANSION PACKS (Available in Store)
  val EXP_PANTSULA_LIFE = Deck(
    id = "exp_pantsula",
    name = "Township Pantsula & Kasi Life",
    tagLine = "Converse All Stars, taxi signs, spotty hats & street swagger",
    iconEmoji = "👟",
    isFreeTier = false,
    priceCoins = 100,
    cards = listOf(
      CharadesCard("p1", "Pantsula High-Kick Quickstep", "Street Dance", listOf("Super fast tapping feet", "High sudden kick", "Touching the floor"), "2000s", "Pantsula originated in South African townships during the 1950s-1980s and exploded in kwaito videos."),
      CharadesCard("p2", "Taxi Driver Hand Signs", "Kasi Life", listOf("Pointing index finger up (Joburg Bree)", "Pointing finger down (Local)", "Flat hand wave"), "SA Culture", "A sophisticated non-verbal language for hailing minibus taxis in SA."),
      CharadesCard("p3", "Car Guard Whistle and Guide", "Kasi Life", listOf("High visibility neon bib", "Blowing whistle", "Waving backwards into parking"), "SA Culture", "Every South African parking lot has a dedicated guard guiding you out."),
      CharadesCard("p4", "Corner Shisanyama Braai Master", "Township Culture", listOf("Flipping braai meat with giant tongs", "Fan coals with cardboard", "Sprinkling braai salt"), "Kasi Vibe", "The master who cooks the tastiest meat while good music plays."),
      CharadesCard("p5", "Spinning 325i BMW Gusheshe", "Motorsport/Kasi", listOf("Holding steering wheel in donuts", "Smoke billowing from tyres", "Crowd cheering"), "Kasi Culture", "The legendary BMW 325i 'Gusheshe' burnout and spinning culture.")
    )
  )

  val EXP_SCHOOL_DAYS = Deck(
    id = "exp_school_days",
    name = "2000s Kasi School Days",
    tagLine = "Morning assembly war cries, brown paper books & tuckshop runs",
    iconEmoji = "🎒",
    isFreeTier = false,
    priceCoins = 100,
    cards = listOf(
      CharadesCard("sc1", "Covering Textbooks with Brown Paper", "School Days", listOf("Folding corners neatly", "Sellotape sticking to fingers", "Writing name in cursive"), "2000s School", "The annual January school holiday ritual of covering all exercise books."),
      CharadesCard("sc2", "Morning Assembly National Anthem", "School Days", listOf("Standing stiff at attention", "Hand over heart", "Singing all 4 language stanzas"), "School Tradition", "South Africa's unique multi-lingual national anthem."),
      CharadesCard("sc3", "Sprinting When the Lunch Bell Rings", "School Days", listOf("Dropping pen instantly", "Dodging corridor crowds", "First in line at tuckshop"), "School Memories", "The desperate dash to buy hot pies or vetkoek before they sold out."),
      CharadesCard("sc4", "Inter-House Sports War Cries", "School Days", listOf("Stamping bleachers", "Chanting team rhyme", "Colored ribbons on t-shirts"), "School Pride", "High energy war cry performances during annual athletics days.")
    )
  )

  val EXP_ROAD_TRIP = Deck(
    id = "exp_road_trip",
    name = "Polokwane to Cape Town Road Trip",
    tagLine = "Padkos boiled eggs, toll gate coins, soft serve at Ultra City",
    iconEmoji = "🚗",
    isFreeTier = false,
    priceCoins = 120,
    cards = listOf(
      CharadesCard("rt1", "Opening the Tupperware of Padkos", "Road Trip", listOf("Cracking hard boiled egg", "Fried chicken in foil", "Strong smell in the hot car"), "Family Tradition", "Padkos (road food) is an unbreakable South African holiday tradition."),
      CharadesCard("rt2", "Toll Gate Scramble for Small Coins", "Road Trip", listOf("Digging into dashboard tray", "Reaching out window", "Dropping R5 coin on road"), "Holiday Drive", "Counting out R2 and R5 coins for the N1 highway toll gates."),
      CharadesCard("rt3", "Table Mountain Tablecloth Clouds", "Cape Town", listOf("Flat mountain shape with arms", "Rolling clouds spilling over", "Wind blowing hard"), "Cape Wonder", "The striking cloud phenomenon draping over Table Mountain."),
      CharadesCard("rt4", "Baboon Jumping on the Car Bonnet", "Kruger/Cape Point", listOf("Chimp posture on car", "Screaming inside locked vehicle", "Stealing fruit"), "Wildlife Encounters", "Mischievous baboons testing the bravery of holiday road trippers.")
    )
  )

  val EXP_MEMES = Deck(
    id = "exp_memes",
    name = "Mzansi Memes & Viral Legends",
    tagLine = "Listen properly, Uncle Vinny, Give that man a Bells!",
    iconEmoji = "🤣",
    isFreeTier = false,
    priceCoins = 120,
    cards = listOf(
      CharadesCard("m1", "'Give That Man a Bells!'", "Viral Lines", listOf("Clapping and nodding", "Handing over imaginary whisky", "Saluting a brilliant move"), "Classic Ad", "The iconic catchphrase for rewarding a true legend."),
      CharadesCard("m2", "'Listen Properly!' (Number Struggles)", "Viral Moment", listOf("Reading speech awkwardly", "Adjusting spectacles", "Counting on fingers"), "Political Humour", "A hilarious parody moment that took South African social media by storm."),
      CharadesCard("m3", "Uncle Vinny Stage Launch Dance", "Viral Dance", listOf("Running into open space", "Jumping wildly with arms flailing", "Youth crowd hype"), "Recent Viral", "His signature high-velocity stage entrance dance went globally viral."),
      CharadesCard("m4", "Vusi Nova Passionate High Note", "Viral Pop", listOf("Closing eyes tightly", "Vocal cord straining", "Dramatic arm outreach"), "Mzansi Music", "His soul-stirring vocal performances are famous for pure intensity.")
    )
  )

  val ALL_DECKS = listOf(
    CORE_KWAITO,
    CORE_TV_SOAPIES,
    CORE_SLANG,
    CORE_TUCKSHOP,
    CORE_SPORTS,
    EXP_PANTSULA_LIFE,
    EXP_SCHOOL_DAYS,
    EXP_ROAD_TRIP,
    EXP_MEMES
  )

  fun getDeckById(id: String): Deck? = ALL_DECKS.find { it.id == id }
}
