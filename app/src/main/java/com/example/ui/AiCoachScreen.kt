package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AiRepository
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTextInput
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
import kotlinx.coroutines.launch

@Composable
fun AiCoachScreen(
  aiRepository: AiRepository
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Fast Slang Coach, 1: High Thinking Referee

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas)
  ) {
    StripiGradientMeshHero(
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 18.dp)
    ) {
      StripiSoftPillTag(text = "Mzansi Cultural Intelligence")
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Slang Coach & Referee.",
        style = MaterialTheme.typography.displaySmall.copy(
          color = StripiInk,
          fontWeight = FontWeight.Light
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Instant acting clues via gemini-3.1-flash-lite and deep cultural dispute adjudication via gemini-4-argon.",
        style = MaterialTheme.typography.bodySmall.copy(color = StripiInkSecondary)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Pill Segmented Switcher
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(percent = 50))
          .background(StripiCanvasSoft)
          .border(1.dp, StripiHairline, RoundedCornerShape(percent = 50))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SegmentPillTab(
          title = "Fast Slang Coach",
          isSelected = selectedTab == 0,
          modifier = Modifier.weight(1f),
          onClick = { selectedTab = 0 }
        )
        SegmentPillTab(
          title = "Cultural Referee",
          isSelected = selectedTab == 1,
          modifier = Modifier.weight(1f),
          onClick = { selectedTab = 1 }
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
      if (selectedTab == 0) {
        FastSlangCoachSection(aiRepository)
      } else {
        HighThinkingRefereeSection(aiRepository)
      }
    }
  }
}

@Composable
private fun SegmentPillTab(
  title: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(percent = 50))
      .clickable { onClick() },
    shape = RoundedCornerShape(percent = 50),
    color = if (isSelected) StripiPrimary else StripiCanvasSoft
  ) {
    Text(
      text = title,
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        color = if (isSelected) StripiOnPrimary else StripiInkSecondary
      )
    )
  }
}

