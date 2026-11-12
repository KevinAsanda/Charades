package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

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

  // Immersive full-screen mode and automatic landscape orientation during active play
  val view = LocalView.current
  val activity = remember(context, view) { view.context.findActivity() ?: context.findActivity() }
  val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  val shouldUseLandscape = isGuideShown && !isTeamIntermission && !isGameOver

  DisposableEffect(activity, view, shouldUseLandscape) {
    val window = activity?.window
    val controller = window?.let { WindowCompat.getInsetsController(it, view) }

    if (shouldUseLandscape) {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      controller?.hide(WindowInsetsCompat.Type.systemBars())
      view.keepScreenOn = true
    } else {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      controller?.show(WindowInsetsCompat.Type.systemBars())
      view.keepScreenOn = false
    }

    onDispose {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
      // 3-2-1 Get Ready Screen with Stripi Gradient Mesh (adapts cleanly to landscape)
      StripiGradientMeshHero(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(if (isLandscape) 20.dp else 24.dp)
      ) {
        val readyText = when (gameMode) {
          "heads_up" -> "Place phone sideways on forehead"
          "hit_the_beat" -> "Get ready to dance & hum the beat"
          "mzansi_30_seconds" -> "Describe fast without forbidden words"
          "my_reaction" -> "Rank 1 to 5 with facial expressions only"
          "party_teams" -> "$activeTeam: Get ready to perform"
          else -> "Get ready to Act It Out (zero words)"
        }
        val headerBlock: @Composable (Modifier, TextAlign, Alignment.Horizontal) -> Unit =
          { mod, align, colAlign ->
            Column(modifier = mod, horizontalAlignment = colAlign) {
              StripiSoftPillTag(
                text = if (gameMode == "party_teams") "$activeTeam Turn" else modeDisplayTitle(gameMode)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = readyText,
                style = StripiTypeTokens.displayLg.copy(color = StripiInk),
                textAlign = align
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = deck.name,
                style = StripiTypeTokens.bodyLg.copy(color = StripiInkSecondary),
                textAlign = align
              )
            }
          }
        val countdownCircle: @Composable () -> Unit = {
          Box(
            modifier = Modifier
              .size(if (isLandscape) 112.dp else 128.dp)
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

        if (isLandscape) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.safeDrawing)
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
          ) {
            headerBlock(Modifier.weight(1f), TextAlign.Start, Alignment.Start)
            countdownCircle()
          }
        } else {
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            headerBlock(Modifier.fillMaxWidth(), TextAlign.Center, Alignment.CenterHorizontally)
            Spacer(modifier = Modifier.height(32.dp))
            countdownCircle()
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
            .padding(
              horizontal = if (isLandscape) 24.dp else 20.dp,
              vertical = if (isLandscape) 8.dp else 12.dp
            ),
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

            Surface(
              shape = StripiRounded.pill,
              color = if (timeLeft <= 10) StripiRuby.copy(alpha = 0.28f) else StripiOnPrimary.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, if (timeLeft <= 10) StripiRuby else StripiOnPrimary.copy(alpha = 0.2f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = if (timeLeft <= 10) StripiRuby else StripiOnPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                StripiTabularText(
                  text = "${timeLeft}s",
                  color = if (timeLeft <= 10) StripiRuby else StripiOnPrimary,
                  fontSize = 18,
                  fontWeight = FontWeight.Medium
                )
              }
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

          // Center Prompt Stage (Hero word dominates the screen with clean breathing room)
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
                modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
              ) {
                // Category & Tier Badges
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = StripiRounded.pill,
                    color = StripiPrimary
                  ) {
                    Text(
                      text = currentCard?.category.orEmpty().uppercase(),
                      style = StripiTypeTokens.microCap.copy(color = StripiOnPrimary),
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                  }
                  if (currentCard != null) {
                    Surface(
                      shape = StripiRounded.pill,
                      color = StripiOnPrimary.copy(alpha = 0.16f)
                    ) {
                      Text(
                        text = currentCard.tierLabel(),
                        style = StripiTypeTokens.microCap.copy(color = StripiSubduedHover),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 20.dp))

                // Hero Prompt Word with responsive font size based on length & orientation
                val term = currentCard?.term.orEmpty()
                val termFontSize = when {
                  term.length > 28 -> if (isLandscape) 34.sp else 32.sp
                  term.length > 16 -> if (isLandscape) 46.sp else 40.sp
                  else -> if (isLandscape) 60.sp else 50.sp
                }
                Text(
                  text = term,
                  style = StripiTypeTokens.displayXxl.copy(
                    fontSize = termFontSize,
                    lineHeight = termFontSize * 1.15f,
                    color = StripiOnPrimary
                  ),
                  textAlign = TextAlign.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                )

                if (!currentCard?.culturalTrivia.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = currentCard?.culturalTrivia.orEmpty(),
                    style = StripiTypeTokens.caption.copy(
                      color = StripiSubduedHover.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 20.dp)
                  )
                }

                // 1) Mzansi 30-Seconds: Forbidden Root Words Capsule (Clean chips, no harsh block)
                if (gameMode == "mzansi_30_seconds" && currentCard != null) {
                  Spacer(modifier = Modifier.height(18.dp))
                  Surface(
                    shape = StripiRounded.lg,
                    color = StripiCanvas.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, StripiRuby.copy(alpha = 0.45f)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StripiRuby)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "FORBIDDEN WORDS (DO NOT SAY)",
                          style = StripiTypeTokens.microCap.copy(color = StripiRuby, letterSpacing = 0.5.sp)
                        )
                      }
                      Spacer(modifier = Modifier.height(8.dp))
                      Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                      ) {
                        currentCard.effectiveForbiddenWords().forEach { word ->
                          Surface(
                            shape = StripiRounded.pill,
                            color = StripiRuby.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, StripiRuby.copy(alpha = 0.35f))
                          ) {
                            Text(
                              text = word,
                              style = StripiTypeTokens.caption.copy(color = StripiOnPrimary),
                              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                          }
                        }
                      }
                    }
                  }
                }

                // 2) My Reaction: 1-to-5 Facial Expression Ranking Bar
                if (gameMode == "my_reaction") {
                  Spacer(modifier = Modifier.height(18.dp))
                  Surface(
                    shape = StripiRounded.lg,
                    color = StripiOnPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, StripiOnPrimary.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(
                        text = "POINT TO RANK 1-5 WITH YOUR FACE ONLY",
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
                }

                // 3) Hit the Beat: Rhythm & Choreography Cue
                if (gameMode == "hit_the_beat") {
                  Spacer(modifier = Modifier.height(16.dp))
                  Surface(
                    shape = StripiRounded.pill,
                    color = StripiOnPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, StripiOnPrimary.copy(alpha = 0.2f))
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = StripiSubduedHover,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Dance & hum the rhythm (No spoken lyrics!)",
                        style = StripiTypeTokens.caption.copy(color = StripiSubduedHover)
                      )
                    }
                  }
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

          // Bottom controls: Clean unified Tilt HUD capsule when sensor is present, or Buttons fallback
          if (hasAccelerometer) {
            Surface(
              shape = StripiRounded.pill,
              color = StripiCanvas.copy(alpha = 0.10f),
              border = BorderStroke(1.dp, StripiOnPrimary.copy(alpha = 0.18f)),
              modifier = Modifier.padding(bottom = 10.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = StripiSubduedHover,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Down: Correct",
                    style = StripiTypeTokens.caption.copy(color = StripiOnPrimary)
                  )
                }

                Box(
                  modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(StripiOnPrimary.copy(alpha = 0.25f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (isTiltArmed) StripiSubduedHover else StripiRuby)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = if (isTiltArmed) "Forehead Ready" else "Hold Upright",
                    style = StripiTypeTokens.caption.copy(
                      color = if (isTiltArmed) StripiSubduedHover else StripiOnPrimary.copy(alpha = 0.7f)
                    )
                  )
                }

                Box(
                  modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(StripiOnPrimary.copy(alpha = 0.25f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = StripiOnPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Up: Pass",
                    style = StripiTypeTokens.caption.copy(color = StripiOnPrimary.copy(alpha = 0.85f))
                  )
                }
              }
            }
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              StripiSecondaryPillButton(
                text = "Pass (Skip)",
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
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StripiSoftPillTag(text = modeDisplayTitle(gameMode))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = when (gameMode) {
            "hit_the_beat" -> "Dance, rhythm & hum."
            "mzansi_30_seconds" -> "30-Seconds verbal blitz."
            "my_reaction" -> "Rank 1-5 with your face."
            "act_it_out" -> "Act It Out (Silent mime)."
            else -> "Tilt to guess."
          },
          style = StripiTypeTokens.displayLg.copy(color = StripiInk),
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = deckName,
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary),
          textAlign = TextAlign.Center
        )
      }

      // 1. Dedicated Forehead & Tilt Direction Visual Card
      StripiFeatureCardLight(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "HOW TO PLAY",
            style = StripiTypeTokens.microCap.copy(color = StripiPrimary)
          )
          StripiSoftPillTag(
            text = if (hasAccelerometer) "Tilt Sensor Active" else "Touch Screen Active",
            containerColor = StripiSubduedHover,
            contentColor = StripiBrandDark900
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // 1. Forehead placement
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(StripiRounded.md)
              .background(StripiCanvasSoft)
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StripiBrandDark900),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = StripiOnPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "1. Place Sideways on Forehead",
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Text(
                text = "Screen rotates to landscape when you start - hold horizontally facing your friends.",
                style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
              )
            }
          }

          if (hasAccelerometer) {
            // 2. Tilt Down (Correct)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(StripiRounded.md)
                .background(StripiPrimary.copy(alpha = 0.08f))
                .border(1.dp, StripiPrimary.copy(alpha = 0.25f), StripiRounded.md)
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(StripiPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = StripiOnPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "2. Tilt DOWN = Correct (+1)",
                  style = StripiTypeTokens.headingSm.copy(color = StripiPrimaryDeep)
                )
                Text(
                  text = "Nod phone down toward the floor when your team guesses right.",
                  style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
                )
              }
            }

            // 3. Tilt Up (Pass)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(StripiRounded.md)
                .background(StripiRuby.copy(alpha = 0.07f))
                .border(1.dp, StripiRuby.copy(alpha = 0.25f), StripiRounded.md)
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(StripiRuby),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = StripiOnPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "3. Tilt UP = Pass (Skip)",
                  style = StripiTypeTokens.headingSm.copy(color = StripiRuby)
                )
                Text(
                  text = "Tip phone back toward the ceiling to skip to the next card.",
                  style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
                )
              }
            }
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(StripiRounded.md)
                .background(StripiCanvasSoft)
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(StripiPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = StripiOnPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Touch Screen Controls",
                  style = StripiTypeTokens.headingSm.copy(color = StripiInk)
                )
                Text(
                  text = "Tap on-screen Pass (Skip) or Correct (+1) buttons during play.",
                  style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
                )
              }
            }
          }
        }
      }

      // 2. Mode Rules Card
      StripiCreamCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when (gameMode) {
              "hit_the_beat" -> Icons.Default.MusicNote
              "mzansi_30_seconds" -> Icons.Default.RecordVoiceOver
              "my_reaction" -> Icons.Default.Face
              else -> Icons.AutoMirrored.Filled.DirectionsRun
            },
            contentDescription = null,
            tint = StripiInk,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when (gameMode) {
              "hit_the_beat" -> "Hit the Beat Mode Rules"
              "mzansi_30_seconds" -> "30-Seconds Verbal Blitz Rules"
              "my_reaction" -> "My Reaction Mode Rules"
              else -> "Act It Out Mode Rules"
            },
            style = StripiTypeTokens.headingSm.copy(color = StripiInk)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = when (gameMode) {
            "hit_the_beat" -> "Dance Amapiano, 3-Step, or Bacardi and hum the rhythm. No singing lyrics!"
            "mzansi_30_seconds" -> "Describe rapidly before the 30s buzzer. Never say the Forbidden Root Words on the card!"
            "my_reaction" -> "Point to ranks 1 to 5 with your face only. Zero spoken words or physical sound!"
            else -> "Pure silent physical mime and SASL gestures. Zero talking or noise allowed."
          },
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
        )
      }

      // 3. Warmup Accordion
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

      // 4. Primary CTA Button
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
