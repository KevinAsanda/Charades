## 2026-11-12T21:47:02Z
You are Worker M2 for Milestone 2 (Main Screen 2-Column Deck Grid Redesign).

Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
Your state folder: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\worker_m2

MANDATORY FIRST STEP:
Read ORIGINAL_REQUEST.md at:
c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\ORIGINAL_REQUEST.md

Also read:
- PROJECT.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\orchestrator_1\PROJECT.md
- SCOPE.md: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\sub_orch_m2\SCOPE.md
- Explorer 1 handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m2_1\handoff.md
- Explorer 2 handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m2_2\handoff.md
- Explorer 3 handoff: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\explorer_m2_3\handoff.md

WRITE OWNERSHIP:
You own EXCLUSIVELY:
`app/src/main/java/com/example/ui/LobbyScreen.kt`
Do NOT edit or touch any other source files.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Your mission:
Implement the complete redesign of `app/src/main/java/com/example/ui/LobbyScreen.kt` to match the reference screenshot (`media_1794515064020_678066d7.jpg`) and all requirements:
1. Compact top header bar labeled "Decks":
   - Height ~52dp.
   - Background `Color(0xFF142B68)` seamlessly matching canvas.
   - Bold white centered "Decks" title.
   - Brand emblem on left, coin balance pill on right with store shortcut.
2. Full-bleed 2-column vertical grid:
   - `LazyVerticalGrid(columns = GridCells.Fixed(2))` with `contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp)` (96dp bottom padding ensures clearance over the bottom navigation bar).
   - Horizontal and vertical spacing between cells = 10dp.
3. Grid cell layout & positioning:
   - Row 0, Column 0: First deck (`deck_trending_apps`).
   - Row 0, Column 1: "Unlock All Decks" gold card (`Color(0xFFF59E0B)`), with padlock icon, title, description, and direct Store CTA.
   - Row 1..N: All remaining core and expansion decks.
4. Deck card anatomy (`DeckGridCard`):
   - Uniform height: 215dp.
   - Corners: `RoundedCornerShape(18.dp)`.
   - Distinct vivid solid background color per deck as defined in Explorer 1 & 2 reports.
   - Top-right corner badge: "FREE", "OWNED", or lock icon.
   - Centered circular container (50dp, `Color.White.copy(alpha = 0.22f)`) holding 26-28dp white vector icon (`deckIconForId`).
   - Centered bold title (15-16sp, max 2 lines, ellipsis).
   - Centered concise description (11-11.5sp, 88-90% white opacity, max 3 lines, ellipsis).
5. "Unlock All Decks" card anatomy (`UnlockAllDecksCard`):
   - Uniform height 215dp, 18dp rounded corners, amber/gold `Color(0xFFF59E0B)`.
   - Padlock icon inside 50dp circular container.
   - Bold title "Unlock All Decks" (or "All Decks Unlocked" if already owned).
   - Description and Store CTA on click.
6. Interactive `DeckQuickPlayDialog`:
   - Triggered when tapping an unlocked deck.
   - Displays deck icon, title, card count, close button.
   - 4 game modes: `Act It Out`, `Hit the Beat`, `30-Seconds`, `Heads-Up Tilt`.
   - Round duration selector pills: `30s`, `60s`, `90s`.
   - "Start Playing" pill button with `testTag("start_match_button")`.
   - On tap: calls `onStartGame(deck, mode, duration)` and dismisses modal.
   - Tapping locked expansion deck calls `onNavigateToStore()`.
7. Public signature:
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
   Zero AI features or dead parameters (`onNavigateToStudio` removed).
