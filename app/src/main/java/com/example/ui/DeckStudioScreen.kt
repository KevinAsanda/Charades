package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CustomCard
import com.example.data.repository.AiRepository
import com.example.data.repository.CardImageSize
import com.example.data.repository.GeneratedDeck
import com.example.ui.components.StripiCreamCard
import com.example.ui.components.StripiFeatureCardLight
import com.example.ui.components.StripiFeaturedDarkCard
import com.example.ui.components.StripiGradientMeshHero
import com.example.ui.components.StripiPrimaryPillButton
import com.example.ui.components.StripiRounded
import com.example.ui.components.StripiSecondaryPillButton
import com.example.ui.components.StripiSoftPillTag
import com.example.ui.components.StripiTabularText
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
import com.example.ui.theme.StripiTypeTokens
import kotlinx.coroutines.launch

@Composable
fun DeckStudioScreen(
  aiRepository: AiRepository,
  customCards: List<CustomCard>,
  onAddCard: (CustomCard) -> Unit,
  onDeleteCard: (String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Deck Maker, 1: Card Art (1K/2K/4K), 2: My Cards

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(StripiCanvas)
  ) {
    StripiGradientMeshHero(
      contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp)
    ) {
      StripiSoftPillTag(text = "Mzansi Creative Studio")
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Custom Decks & Card Art.",
        style = StripiTypeTokens.displayLg.copy(color = StripiInk)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Generate structured decks with gemini-3.8-flash and 1K, 2K, or 4K artwork with gemini-3-pro-image-preview.",
        style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
      )

      Spacer(modifier = Modifier.height(18.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(StripiRounded.pill)
          .background(StripiCanvasSoft)
          .border(1.dp, StripiHairline, StripiRounded.pill)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        StudioTabPill(
          title = "AI Deck Maker",
          isSelected = selectedTab == 0,
          modifier = Modifier.weight(1f),
          onClick = { selectedTab = 0 }
        )
        StudioTabPill(
          title = "Art (1K/2K/4K)",
          isSelected = selectedTab == 1,
          modifier = Modifier.weight(1f),
          onClick = { selectedTab = 1 }
        )
        StudioTabPill(
          title = "Cards (${customCards.size})",
          isSelected = selectedTab == 2,
          modifier = Modifier.weight(1f),
          onClick = { selectedTab = 2 }
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
      when (selectedTab) {
        0 -> AiDeckGeneratorTab(aiRepository, onAddCard)
        1 -> AiCardArtGeneratorTab(aiRepository)
        2 -> MyCustomCardsTab(customCards, onAddCard, onDeleteCard)
      }
    }
  }
}

@Composable
private fun StudioTabPill(
  title: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .clip(StripiRounded.pill)
      .clickable { onClick() },
    shape = StripiRounded.pill,
    color = if (isSelected) StripiPrimary else StripiCanvasSoft
  ) {
    Text(
      text = title,
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
      textAlign = TextAlign.Center,
      style = StripiTypeTokens.caption.copy(
        color = if (isSelected) StripiOnPrimary else StripiInkSecondary
      )
    )
  }
}

