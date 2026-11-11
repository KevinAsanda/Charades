package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.model.Deck
import com.example.data.model.GameSession
import com.example.data.model.PredefinedDecks
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiRounded
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiPrimaryDeep
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import kotlinx.coroutines.delay
import kotlin.math.abs

private fun modeDisplayTitle(gameMode: String): String = when (gameMode) {
  "act_it_out" -> "Act It Out"
  "hit_the_beat" -> "Hit the Beat"
  "mzansi_30_seconds" -> "Mzansi 30-Seconds"
  "my_reaction" -> "My Reaction (Oct 2026)"
  "party_teams" -> "Party Teams"
  "heads_up" -> "Heads-Up Tilt"
  else -> "Quick Play"
}

@Composable
fun GamePlayScreen(
  deck: Deck,
  gameMode: String,
  roundDurationSeconds: Long,
  onFinishGame: (GameSession) -> Unit,
  onExit: () -> Unit
) {
  val context = LocalContext.current
  BackHandler { onExit() }

  var roundKey by remember { mutableIntStateOf(0) }
  val cards = remember(deck, roundKey) { deck.cards.shuffled() }
  var currentCardIndex by remember { mutableIntStateOf(0) }
  var timeLeft by remember { mutableLongStateOf(roundDurationSeconds) }
  var isPlaying by remember { mutableStateOf(false) }
  var isCountdownReady by remember { mutableStateOf(true) }
  var readyCountdown by remember { mutableIntStateOf(3) }
  var isTeamIntermission by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var showHints by remember { mutableStateOf(gameMode == "mzansi_30_seconds") }

  // My Reaction mode: track selected facial expression rank (1 to 5)
  var selectedReactionRank by remember { mutableIntStateOf(5) }

  // Skip Penalty / Roast overlay state (Section 9.1 of Blueprint)
  var activeSkipRoastLine by remember { mutableStateOf("") }
  var skipRoastCount by remember { mutableIntStateOf(0) }

  val guessedWords = remember { mutableStateListOf<String>() }
  val passedWords = remember { mutableStateListOf<String>() }

  var teamAScore by remember { mutableIntStateOf(0) }
  var teamBScore by remember { mutableIntStateOf(0) }
  var activeTeam by remember { mutableStateOf("Team A") }

  val sensorManager = remember {
    context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  }
  var lastTiltTime by remember { mutableLongStateOf(0L) }
  var isTiltArmed by remember { mutableStateOf(false) }
  val hasAccelerometer = remember {
    sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
  }

  // Pre-game guide & 6 Signature Mzansi Physical Cues shown once before first countdown
  var isGuideShown by remember { mutableStateOf(false) }

  // Full-screen flash feedback ("correct" / "pass")
  var feedbackKind by remember { mutableStateOf<String?>(null) }
  var feedbackTick by remember { mutableIntStateOf(0) }

  LaunchedEffect(feedbackTick) {
    if (feedbackTick > 0) {
      delay(750)
      feedbackKind = null
    }
  }

  // Immersive full-screen mode while playing: hide system bars and keep the screen awake
  val view = LocalView.current
  DisposableEffect(Unit) {
    var ctx: Context = view.context
    while (ctx is ContextWrapper && ctx !is Activity) ctx = ctx.baseContext
    val window = (ctx as? Activity)?.window
    val controller = window?.let { WindowCompat.getInsetsController(it, view) }
    controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller?.hide(WindowInsetsCompat.Type.systemBars())
    view.keepScreenOn = true
    onDispose {
      controller?.show(WindowInsetsCompat.Type.systemBars())
      view.keepScreenOn = false
    }
  }

  // Single shared ToneGenerator for the whole match (creating one per card leaks native
  // audio tracks and makes sound stop working after a few rounds on real devices)
  val toneGenerator = remember {
    runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 75) }.getOrNull()
  }
  DisposableEffect(toneGenerator) {
    onDispose { runCatching { toneGenerator?.release() } }
  }

  fun triggerHapticAndSound(success: Boolean, isTierOneSkip: Boolean = false) {
    try {
      val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (success) {
          vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 90, 50, 110), -1))
        }
      }
    } catch (_: Exception) {}

    // Synthesize audio cue for Skip Penalty / Roast Pack or Correct chime
    try {
      val toneGen = toneGenerator ?: return
      if (success) {
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 140)
      } else if (isTierOneSkip) {
        toneGen.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 260)
      } else {
        toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 180)
      }
    } catch (_: Exception) {}
  }

  fun advanceOrCompleteTurn() {
    if (currentCardIndex + 1 < cards.size) {
      currentCardIndex++
    } else if (gameMode == "party_teams" && activeTeam == "Team A") {
      roundKey++
      currentCardIndex = 0
      isPlaying = false
      isTeamIntermission = true
    } else {
      isPlaying = false
      isGameOver = true
    }
  }

  fun onCorrect() {
    if (!isPlaying || isGameOver || isTeamIntermission || currentCardIndex >= cards.size) return
    val card = cards[currentCardIndex]
    guessedWords.add(card.term)
    if (activeTeam == "Team A") teamAScore++ else teamBScore++
    triggerHapticAndSound(success = true)
    feedbackKind = "correct"
    feedbackTick++
    advanceOrCompleteTurn()
  }

  fun onPass() {
    if (!isPlaying || isGameOver || isTeamIntermission || currentCardIndex >= cards.size) return
    val card = cards[currentCardIndex]
    passedWords.add(card.term)
    skipRoastCount++
    val roastPool = PredefinedDecks.SKIP_ROAST_LINES
    activeSkipRoastLine = if (card.difficultyTier == 1) {
      "Tier-1 Easy Skip Penalty: ${roastPool[skipRoastCount % roastPool.size]}"
    } else {
      roastPool[skipRoastCount % roastPool.size]
    }
    triggerHapticAndSound(success = false, isTierOneSkip = card.difficultyTier == 1)
    feedbackKind = "pass"
    feedbackTick++
    advanceOrCompleteTurn()
  }

  // Tilt Sensor Listener: works across all modes when phone is held upright on forehead
  DisposableEffect(gameMode, isPlaying) {
    if (isPlaying && sensorManager != null) {
      val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          val now = System.currentTimeMillis()
          val z = event.values[2]

          if (abs(z) < 5.0f) {
            isTiltArmed = true
            return
          }

          if (!isTiltArmed || now - lastTiltTime < 1100L) return

          if (z < -7.0f) {
            isTiltArmed = false
            lastTiltTime = now
            onCorrect()
          } else if (z > 7.0f) {
            isTiltArmed = false
            lastTiltTime = now
            onPass()
          }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
      }

      accelerometer?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
      }

      onDispose {
        isTiltArmed = false
        sensorManager.unregisterListener(listener)
      }
    } else {
      onDispose {}
    }
  }

  // Ready 3-2-1 countdown
  LaunchedEffect(isCountdownReady, isGuideShown) {
    if (isCountdownReady && isGuideShown) {
      while (readyCountdown > 0) {
        delay(1000)
        readyCountdown--
      }
      isCountdownReady = false
      isPlaying = true
    }
  }

  // Round timer countdown
  LaunchedEffect(isPlaying, isGameOver, isTeamIntermission) {
    if (isPlaying && !isGameOver && !isTeamIntermission) {
      while (timeLeft > 0 && !isGameOver && !isTeamIntermission) {
        delay(1000)
        timeLeft--
      }
      if (timeLeft <= 0) {
        isPlaying = false
        triggerHapticAndSound(success = false)
        if (gameMode == "party_teams" && activeTeam == "Team A") {
          isTeamIntermission = true
        } else {
          isGameOver = true
        }
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = StripiCanvas
  ) {
    if (!isGuideShown) {
      TiltGuideScreen(
        gameMode = gameMode,
        deckName = deck.name,
        hasAccelerometer = hasAccelerometer,
        onStart = { isGuideShown = true }
      )
    } else if (isCountdownReady) {
      // 3-2-1 Get Ready Screen with Stripi Gradient Mesh
      StripiGradientMeshHero(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          StripiSoftPillTag(
            text = if (gameMode == "party_teams") "$activeTeam Turn" else modeDisplayTitle(gameMode)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = when (gameMode) {
              "heads_up" -> "Place phone upright on forehead"
              "hit_the_beat" -> "Get ready to dance & hum the beat"
              "mzansi_30_seconds" -> "Describe fast without forbidden words"
              "my_reaction" -> "Rank 1 to 5 with facial expressions only"
              "party_teams" -> "$activeTeam: Get ready to perform"
              else -> "Get ready to Act It Out (zero words)"
            },
            style = StripiTypeTokens.displayLg.copy(color = StripiInk),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = deck.name,
            style = StripiTypeTokens.bodyLg.copy(color = StripiInkSecondary)
          )
          Spacer(modifier = Modifier.height(32.dp))
          Box(
            modifier = Modifier
              .size(128.dp)
              .clip(CircleShape)
              .background(StripiBrandDark900),
            contentAlignment = Alignment.Center
          ) {
            StripiTabularText(
              text = if (readyCountdown > 0) "$readyCountdown" else "GO",
              color = StripiOnPrimary,
              fontSize = 48,
              fontWeight = FontWeight.Light
            )
          }
        }
      }
    } else if (isTeamIntermission) {
      // Team A -> Team B Intermission Screen for Party Teams Mode
      StripiGradientMeshHero(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(StripiSubduedHover.copy(alpha = 0.45f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "Pass to Team B",
                tint = StripiPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(14.dp))
            StripiSoftPillTag(text = "Halftime Switch")
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Pass Phone to Team B.",
              style = StripiTypeTokens.displayLg.copy(color = StripiInk),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Team A finished their round. Now Team B steps up to beat their score.",
              style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary),
              textAlign = TextAlign.Center
            )
          }

          StripiFeaturedDarkCard(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Team A Score to Beat",
                style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
              )
              Spacer(modifier = Modifier.height(6.dp))
              StripiTabularText(
                text = "$teamAScore",
                color = StripiOnPrimary,
                fontSize = 48,
                fontWeight = FontWeight.Light
              )
              Spacer(modifier = Modifier.height(8.dp))
              StripiSoftPillTag(
                text = "${roundDurationSeconds}s on the clock for Team B",
                containerColor = StripiPrimary,
                contentColor = StripiOnPrimary
              )
            }
          }

          StripiPrimaryPillButton(
            text = "Start Team B Turn",
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("start_team_b_button"),
            onClick = {
              activeTeam = "Team B"
              if (currentCardIndex >= cards.size) {
                roundKey++
                currentCardIndex = 0
              }
              timeLeft = roundDurationSeconds
              readyCountdown = 3
              isTeamIntermission = false
              isCountdownReady = true
            }
          )
        }
      }
    } else if (isGameOver) {
      // Game Over Summary Screen + Section 9.3 "Me and My POV" Split-Screen Share Card
      val totalScore = guessedWords.size.toLong()
      val session = remember(roundKey, teamAScore, teamBScore, totalScore) {
        GameSession(
          deckId = deck.id,
          deckName = deck.name,
          gameMode = gameMode,
          teamAScore = if (gameMode == "party_teams") teamAScore.toLong() else totalScore,
          teamBScore = teamBScore.toLong(),
          wordsGuessed = guessedWords.toList(),
          wordsPassed = passedWords.toList(),
          durationSeconds = roundDurationSeconds
        )
      }

      LaunchedEffect(session) {
        onFinishGame(session)
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(StripiCanvas)
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(StripiSubduedHover.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = "Trophy",
              tint = StripiPrimary,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          StripiSoftPillTag(text = modeDisplayTitle(gameMode))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (gameMode == "party_teams") {
              when {
                teamAScore > teamBScore -> "Team A Wins!"
                teamBScore > teamAScore -> "Team B Wins!"
                else -> "It's a Draw!"
              }
            } else {
              "Round Complete."
            },
            style = StripiTypeTokens.displayLg.copy(color = StripiInk)
          )
          Text(
            text = deck.name,
            style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
          )
        }

        // Featured Dark Score Telemetry Card (`card-pricing-featured` #1C1E54)
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (gameMode == "party_teams") {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("Team A", color = StripiSubduedHover, style = StripiTypeTokens.caption)
                  StripiTabularText(
                    text = "$teamAScore",
                    color = StripiOnPrimary,
                    fontSize = 36,
                    fontWeight = FontWeight.Light
                  )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("Team B", color = StripiSubduedHover, style = StripiTypeTokens.caption)
                  StripiTabularText(
                    text = "$teamBScore",
                    color = StripiOnPrimary,
                    fontSize = 36,
                    fontWeight = FontWeight.Light
                  )
                }
              }
            } else {
              Text(
                text = "Words Guessed",
                style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
              )
              Spacer(modifier = Modifier.height(4.dp))
              StripiTabularText(
                text = "${guessedWords.size}",
                color = StripiOnPrimary,
                fontSize = 48,
                fontWeight = FontWeight.Light
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            StripiSoftPillTag(
              text = "+${10 + (guessedWords.size * 5)} Coins Earned",
              containerColor = StripiPrimary,
              contentColor = StripiOnPrimary
            )

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 16.dp),
              color = StripiOnPrimary.copy(alpha = 0.15f)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              StripiTabularText(
                text = "Correct: ${guessedWords.size}",
                color = StripiSubduedHover,
                fontSize = 14,
                fontWeight = FontWeight.Normal
              )
              StripiTabularText(
                text = "Passed: ${passedWords.size}",
                color = StripiRuby,
                fontSize = 14,
                fontWeight = FontWeight.Normal
              )
            }
          }
        }

        // Blueprint Section 9.3: In-App "Me and My POV" Split-Screen Share Card
        StripiCreamCard(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            StripiSoftPillTag(
              text = "2026 TikTok Format: Me & My POV",
              containerColor = StripiBrandDark900,
              contentColor = StripiOnPrimary
            )
            Text(
              text = "Split-Screen Recap",
              style = StripiTypeTokens.micro.copy(color = StripiInkMute)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Split-Screen 2-Column Visual Card ("ME" vs "MY POV")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(
              modifier = Modifier
                .weight(1f)
                .clip(StripiRounded.md)
                .background(StripiCanvas)
                .border(1.dp, StripiHairline, StripiRounded.md)
                .padding(12.dp)
            ) {
              Text(
                text = "ME (PERFORMING)",
                style = StripiTypeTokens.microCap.copy(color = StripiPrimary)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = guessedWords.firstOrNull() ?: deck.cards.firstOrNull()?.term.orEmpty(),
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Mode: ${modeDisplayTitle(gameMode)}",
                style = StripiTypeTokens.micro.copy(color = StripiInkMute)
              )
            }

            Column(
              modifier = Modifier
                .weight(1f)
                .clip(StripiRounded.md)
                .background(StripiBrandDark900)
                .padding(12.dp)
            ) {
              Text(
                text = "MY POV (THE CREW)",
                style = StripiTypeTokens.microCap.copy(color = StripiSubduedHover)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (passedWords.isNotEmpty()) {
                  "Roasting ${passedWords.size} skips in 5 languages!"
                } else {
                  "Zero skips! Pure Mzansi Grootman energy."
                },
                style = StripiTypeTokens.caption.copy(color = StripiOnPrimary)
              )
              Spacer(modifier = Modifier.height(4.dp))
              StripiTabularText(
                text = "Score: ${guessedWords.size} in ${roundDurationSeconds}s",
                color = StripiSubduedHover,
                fontSize = 12
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          StripiSecondaryPillButton(
            text = "Share 'Me & My POV' to WhatsApp / TikTok",
            modifier = Modifier
              .fillMaxWidth()
              .testTag("share_pov_button"),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = StripiPrimary,
                modifier = Modifier.size(16.dp)
              )
            },
            onClick = {
              val shareText = buildString {
                append("Mzansi Charades (Me & My POV):\n")
                append("Deck: ${deck.name} (${modeDisplayTitle(gameMode)})\n")
                append("ME: Acting out ${guessedWords.take(3).joinToString(", ").ifBlank { "Mzansi classics" }}\n")
                append("MY POV: Guessed ${guessedWords.size} cards in ${roundDurationSeconds}s! ")
                if (passedWords.isNotEmpty()) {
                  append("(Skipped ${passedWords.size}: Is not make sure!)")
                } else {
                  append("(Zero skips - Sharp Sharp!)")
                }
              }
              val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
              }
              runCatching {
                context.startActivity(Intent.createChooser(sendIntent, "Share Mzansi POV Recap"))
              }
            }
          )
        }

        // Action Pill Buttons (1 Secondary + 1 Primary per Stripi single-CTA rule)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StripiSecondaryPillButton(
            text = "Back to Lobby",
            modifier = Modifier
              .weight(1f)
              .testTag("exit_game_button"),
            onClick = onExit
          )

          StripiPrimaryPillButton(
            text = "Play Again",
            modifier = Modifier
              .weight(1f)
              .testTag("play_again_button"),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = null,
                tint = StripiOnPrimary,
                modifier = Modifier.size(16.dp)
              )
            },
            onClick = {
              roundKey++
              currentCardIndex = 0
              timeLeft = roundDurationSeconds
              guessedWords.clear()
              passedWords.clear()
              teamAScore = 0
              teamBScore = 0
              activeTeam = "Team A"
              isTeamIntermission = false
              readyCountdown = 3
              isCountdownReady = true
              isGameOver = false
            }
          )
        }
      }
    } else {
      // Full-screen Active Gameplay View with Mode-Specific Stage & Skip Roast Overlay
      val currentCard = cards.getOrNull(currentCardIndex)
      val stageColor by animateColorAsState(
        targetValue = when (feedbackKind) {
          "correct" -> StripiPrimary
          "pass" -> StripiRuby
          else -> StripiBrandDark900
        },
        animationSpec = tween(durationMillis = 150),
        label = "tilt_feedback_flash"
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(stageColor)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Slim top bar: exit, timer, score, hints
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            IconButton(onClick = onExit, modifier = Modifier.size(40.dp)) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Exit Game",
                tint = StripiOnPrimary.copy(alpha = 0.75f)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = StripiOnPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              StripiTabularText(
                text = "${timeLeft}s",
                color = if (timeLeft <= 10) StripiSubduedHover else StripiOnPrimary,
                fontSize = 22,
                fontWeight = FontWeight.Normal
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              StripiSoftPillTag(
                text = if (gameMode == "party_teams") {
                  "$activeTeam: ${if (activeTeam == "Team A") teamAScore else teamBScore}"
                } else {
                  "Score: ${guessedWords.size}"
                },
                containerColor = StripiSubduedHover,
                contentColor = StripiBrandDark900
              )
              IconButton(onClick = { showHints = !showHints }, modifier = Modifier.size(40.dp)) {
                Icon(
                  imageVector = Icons.Default.Lightbulb,
                  contentDescription = "Toggle Hints",
                  tint = if (showHints) StripiSubduedHover else StripiOnPrimary.copy(alpha = 0.55f)
                )
              }
            }
          }

          // Giant prompt stage (or instant Correct / Skip Roast Penalty feedback)
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            if (feedbackKind != null) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
              ) {
                Text(
                  text = if (feedbackKind == "correct") "Sharp Sharp! +1" else "Pass!",
                  style = StripiTypeTokens.displayXxl.copy(color = StripiOnPrimary),
                  textAlign = TextAlign.Center
                )
                if (feedbackKind == "pass" && activeSkipRoastLine.isNotBlank()) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = activeSkipRoastLine,
                    style = StripiTypeTokens.headingSm.copy(color = StripiOnPrimary),
                    textAlign = TextAlign.Center
                  )
                }
              }
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
              ) {
                // Era Tag + Difficulty Tier + Category Badges
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  StripiSoftPillTag(
                    text = currentCard?.category.orEmpty(),
                    containerColor = StripiPrimary,
                    contentColor = StripiOnPrimary
                  )
                  if (currentCard != null) {
                    StripiSoftPillTag(
                      text = currentCard.tierLabel(),
                      containerColor = StripiOnPrimary.copy(alpha = 0.16f),
                      contentColor = StripiSubduedHover
                    )
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  text = currentCard?.term.orEmpty(),
                  style = StripiTypeTokens.displayXxl.copy(color = StripiOnPrimary),
                  textAlign = TextAlign.Center
                )

                if (!currentCard?.culturalTrivia.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = currentCard?.culturalTrivia.orEmpty(),
                    style = StripiTypeTokens.bodyMd.copy(
                      color = StripiSubduedHover.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                  )
                }

                // Mode-Specific Stage Helper:
                // 1) Mzansi 30-Seconds: Forbidden Root Words Banner
                if (gameMode == "mzansi_30_seconds" && currentCard != null) {
                  Spacer(modifier = Modifier.height(16.dp))
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(StripiRounded.md)
                      .background(StripiRuby.copy(alpha = 0.24f))
                      .border(1.dp, StripiRuby, StripiRounded.md)
                      .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = StripiOnPrimary,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "FORBIDDEN ROOT WORDS (DO NOT SAY):",
                        style = StripiTypeTokens.microCap.copy(color = StripiOnPrimary)
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = currentCard.effectiveForbiddenWords().joinToString(" · "),
                      style = StripiTypeTokens.headingSm.copy(color = StripiOnPrimary),
                      textAlign = TextAlign.Center
                    )
                  }
                }

                // 2) My Reaction (Oct 2026 TikTok Trend): 1-to-5 Facial Expression Ranking Bar
                if (gameMode == "my_reaction") {
                  Spacer(modifier = Modifier.height(16.dp))
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(StripiRounded.md)
                      .background(StripiOnPrimary.copy(alpha = 0.10f))
                      .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text(
                      text = "OCT 2026 TREND: POINT TO RANK 1-5 WITH YOUR FACE ONLY",
                      style = StripiTypeTokens.microCap.copy(color = StripiSubduedHover)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                      listOf(
                        1 to "1: Eish",
                        2 to "2: Meh",
                        3 to "3: Sharp",
                        4 to "4: Vibe",
                        5 to "5: Lit"
                      ).forEach { (rank, label) ->
                        val isRankSelected = selectedReactionRank == rank
                        Surface(
                          shape = StripiRounded.pill,
                          color = if (isRankSelected) StripiPrimary else StripiBrandDark900,
                          border = BorderStroke(
                            1.dp,
                            if (isRankSelected) StripiSubduedHover else StripiOnPrimary.copy(alpha = 0.25f)
                          ),
                          modifier = Modifier.clickable { selectedReactionRank = rank }
                        ) {
                          Text(
                            text = label,
                            style = StripiTypeTokens.caption.copy(color = StripiOnPrimary),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                          )
                        }
                      }
                    }
                  }
                }

                // 3) Hit the Beat: Rhythm & Choreography Cue Banner
                if (gameMode == "hit_the_beat") {
                  Spacer(modifier = Modifier.height(14.dp))
                  StripiSoftPillTag(
                    text = "Dance choreography & humming allowed (No lyrics!)",
                    containerColor = StripiOnPrimary.copy(alpha = 0.14f),
                    contentColor = StripiSubduedHover
                  )
                }
              }
            }
          }

          AnimatedVisibility(visible = showHints && feedbackKind == null) {
            StripiCreamCard(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
              contentPadding = PaddingValues(14.dp)
            ) {
              Text(
                text = when (gameMode) {
                  "mzansi_30_seconds" -> "30-Seconds Verbal Clue Angles"
                  "hit_the_beat" -> "Dance & Rhythm Cues"
                  "my_reaction" -> "Facial Expression Cues"
                  else -> "Silent Acting Clues"
                },
                style = StripiTypeTokens.caption.copy(color = StripiInk)
              )
              Spacer(modifier = Modifier.height(4.dp))
              currentCard?.hints?.forEach { hint ->
                Text(
                  text = "- $hint",
                  style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
                )
              }
            }
          }

          // Bottom controls: Both Tilt status pills (when accelerometer exists) AND Pass/Correct buttons
          if (hasAccelerometer) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TiltHintPill(icon = Icons.Default.ArrowDownward, text = "Tilt down: Correct")
              TiltHintPill(
                icon = Icons.Default.PhoneAndroid,
                text = if (isTiltArmed) "Tilt Armed" else "Hold upright",
                highlighted = isTiltArmed
              )
              TiltHintPill(icon = Icons.Default.ArrowUpward, text = "Tilt up: Pass")
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            StripiSecondaryPillButton(
              text = "Pass (Roast)",
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("pass_card_button"),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Pass",
                  tint = StripiPrimary,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = { onPass() }
            )
            StripiPrimaryPillButton(
              text = "Correct (+1)",
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("correct_card_button"),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Correct",
                  tint = StripiOnPrimary,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = { onCorrect() }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TiltHintPill(
  icon: ImageVector,
  text: String,
  highlighted: Boolean = false
) {
  Row(
    modifier = Modifier
      .clip(CircleShape)
      .background(
        if (highlighted) StripiOnPrimary.copy(alpha = 0.28f) else StripiOnPrimary.copy(alpha = 0.14f)
      )
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = StripiOnPrimary,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      style = StripiTypeTokens.caption.copy(color = StripiOnPrimary),
      maxLines = 1
    )
  }
}

