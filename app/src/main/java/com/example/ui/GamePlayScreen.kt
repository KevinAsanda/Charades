package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.Deck
import com.example.data.model.GameSession
import com.example.ui.components.StripiCreamCard
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
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTypeTokens
import kotlinx.coroutines.delay
import kotlin.math.abs

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
  var showHints by remember { mutableStateOf(false) }

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

  // Pre-game "how to tilt" guide shown once before the first countdown
  var isGuideShown by remember { mutableStateOf(false) }

  // Full-screen flash feedback ("correct" / "pass") so tilting gives instant visual confirmation
  var feedbackKind by remember { mutableStateOf<String?>(null) }
  var feedbackTick by remember { mutableIntStateOf(0) }

  LaunchedEffect(feedbackTick) {
    if (feedbackTick > 0) {
      delay(650)
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

  fun triggerHaptic(success: Boolean) {
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
          vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 80), -1))
        }
      }
    } catch (_: Exception) {}
  }

  fun advanceOrCompleteTurn() {
    if (currentCardIndex + 1 < cards.size) {
      currentCardIndex++
    } else if (gameMode == "party_teams" && activeTeam == "Team A") {
      // Reshuffle for Team B and enter intermission
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
    triggerHaptic(true)
    feedbackKind = "correct"
    feedbackTick++
    advanceOrCompleteTurn()
  }

  fun onPass() {
    if (!isPlaying || isGameOver || isTeamIntermission || currentCardIndex >= cards.size) return
    val card = cards[currentCardIndex]
    passedWords.add(card.term)
    triggerHaptic(false)
    feedbackKind = "pass"
    feedbackTick++
    advanceOrCompleteTurn()
  }

  // Tilt Sensor Listener for Heads-Up Mode:
  // Requires returning to vertical neutral position (abs(z) < 4.0f) on the forehead before arming,
  // so holding the phone flat in hand never auto-skips cards.
  DisposableEffect(gameMode, isPlaying) {
    if (isPlaying && sensorManager != null) {
      val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          val now = System.currentTimeMillis()
          val z = event.values[2]

          // Arm the tilt sensor once the phone is held roughly upright (neutral zone)
          if (abs(z) < 5.0f) {
            isTiltArmed = true
            return
          }

          if (!isTiltArmed || now - lastTiltTime < 1100L) return

          // Tilting forward/down (screen faces floor, z < -7) = Correct;
          // tilting back/up (screen faces ceiling, z > 7) = Pass
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
        triggerHaptic(false)
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
            text = when (gameMode) {
              "heads_up" -> "Forehead Tilt Mode"
              "party_teams" -> "$activeTeam Turn"
              else -> "Quick Solo Blitz"
            }
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = when (gameMode) {
              "heads_up" -> "Place phone upright on forehead"
              "party_teams" -> "$activeTeam: Get ready to act"
              else -> "Get ready to act"
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
      // Game Over Summary Screen
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
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 16.dp)
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
              Spacer(modifier = Modifier.height(10.dp))
              StripiSoftPillTag(
                text = "+${10 + (guessedWords.size * 5)} Coins Earned",
                containerColor = StripiPrimary,
                contentColor = StripiOnPrimary
              )
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
              Spacer(modifier = Modifier.height(8.dp))
              StripiSoftPillTag(
                text = "+${10 + (guessedWords.size * 5)} Coins Earned",
                containerColor = StripiPrimary,
                contentColor = StripiOnPrimary
              )
            }

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
      // Full-screen Active Gameplay View: no buttons, tilt controls the game
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

          // Giant prompt stage (or instant Correct / Pass feedback)
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            if (feedbackKind != null) {
              Text(
                text = if (feedbackKind == "correct") "Correct!" else "Pass",
                style = StripiTypeTokens.displayXxl.copy(color = StripiOnPrimary),
                textAlign = TextAlign.Center
              )
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
              ) {
                StripiSoftPillTag(
                  text = currentCard?.category.orEmpty(),
                  containerColor = StripiPrimary,
                  contentColor = StripiOnPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                  text = currentCard?.term.orEmpty(),
                  style = StripiTypeTokens.displayXxl.copy(color = StripiOnPrimary),
                  textAlign = TextAlign.Center
                )
                if (!currentCard?.culturalTrivia.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(14.dp))
                  Text(
                    text = currentCard?.culturalTrivia.orEmpty(),
                    style = StripiTypeTokens.bodyMd.copy(
                      color = StripiSubduedHover.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
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
                text = "Silent Acting Clues",
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

          if (hasAccelerometer) {
            // Small tilt guide pinned to the bottom edge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TiltHintPill(icon = Icons.Default.ArrowDownward, text = "Tilt down: Correct")
              TiltHintPill(
                icon = Icons.Default.PhoneAndroid,
                text = if (isTiltArmed) "Ready" else "Hold upright",
                highlighted = isTiltArmed
              )
              TiltHintPill(icon = Icons.Default.ArrowUpward, text = "Tilt up: Pass")
            }
          } else {
            // Fallback for devices without an accelerometer so the game stays playable
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              StripiSecondaryPillButton(
                text = "Pass",
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
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
                text = "Correct",
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
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
      .padding(horizontal = 12.dp, vertical = 7.dp),
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
  val isHeadsUp = gameMode == "heads_up"
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
      StripiSoftPillTag(text = "How to play")
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Tilt to answer.",
        style = StripiTypeTokens.displayLg.copy(color = StripiInk),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = deckName,
        style = StripiTypeTokens.bodyLg.copy(color = StripiInkSecondary)
      )
      Spacer(modifier = Modifier.height(24.dp))

      if (hasAccelerometer) {
        TiltGuideStep(
          icon = Icons.Default.PhoneAndroid,
          title = if (isHeadsUp) "1. Phone on your forehead" else "1. Hold the phone upright",
          body = if (isHeadsUp) {
            "Hold it upright with the screen facing your friends. They act, you guess."
          } else {
            "Keep the screen facing you while you act the word out for your team."
          }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TiltGuideStep(
          icon = Icons.Default.ArrowDownward,
          title = "2. Tilt DOWN = Correct",
          body = "Nod the phone forward until the screen faces the floor when the word is guessed."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TiltGuideStep(
          icon = Icons.Default.ArrowUpward,
          title = "3. Tilt UP = Pass",
          body = "Tip the phone back until the screen faces the ceiling to skip a tough word."
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Return the phone upright between cards to get the next word ready.",
          style = StripiTypeTokens.caption.copy(color = StripiInkSecondary),
          textAlign = TextAlign.Center
        )
      } else {
        TiltGuideStep(
          icon = Icons.Default.Check,
          title = "No tilt sensor found",
          body = "This device has no accelerometer, so Correct and Pass buttons will appear on screen."
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
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
