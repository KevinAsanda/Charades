package com.example.ui

import android.content.Context
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Deck
import com.example.data.model.GameSession
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiEmerald
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import kotlinx.coroutines.delay

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

  val cards = remember { deck.cards.shuffled() }
  var currentCardIndex by remember { mutableIntStateOf(0) }
  var timeLeft by remember { mutableLongStateOf(roundDurationSeconds) }
  var isPlaying by remember { mutableStateOf(false) }
  var isCountdownReady by remember { mutableStateOf(true) }
  var readyCountdown by remember { mutableIntStateOf(3) }
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

  fun onCorrect() {
    if (!isPlaying || isGameOver || currentCardIndex >= cards.size) return
    val card = cards[currentCardIndex]
    guessedWords.add(card.term)
    if (activeTeam == "Team A") teamAScore++ else teamBScore++
    triggerHaptic(true)

    if (currentCardIndex + 1 < cards.size) {
      currentCardIndex++
    } else {
      isGameOver = true
    }
  }

  fun onPass() {
    if (!isPlaying || isGameOver || currentCardIndex >= cards.size) return
    val card = cards[currentCardIndex]
    passedWords.add(card.term)
    triggerHaptic(false)

    if (currentCardIndex + 1 < cards.size) {
      currentCardIndex++
    } else {
      isGameOver = true
    }
  }

  // Tilt Sensor Listener for Heads-Up Mode
  DisposableEffect(gameMode, isPlaying) {
    if (gameMode == "heads_up" && isPlaying && sensorManager != null) {
      val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          val now = System.currentTimeMillis()
          if (now - lastTiltTime < 1200L) return

          val z = event.values[2]
          if (z > 7.5f) {
            lastTiltTime = now
            onCorrect()
          } else if (z < -7.5f) {
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
        sensorManager.unregisterListener(listener)
      }
    } else {
      onDispose {}
    }
  }

  // Ready 3-2-1 countdown
  LaunchedEffect(isCountdownReady) {
    if (isCountdownReady) {
      while (readyCountdown > 0) {
        delay(1000)
        readyCountdown--
      }
      isCountdownReady = false
      isPlaying = true
    }
  }

  // Round timer countdown
  LaunchedEffect(isPlaying, isGameOver) {
    if (isPlaying && !isGameOver) {
      while (timeLeft > 0 && !isGameOver) {
        delay(1000)
        timeLeft--
      }
      if (timeLeft <= 0) {
        isGameOver = true
        isPlaying = false
        triggerHaptic(false)
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = StripiCanvas
  ) {
    if (isCountdownReady) {
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
            text = if (gameMode == "heads_up") "Forehead Tilt Mode" else "Party Match Mode"
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = if (gameMode == "heads_up") "Place phone on forehead" else "Get ready to act",
            style = MaterialTheme.typography.headlineLarge.copy(
              color = StripiInk,
              fontWeight = FontWeight.Light
            ),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = deck.name,
            style = MaterialTheme.typography.titleMedium.copy(color = StripiInkSecondary)
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
    } else if (isGameOver) {
      // Game Over Summary Screen
      val totalScore = guessedWords.size.toLong()
      val session = remember {
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

      LaunchedEffect(Unit) {
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
            text = "Round Complete.",
            style = MaterialTheme.typography.displayMedium.copy(
              color = StripiInk,
              fontWeight = FontWeight.Light
            )
          )
          Text(
            text = deck.name,
            style = MaterialTheme.typography.bodyMedium.copy(color = StripiInkMute)
          )
        }

        // Featured Dark Score Telemetry Card
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
                  Text("Team A", color = StripiSubduedHover, style = MaterialTheme.typography.labelMedium)
                  StripiTabularText(
                    text = "$teamAScore",
                    color = StripiOnPrimary,
                    fontSize = 36,
                    fontWeight = FontWeight.Light
                  )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("Team B", color = StripiSubduedHover, style = MaterialTheme.typography.labelMedium)
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
                text = "WORDS GUESSED",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = StripiSubduedHover,
                  letterSpacing = 0.8.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              StripiTabularText(
                text = "${guessedWords.size}",
                color = StripiOnPrimary,
                fontSize = 48,
                fontWeight = FontWeight.Light
              )
              Spacer(modifier = Modifier.height(6.dp))
              StripiSoftPillTag(
                text = "+${10 + (guessedWords.size * 5)} Gold Coins Earned",
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
                fontWeight = FontWeight.Medium
              )
              StripiTabularText(
                text = "Passed: ${passedWords.size}",
                color = StripiRuby,
                fontSize = 14,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Action Pill Buttons
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
              currentCardIndex = 0
              timeLeft = roundDurationSeconds
              guessedWords.clear()
              passedWords.clear()
              teamAScore = 0
              teamBScore = 0
              readyCountdown = 3
              isCountdownReady = true
              isGameOver = false
            }
          )
        }
      }
    } else {
      // Active Gameplay View
      val currentCard = cards.getOrNull(currentCardIndex)

      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(StripiCanvasSoft)
      ) {
        // Top Header Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(StripiCanvas)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          IconButton(onClick = onExit) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Exit Game",
              tint = StripiInk
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = if (timeLeft <= 10) StripiRuby else StripiPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            StripiTabularText(
              text = "${timeLeft}s",
              color = if (timeLeft <= 10) StripiRuby else StripiInk,
              fontSize = 20,
              fontWeight = FontWeight.Medium
            )
          }

          StripiSoftPillTag(
            text = "Score: ${guessedWords.size}"
          )
        }

        val progress = (timeLeft.toFloat() / roundDurationSeconds.toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
          color = if (timeLeft <= 10) StripiRuby else StripiPrimary,
          trackColor = StripiHairline
        )

        // Main Prompt Stage Card (Deep Navy #1C1E54 for crisp legibility across the room)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          StripiFeaturedDarkCard(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                StripiSoftPillTag(
                  text = currentCard?.category.orEmpty(),
                  containerColor = StripiPrimary,
                  contentColor = StripiOnPrimary
                )
                StripiSoftPillTag(
                  text = currentCard?.culturalEra.orEmpty(),
                  containerColor = StripiSubduedHover,
                  contentColor = StripiBrandDark900
                )
              }

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
              ) {
                Text(
                  text = currentCard?.term.orEmpty(),
                  style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = StripiOnPrimary,
                    fontSize = 34.sp,
                    lineHeight = 40.sp
                  ),
                  textAlign = TextAlign.Center
                )

                if (!currentCard?.culturalTrivia.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = currentCard?.culturalTrivia.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = StripiSubduedHover.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                  )
                }
              }

              // Acting Clues toggle
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
              ) {
                IconButton(onClick = { showHints = !showHints }) {
                  Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Toggle Hints",
                    tint = if (showHints) StripiSubduedHover else StripiOnPrimary.copy(alpha = 0.5f)
                  )
                }
                AnimatedVisibility(visible = showHints) {
                  StripiCreamCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp)
                  ) {
                    Text(
                      text = "Silent Acting Clues:",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = StripiInk
                      )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    currentCard?.hints?.forEach { hint ->
                      Text(
                        text = "• $hint",
                        style = MaterialTheme.typography.bodySmall.copy(color = StripiInkSecondary)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Bottom Controls (Pill Shaped)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(StripiCanvas)
            .border(1.dp, StripiHairline)
            .padding(horizontal = 20.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Button(
            onClick = { onPass() },
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("pass_card_button"),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
              containerColor = StripiRuby,
              contentColor = StripiOnPrimary
            )
          ) {
            Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pass Card", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
          }

          Button(
            onClick = { onCorrect() },
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("correct_card_button"),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
              containerColor = StripiPrimary,
              contentColor = StripiOnPrimary
            )
          ) {
            Icon(Icons.Default.Check, contentDescription = "Correct", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Correct!", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
          }
        }
      }
    }
  }
}
