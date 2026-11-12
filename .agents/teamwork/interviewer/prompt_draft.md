Redesign the main screen of Lekker Charades into a clean, professional, and easy-to-use 2-column deck grid layout based on the reference screenshot, remove all visible AI features, automatically switch to landscape orientation during active gameplay, and assemble a verified local APK build.

Working directory: c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades
Integrity mode: development

Reference material:
- Reference screenshot: C:/Users/kevin/.gemini/antigravity/brain/d5830d68-8241-4496-a4c4-ec65c258cf7b/.user_uploaded/media_1794515064020_678066d7.jpg

## Requirements

### R1. Implementation Plan Approval
Create a detailed implementation plan artifact outlining the UI redesign, gameplay orientation handling, and local build verification steps. Obtain explicit user approval on the plan before making code modifications or running modifying commands.

### R2. Main Screen Deck Grid Redesign
Redesign the main screen (`LobbyScreen.kt` in the Play tab) to match the layout and visual structure of the reference screenshot:
- A compact top header bar labeled "Decks".
- A full-bleed 2-column vertical grid displaying deck cards.
- An "Unlock All Decks" lock card at the top of the right column.
- Each deck card displays a corner badge/indicator, centered icon or artwork, centered title, and a concise 2-3 line description, adapting existing app decks and categories to this visual structure.
- Adhere to the Stripi design system (`Color.kt`, `Type.kt`, `StripiComponents.kt`) for a clean, intentional, and professional appearance without cluttered elements.
- Maintain the 3-tab navigation structure: Play, Store, and Profile.

### R3. Feature Simplification and AI Removal
Strip down the app to only playing the game, the store, and the profile. Ensure AI coach, studio, and all other AI features are completely removed from visibility. Update logos and relevant branding assets so the app appears clean and professional.

### R4. Automatic Gameplay Landscape Orientation
Ensure that active gameplay (`GamePlayScreen.kt`) automatically rotates and locks to landscape orientation when playing the game, and restores standard orientation upon exiting gameplay.

### R5. Local Build Verification
Configure the necessary Gradle build environment tooling and compile a debug APK locally to verify that the project builds cleanly without errors.

## Acceptance Criteria

### Planning
- [ ] A detailed implementation plan artifact is created and approved before code changes or modifying commands are executed.

### Main Screen UI
- [ ] Main Play screen displays a compact top header bar with "Decks".
- [ ] Main Play screen displays a 2-column vertical card grid with the "Unlock All Decks" card positioned at the top of the right column.
- [ ] Deck cards show a corner badge/indicator, centered icon/art, title, and 2-3 line description adapted from existing app decks.
- [ ] Navigation is restricted to Play, Store, and Profile tabs.

### Feature Stripping
- [ ] Zero visible entry points or UI elements for AI coach, AI studio, or related AI features exist in the app.
- [ ] App branding and logos are clean, consistent, and professional across Play, Store, and Profile screens.

### Orientation
- [ ] Navigating into active gameplay triggers landscape orientation automatically.
- [ ] Exiting gameplay restores normal screen orientation.

### Build Verification
- [ ] Gradle build executes locally and outputs a valid compiled APK without compilation or packaging errors.