@Composable
private fun FastSlangCoachSection(aiRepository: AiRepository) {
  val scope = rememberCoroutineScope()
  var inputQuery by remember { mutableStateOf("") }
  var isStreaming by remember { mutableStateOf(false) }
  var coachResponse by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val sampleSlang = listOf(
    "Izikhothane Custard Battle",
    "Nkalakatha",
    "Voetstoots",
    "Eish!",
    "Skedonk",
    "Chappies Did You Know",
    "Hosh My Chommie",
    "Tata Ma Millions",
    "Spatlo / Kota",
    "Blowing Vuvuzela",
    "Lekker Braai"
  )

  fun runCoach(term: String) {
    if (term.isBlank() || isStreaming) return
    isStreaming = true
    errorMessage = null
    coachResponse = ""

    scope.launch {
      try {
        aiRepository.explainSlangAndCluesStream(term).collect { chunk ->
          coachResponse = chunk
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Coach explanation failed"
      } finally {
        isStreaming = false
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      StripiFeatureCardLight(
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = StripiPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Instant Charades Clues & Slang Meaning",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Medium,
              color = StripiInk
            )
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Powered by gemini-3.1-flash-lite for low-latency mime gestures and early 2000s South African context.",
          style = MaterialTheme.typography.bodySmall.copy(color = StripiInkMute)
        )

        Spacer(modifier = Modifier.height(14.dp))

        StripiTextInput(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          label = "South African Slang or Pop Culture Term",
          placeholder = "e.g. Sharp Fede, Yizo Yizo, Skedonk",
          modifier = Modifier.testTag("slang_input_field"),
          trailingIcon = {
            IconButton(
              onClick = { runCoach(inputQuery) },
              enabled = inputQuery.isNotBlank() && !isStreaming
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = StripiPrimary
              )
            }
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Quick Mzansi terms:",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            color = StripiInkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(sampleSlang) { slang ->
            val selected = inputQuery == slang
            Surface(
              shape = RoundedCornerShape(percent = 50),
              color = if (selected) StripiPrimary else StripiCanvasSoft,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selected) StripiPrimary else StripiHairline
              ),
              modifier = Modifier.clickable {
                inputQuery = slang
                runCoach(slang)
              }
            ) {
              Text(
                text = slang,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (selected) StripiOnPrimary else StripiInk
                )
              )
            }
          }
        }
      }
    }

    if (isStreaming && coachResponse.isNullOrBlank()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = StripiPrimary)
        }
      }
    }

    if (errorMessage != null) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodySmall.copy(color = StripiRuby)
          )
        }
      }
    }

    if (!coachResponse.isNullOrBlank()) {
      item {
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = StripiSubduedHover,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "COACH ANALYSIS & ACTING CUES",
              style = MaterialTheme.typography.labelSmall.copy(
                color = StripiSubduedHover,
                letterSpacing = 0.8.sp
              )
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = coachResponse.orEmpty(),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = StripiOnPrimary,
              lineHeight = 22.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun HighThinkingRefereeSection(aiRepository: AiRepository) {
  val scope = rememberCoroutineScope()
  var disputeQuery by remember { mutableStateOf("") }
  var isThinking by remember { mutableStateOf(false) }
  var refereeVerdict by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val sampleDisputes = listOf(
    "Did Mandoza Nkalakatha release in 2000 or 1999?",
    "Team B mimed blowing a vuvuzela for 1996 AFCON. Should it count?",
    "Is miming eating a Kota with a knife and fork acceptable or a foul?",
    "Generations: Was Queen Moroka in the soapie before Karabo Moroka?"
  )

  fun submitDispute(query: String) {
    if (query.isBlank() || isThinking) return
    isThinking = true
    errorMessage = null
    refereeVerdict = null

    scope.launch {
      try {
        val verdict = aiRepository.adjudicateCulturalDispute(query)
        refereeVerdict = verdict
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Referee dispute adjudication failed"
      } finally {
        isThinking = false
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      StripiFeatureCardLight(
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Gavel,
            contentDescription = null,
            tint = StripiPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Cultural Referee & Nostalgia Time Machine",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Medium,
              color = StripiInk
            )
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Settles heated party disputes using gemini-4-argon with high thinking mode (ThinkingLevel.HIGH).",
          style = MaterialTheme.typography.bodySmall.copy(color = StripiInkMute)
        )

        Spacer(modifier = Modifier.height(14.dp))

        StripiTextInput(
          value = disputeQuery,
          onValueChange = { disputeQuery = it },
          label = "Describe the Party Dispute",
          placeholder = "e.g. Did Bafana win AFCON in 1996 or 1998?",
          modifier = Modifier.testTag("referee_query_input"),
          trailingIcon = {
            IconButton(
              onClick = { submitDispute(disputeQuery) },
              enabled = disputeQuery.isNotBlank() && !isThinking
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = StripiPrimary
              )
            }
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Common Mzansi party debates:",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            color = StripiInkSecondary
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          sampleDisputes.forEach { dispute ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StripiCanvasSoft,
              border = androidx.compose.foundation.BorderStroke(1.dp, StripiHairline),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  disputeQuery = dispute
                  submitDispute(dispute)
                }
            ) {
              Text(
                text = dispute,
                style = MaterialTheme.typography.bodySmall.copy(color = StripiInk),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
              )
            }
          }
        }
      }
    }

    if (isThinking) {
      item {
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(color = StripiSubduedHover)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Referee is reasoning deeply (gemini-4-argon)...",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = StripiOnPrimary
              )
            )
            Text(
              text = "Cross-checking early 2000s South African broadcast history and township etiquette.",
              style = MaterialTheme.typography.bodySmall.copy(color = StripiSubduedHover),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    if (errorMessage != null) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodySmall.copy(color = StripiRuby)
          )
        }
      }
    }

    if (!refereeVerdict.isNullOrBlank()) {
      item {
        StripiFeaturedDarkCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Psychology,
              contentDescription = null,
              tint = StripiSubduedHover,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "OFFICIAL REFEREE VERDICT",
              style = MaterialTheme.typography.labelSmall.copy(
                color = StripiSubduedHover,
                letterSpacing = 0.8.sp
              )
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = refereeVerdict.orEmpty(),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = StripiOnPrimary,
              lineHeight = 22.sp
            )
          )
        }
      }
    }
  }
}
