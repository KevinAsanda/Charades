# Scope: Milestone 2 — Main Screen 2-Column Deck Grid Redesign

## Architecture
- **Target File**: `app/src/main/java/com/example/ui/LobbyScreen.kt` (exclusive write ownership by M2 Worker).
- **Design Tokens**: `Color.kt`, `Type.kt`, `StripiComponents.kt`.
- **Layout**:
  - Compact header bar (~52dp) labeled "Decks" (bold white centered title, royal blue `#142B68` background, emblem left, coin shortcut right).
  - Full-bleed 2-column vertical grid (`LazyVerticalGrid(columns = GridCells.Fixed(2))`).
  - "Unlock All Decks" gold card (`#F59E0B`) at Row 0, Column 1 with padlock icon, title, description, and direct Store CTA.
  - Deck cards (215dp height) with top-right corner badge (`"FREE"`, `"OWNED"`, or lock), centered artwork icon in 50dp soft circular container, bold title, and concise 2-3 line description with ellipsis.
  - Quick Play modal (`DeckQuickPlayDialog`) on unlocked deck click, letting the player pick game mode (`Act It Out`, `Hit the Beat`, `30-Seconds`, `Heads-Up Tilt`) and round duration (`30s`, `60s`, `90s`) with a prominent "Start Playing" button (`testTag("start_match_button")`).
  - Signature cleanup: No `onNavigateToStudio` parameter.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Compact Decks Header Bar | Compact ~52dp header with bold white "Decks" title seamlessly blended into canvas | M2 | Survey (Explorer 1) |
| 2 | 2-Column Full-Bleed Grid | Full-bleed 2-column vertical deck grid with 12dp horizontal and 10dp vertical spacing | M2 | Survey (Explorer 1) |
| 3 | "Unlock All Decks" Anchor Card | Amber/gold anchor card at Row 0, Column 1 with padlock icon, title, and direct Store CTA | M2 | Survey (Explorer 1) |
| 4 | Deck Grid Card Anatomy | Uniform 215dp height cards with corner badge, circular artwork container, bold title, and 2-3 line description | M2 | Survey (Explorer 1) |
| 5 | Deck Quick Play Modal | Lightweight mode selection and timer pill selector before launching game with `start_match_button` testTag | M2 | Survey (Explorer 1) |
| 6 | Interface Signature Cleanliness | Clean `LobbyScreen` signature removing dead `onNavigateToStudio` parameter | M2 | Survey (Explorer 1 & 2) |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M2 | Main Screen 2-Column Deck Grid Redesign | Complete redesign of `LobbyScreen.kt` | M1 (Done) | DONE |

## Interface Contracts
### LobbyScreen ↔ MainFeatureScreen
```kotlin
@Composable
fun LobbyScreen(
  userProfile: UserProfile?,
  customCards: List<CustomCard> = emptyList(),
  onStartGame: (Deck, String, Long) -> Unit,
  onNavigateToStore: () -> Unit,
  onOpenPaymentSetup: () -> Unit = {}
)
```
- Dead parameter `onNavigateToStudio` removed.
- `onStartGame`: triggered when user selects a deck, chooses game mode & duration in `DeckQuickPlayDialog`, and taps "Start Playing" (`testTag("start_match_button")`).
- `onNavigateToStore`: triggered when tapping "Unlock All Decks" anchor card or any locked expansion deck.
