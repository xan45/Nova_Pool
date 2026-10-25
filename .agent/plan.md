# Project Plan

Nova Pool

A vibe-coded pool game created as an experimental side project during free time.

Nova Pool is built from scratch for Android with a focus on modern graphics, realistic physics, smooth controls, and long-term progression systems.

Features include:
• 3D Pool & Snooker gameplay (8-Ball, 9-Ball, Snooker modes)
• Realistic ball physics & 3D rendering / simulation
• Offline progression system
• Currency and unlockables (Coins, Gems, XP, leveling)
• Cue Shop (Cues with power, spin, aim line stats, customizable visual styles)
• Local tournaments & AI bot opponents
• Player statistics & achievements
• Multiple table themes & custom cue balls / felt colors
• Material 3 Design UI integrated with game engine overlays

## Project Brief

# Nova Pool - Project Brief

## Features

- **3D 8-Ball Gameplay & Physics Engine**: Interactive 3D pool game simulation featuring realistic ball collision physics, aiming guides, and intuitive cue power controls.
- **AI Bot Opponents & Local Play**: Offline single-player mode allowing users to play matches against AI bot opponents or local pass-and-play.
- **Cue Shop & Customization**: In-game catalog to unlock and equip distinct cues with custom visual styles and attribute stats (power, spin, aim line).
- **Offline Progression & Rewards**: Match reward system earning coins and XP to level up, track player stats, and unlock new cues and table themes.
- **Material 3 UI Overlays**: Modern Material 3 menus and game HUD seamlessly integrated over the 3D graphics rendering canvas.

## High-Level Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven navigation) & Compose Material Adaptive library
- **Asynchronous & State Management**: Kotlin Coroutines, Flow, ViewModel
- **Graphics & Rendering**: 3D SurfaceView/Canvas engine integration with Compose overlays

## Implementation Steps

### Task_1_PhysicsAndRendering: Implement the 3D pool table rendering canvas integrated with Jetpack Compose, along with physics simulation for ball movement, collision physics, table boundaries, pocket detection, cue stick aiming guide, and power controls.
- **Status:** COMPLETED
- **Updates:** Implemented 3D perspective pool table renderer with camera projection, 3D ball lighting, rolling rotation, pocket entry animations, continuous elastic collision physics engine, interactive touch aiming and fine-nudge controls, power slider, spin control modal, HUD overlays, and verified unit tests. Gradle build passed successfully.
- **Acceptance Criteria:**
  - 3D pool table and ball rendering working in Compose overlay
  - Ball collision physics and table rail bouncing simulated
  - Interactive cue stick aiming and shot power mechanics functional
  - build pass

### Task_2_GameLogicAndAI: Implement 8-Ball pool game rules, turn management, local pass-and-play mode, foul/pocket detection, and offline AI bot opponent targeting and shooting logic.
- **Status:** COMPLETED
- **Updates:** Implemented complete 8-Ball and 9-Ball game rules engine, foul handling, cue ball in hand positioning, pass-and-play and Human vs AI mode with 3 difficulty levels, AI targeting raycast engine, match summary UI dialogs, and 23 passing unit tests. Gradle build passed successfully.
- **Acceptance Criteria:**
  - 8-Ball game state rules and turn transitions working
  - AI bot opponent can evaluate table state and execute shots
  - Pass-and-play mode working
  - Match win and loss conditions handled correctly
  - build pass

### Task_3_UIAndCueShop: Develop Jetpack Compose Material 3 HUD overlays, main menu, cue shop for unlocking and equipping cues with attribute stats (power, spin, aim line), and player progression/rewards system (coins, XP, stats).
- **Status:** COMPLETED
- **Updates:** Developed complete Material 3 UI navigation flow, Main Menu, Cue Shop with attribute stats & purchases, Table Customization screen, Player Profile & persistent Progression Repository, 8-Player Knockout Tournament bracket mode, end-of-match rewards, and integrated all cue/table stats into 3D renderer & physics engine. 39 unit tests passing, gradle build passed.
- **Acceptance Criteria:**
  - Material 3 UI HUD and menus integrated over 3D rendering canvas
  - Cue shop catalog allows unlocking and equipping cues with varying stats
  - Player stats, coins, and XP progression saved locally
  - build pass

### Task_4_RunAndVerify: Run and verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Deployed and verified on device with critic_agent. Confirmed landscape orientation, top app bar hidden in game, sleek side vertical power meter, working cue strike mechanics, 3D table perspective rendering, 39 passing unit tests, and 0 crashes.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - all game modes, shop, and rendering verified without crashes
- **Duration:** N/A