@Composable
private fun AiDeckGeneratorTab(
  aiRepository: AiRepository,
  onAddCard: (CustomCard) -> Unit
) {
  val scope = rememberCoroutineScope()
  var themeInput by remember { mutableStateOf("") }
  var isGenerating by remember { mutableStateOf(false) }
  var generatedDeck by remember { mutableStateOf<GeneratedDeck?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var savedNotification by remember { mutableStateOf<String?>(null) }

  val sampleThemes = listOf(
    "Durban July 2000s High Fashion",
    "Soweto Derby Legends 2002",
    "SABC 2000s Kids Cartoons",
    "Cape Town Tweede Nuwejaar",
    "Sunday Jazz & Braai Anthems"
  )

  fun generateDeck(theme: String) {
    if (theme.isBlank() || isGenerating) return
    isGenerating = true
    errorMessage = null
    savedNotification = null
    generatedDeck = null

    scope.launch {
      try {
        val result = aiRepository.generateThemedDeck(theme)
        generatedDeck = result
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Failed to generate deck"
      } finally {
        isGenerating = false
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
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(22.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = StripiPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Generate Custom Themed Deck",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Uses gemini-3.8-flash structured JSON schema to build 6 playable Mzansi cards.",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
        )

        Spacer(modifier = Modifier.height(16.dp))

        StripiTextInput(
          value = themeInput,
          onValueChange = { themeInput = it },
          label = "Deck Theme",
          placeholder = "e.g. 2000s Soapie Dramas, Township Car Culture",
          modifier = Modifier.testTag("theme_input_field")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Suggested Mzansi themes",
          style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scroll-snap pills (Section 4.9)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(sampleThemes) { theme ->
            val selected = themeInput == theme
            Surface(
              shape = StripiRounded.pill,
              color = if (selected) StripiPrimary else StripiCanvasSoft,
              border = BorderStroke(1.dp, if (selected) StripiPrimary else StripiHairline),
              modifier = Modifier.clickable {
                themeInput = theme
                generateDeck(theme)
              }
            ) {
              Text(
                text = theme,
                style = StripiTypeTokens.caption.copy(
                  color = if (selected) StripiOnPrimary else StripiInk
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        StripiPrimaryPillButton(
          text = if (isGenerating) "Creating Deck..." else "Generate Themed Deck",
          enabled = themeInput.isNotBlank() && !isGenerating,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("generate_deck_button"),
          leadingIcon = {
            if (isGenerating) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = StripiOnPrimary,
                strokeWidth = 2.dp
              )
            }
          },
          onClick = { generateDeck(themeInput) }
        )
      }
    }

    if (errorMessage != null) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Error: $errorMessage",
            style = StripiTypeTokens.bodyMd.copy(color = StripiRuby)
          )
        }
      }
    }

    if (savedNotification != null) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = savedNotification.orEmpty(),
            style = StripiTypeTokens.bodyMd.copy(color = StripiInk)
          )
        }
      }
    }

    generatedDeck?.let { deck ->
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = deck.title,
              style = StripiTypeTokens.headingLg.copy(color = StripiInk)
            )
            Text(
              text = deck.description,
              style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          StripiPrimaryPillButton(
            text = "Save All",
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = StripiOnPrimary,
                modifier = Modifier.size(16.dp)
              )
            },
            onClick = {
              deck.cards.forEach { card ->
                onAddCard(
                  CustomCard(
                    term = card.term,
                    category = card.category,
                    hints = card.hints,
                    culturalEra = card.culturalEra
                  )
                )
              }
              savedNotification = "Saved all ${deck.cards.size} cards to your personal collection."
            }
          )
        }
      }

      items(deck.cards) { card ->
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
              text = card.term,
              style = StripiTypeTokens.headingSm.copy(color = StripiInk)
            )
            StripiSoftPillTag(text = card.category)
          }
          Spacer(modifier = Modifier.height(6.dp))
          card.hints.forEach { hint ->
            Text(
              text = "- $hint",
              style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
            )
          }
          if (card.culturalTrivia.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Trivia: ${card.culturalTrivia}",
              style = StripiTypeTokens.caption.copy(color = StripiInkMute)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AiCardArtGeneratorTab(aiRepository: AiRepository) {
  val scope = rememberCoroutineScope()
  var imagePrompt by remember { mutableStateOf("") }
  var selectedSizeOption by remember { mutableStateOf(CardImageSize.SIZE_1K) }
  var isGeneratingImage by remember { mutableStateOf(false) }
  var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val sampleImagePrompts = listOf(
    "Vintage 2000s Kwaito cassette tape with colorful township graffiti",
    "Golden 2010 World Cup soccer ball with bright plastic vuvuzelas",
    "Retro South African tuckshop with Chappies bubblegum jars and Kotas",
    "Sophie Ndaba as Queen Moroka in high-glam 2000s soapie party outfit"
  )

  fun generateImage() {
    if (imagePrompt.isBlank() || isGeneratingImage) return
    isGeneratingImage = true
    errorMessage = null
    generatedBitmap = null

    scope.launch {
      try {
        val bitmap = aiRepository.generateCardArt(imagePrompt, selectedSizeOption)
        if (bitmap != null) {
          generatedBitmap = bitmap
        } else {
          errorMessage = "No image was returned by the model."
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Image generation failed"
      } finally {
        isGeneratingImage = false
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
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(22.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = StripiPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Generate High-Resolution Card Art",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Powered by gemini-3-pro-image-preview with selectable 1K, 2K, and 4K output sizes.",
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
        )

        Spacer(modifier = Modifier.height(16.dp))

        StripiTextInput(
          value = imagePrompt,
          onValueChange = { imagePrompt = it },
          label = "Artwork Prompt",
          placeholder = "e.g. Kwaito boombox under Johannesburg skyline",
          modifier = Modifier.testTag("image_prompt_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Image Resolution",
          style = StripiTypeTokens.caption.copy(color = StripiInkSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          CardImageSize.values().forEach { sizeOption ->
            val selected = selectedSizeOption == sizeOption
            Surface(
              shape = StripiRounded.pill,
              color = if (selected) StripiPrimary else StripiCanvasSoft,
              border = BorderStroke(
                1.dp,
                if (selected) StripiPrimary else StripiHairline
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedSizeOption = sizeOption }
            ) {
              Text(
                text = sizeOption.label,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                textAlign = TextAlign.Center,
                style = StripiTypeTokens.caption.copy(
                  color = if (selected) StripiOnPrimary else StripiInk
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Prompt inspiration",
          style = StripiTypeTokens.caption.copy(color = StripiInkMute)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(sampleImagePrompts) { sample ->
            Surface(
              shape = StripiRounded.pill,
              color = StripiCanvasSoft,
              border = BorderStroke(1.dp, StripiHairline),
              modifier = Modifier.clickable { imagePrompt = sample }
            ) {
              Text(
                text = sample,
                style = StripiTypeTokens.caption.copy(color = StripiInk),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        StripiPrimaryPillButton(
          text = if (isGeneratingImage) {
            "Synthesizing..."
          } else {
            "Generate Art (${selectedSizeOption.name.replace("SIZE_", "")})"
          },
          enabled = imagePrompt.isNotBlank() && !isGeneratingImage,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("generate_image_button"),
          leadingIcon = {
            if (isGeneratingImage) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = StripiOnPrimary,
                strokeWidth = 2.dp
              )
            }
          },
          onClick = { generateImage() }
        )
      }
    }

    if (errorMessage != null) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Error: $errorMessage",
            style = StripiTypeTokens.bodyMd.copy(color = StripiRuby)
          )
        }
      }
    }

    generatedBitmap?.let { bitmap ->
      item {
        StripiFeaturedDarkCard(modifier = Modifier.fillMaxWidth()) {
          StripiSoftPillTag(
            text = "Generated Artwork (${selectedSizeOption.name.replace("SIZE_", "")})",
            containerColor = StripiPrimary,
            contentColor = StripiOnPrimary
          )
          Spacer(modifier = Modifier.height(12.dp))
          Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Generated South African Card Art",
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1f)
              .clip(StripiRounded.md),
            contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = imagePrompt,
            style = StripiTypeTokens.bodyMd.copy(color = StripiSubduedHover)
          )
        }
      }
    }
  }
}

@Composable
private fun MyCustomCardsTab(
  customCards: List<CustomCard>,
  onAddCard: (CustomCard) -> Unit,
  onDeleteCard: (String) -> Unit
) {
  var showNewCardDialog by remember { mutableStateOf(false) }
  var term by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Music & Slang") }
  var hints by remember { mutableStateOf("") }
  var culturalEra by remember { mutableStateOf("Early 2000s") }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Personal Mzansi Cards",
            style = StripiTypeTokens.headingMd.copy(color = StripiInk)
          )
          StripiTabularText(
            text = "${customCards.size} cards synced to Firestore",
            color = StripiInkMute,
            fontSize = 14
          )
        }

        StripiPrimaryPillButton(
          text = "Add Card",
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = StripiOnPrimary,
              modifier = Modifier.size(16.dp)
            )
          },
          onClick = { showNewCardDialog = true }
        )
      }
    }

    if (customCards.isEmpty()) {
      item {
        StripiCreamCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Your custom deck is empty",
            style = StripiTypeTokens.headingSm.copy(color = StripiInk)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Use the AI Deck Maker tab or tap Add Card to build your own South African charades cards.",
            style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
          )
        }
      }
    }

    items(customCards) { card ->
      StripiFeatureCardLight(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = card.term,
                style = StripiTypeTokens.headingSm.copy(color = StripiInk)
              )
              Spacer(modifier = Modifier.width(8.dp))
              StripiSoftPillTag(text = card.category)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Era: ${card.culturalEra}",
              style = StripiTypeTokens.caption.copy(color = StripiInkMute)
            )
            if (card.hints.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Clues: ${card.hints.joinToString(", ")}",
                style = StripiTypeTokens.bodyMd.copy(color = StripiInkSecondary)
              )
            }
          }

          IconButton(onClick = { onDeleteCard(card.id) }) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = StripiRuby
            )
          }
        }
      }
    }
  }

  if (showNewCardDialog) {
    Dialog(onDismissRequest = { showNewCardDialog = false }) {
      Surface(
        shape = StripiRounded.xl,
        color = StripiCanvas
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Add New Mzansi Card",
            style = StripiTypeTokens.headingLg.copy(color = StripiInk)
          )

          StripiTextInput(
            value = term,
            onValueChange = { term = it },
            label = "Word or Phrase",
            placeholder = "e.g. Boet and Swaer"
          )

          StripiTextInput(
            value = category,
            onValueChange = { category = it },
            label = "Category",
            placeholder = "e.g. TV Ads, Music, Food"
          )

          StripiTextInput(
            value = hints,
            onValueChange = { hints = it },
            label = "Acting Clues (comma separated)",
            placeholder = "e.g. Kalahari heat, waving flies, broken bakkie"
          )

          StripiTextInput(
            value = culturalEra,
            onValueChange = { culturalEra = it },
            label = "Cultural Era",
            placeholder = "e.g. Early 2000s"
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StripiSecondaryPillButton(
              text = "Cancel",
              modifier = Modifier.weight(1f),
              onClick = { showNewCardDialog = false }
            )
            StripiPrimaryPillButton(
              text = "Save Card",
              modifier = Modifier.weight(1f),
              onClick = {
                if (term.isNotBlank()) {
                  val hintsList = hints.split(",").map { it.trim() }.filter { it.isNotBlank() }
                  onAddCard(
                    CustomCard(
                      term = term.trim(),
                      category = category.trim(),
                      hints = hintsList,
                      culturalEra = culturalEra.trim()
                    )
                  )
                  term = ""
                  hints = ""
                  showNewCardDialog = false
                }
              }
            )
          }
        }
      }
    }
  }
}