@Composable
private fun TiltGuideStep(
  icon: ImageVector,
  title: String,
  body: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(StripiRounded.lg)
      .background(StripiCanvas)
      .border(1.dp, StripiHairline, StripiRounded.lg)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(StripiSubduedHover.copy(alpha = 0.45f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = StripiPrimary,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = StripiTypeTokens.headingSm.copy(color = StripiInk))
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = body, style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary))
    }
  }
}

@Composable
private fun TiltGuideScreen(
  gameMode: String,
  deckName: String,
  hasAccelerometer: Boolean,
  onStart: () -> Unit
) {
  var showPhysicalCues by remember { mutableStateOf(false) }

  StripiGradientMeshHero(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      StripiSoftPillTag(text = modeDisplayTitle(gameMode))
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = when (gameMode) {
          "hit_the_beat" -> "Dance, rhythm & hum."
          "mzansi_30_seconds" -> "30-Seconds verbal blitz."
          "my_reaction" -> "Rank 1-5 with your face."
          "act_it_out" -> "Act It Out (Silent mime)."
          else -> "Tilt or tap to answer."
        },
        style = StripiTypeTokens.displayLg.copy(color = StripiInk),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = deckName,
        style = StripiTypeTokens.bodyLg.copy(color = StripiInkSecondary),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(20.dp))

      // Mode-specific primary instruction step
      when (gameMode) {
        "hit_the_beat" -> {
          TiltGuideStep(
            icon = Icons.Default.MusicNote,
            title = "1. Hit the choreography & hum",
            body = "Perform Amapiano, 3-Step, Lekompo, or Bacardi footwork and hum the bassline without singing any words."
          )
        }
        "mzansi_30_seconds" -> {
          TiltGuideStep(
            icon = Icons.Default.RecordVoiceOver,
            title = "1. Rapid 30s verbal description",
            body = "Describe as many Mzansi prompts as possible before the 30s buzzer, without saying any Forbidden Root Words on the card."
          )
        }
        "my_reaction" -> {
          TiltGuideStep(
            icon = Icons.Default.Face,
            title = "1. Oct 2026 'My Reaction' Ranking",
            body = "Point to invisible ranks 1 to 5 in the air and shift your face from disgust to joy with zero spoken words."
          )
        }
        else -> {
          TiltGuideStep(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            title = "1. Physical comedy & SASL gestures",
            body = "Use pure physical mime, SASL youth signs ('Sharp sharp', 'Vibe'), and body language to get your crew shouting the answer."
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (hasAccelerometer) {
        TiltGuideStep(
          icon = Icons.Default.ArrowDownward,
          title = "2. Tilt DOWN or Tap Correct (+1)",
          body = "Nod the phone forward toward the floor (or tap Correct) when your crew guesses the card."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TiltGuideStep(
          icon = Icons.Default.ArrowUpward,
          title = "3. Tilt UP or Tap Pass (Skip Roast!)",
          body = "Tip the phone back toward the ceiling to skip, but watch out for the Mzansi Skip Roast penalty on easy cards!"
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 8 Blueprint: 6 Signature Mzansi Physical Cues Tutorial Accordion
      StripiFeatureCardLight(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = StripiCanvasSoft,
        contentPadding = PaddingValues(16.dp),
        onClick = { showPhysicalCues = !showPhysicalCues }
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "6 Signature Mzansi Physical Cues",
              style = StripiTypeTokens.headingSm.copy(color = StripiInk)
            )
            Text(
              text = if (showPhysicalCues) "Tap to hide warmup gestures" else "Tap to preview Bacardi, SASL Vibe, Shoe Kick & more",
              style = StripiTypeTokens.caption.copy(color = StripiInkMute)
            )
          }
          StripiSoftPillTag(
            text = if (showPhysicalCues) "Hide" else "Warmup",
            containerColor = StripiSubduedHover,
            contentColor = StripiPrimaryDeep
          )
        }

        AnimatedVisibility(visible = showPhysicalCues) {
          Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            PredefinedDecks.SIGNATURE_PHYSICAL_CUES.forEach { cue ->
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(StripiRounded.md)
                  .background(StripiCanvas)
                  .border(1.dp, StripiHairline, StripiRounded.md)
                  .padding(12.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${cue.number}. ${cue.title}",
                    style = StripiTypeTokens.headingSm.copy(color = StripiPrimary)
                  )
                  Text(
                    text = cue.eraOrContext,
                    style = StripiTypeTokens.micro.copy(color = StripiInkMute)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = cue.instruction,
                  style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      StripiPrimaryPillButton(
        text = "I'm Ready, Start Countdown",
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("start_game_button"),
        onClick = onStart
      )
    }
  }
}
