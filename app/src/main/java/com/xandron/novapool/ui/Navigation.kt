package com.xandron.novapool.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.game.AIDifficulty
import com.xandron.novapool.game.MatchMode
import com.xandron.novapool.game.TournamentManager
import com.xandron.novapool.model.GameMode

sealed class NovaScreen {
    data object MainMenu : NovaScreen()
    data object CueShop : NovaScreen()
    data object TableCustomization : NovaScreen()
    data object PlayerProfile : NovaScreen()
    data object LocalTournament : NovaScreen()
    data class Game(
        val gameMode: GameMode = GameMode.EIGHT_BALL,
        val matchMode: MatchMode = MatchMode.VS_AI,
        val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
        val isTournamentMatch: Boolean = false
    ) : NovaScreen()
}

@Composable
fun NovaPoolNavHost(
    repository: ProgressionRepository,
    tournamentManager: TournamentManager
) {
    val backStack = remember { mutableStateListOf<NovaScreen>(NovaScreen.MainMenu) }
    val currentScreen = backStack.lastOrNull() ?: NovaScreen.MainMenu

    val progressionState by repository.progressionState.collectAsState()
    val tournamentState by tournamentManager.tournamentState.collectAsState()

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLast()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "NovaPoolNavTransition"
    ) { screen ->
        when (screen) {
            is NovaScreen.MainMenu -> {
                MainMenuScreen(
                    progressionState = progressionState,
                    onNavigateToGame = { mode, matchMode, difficulty ->
                        backStack.add(NovaScreen.Game(mode, matchMode, difficulty))
                    },
                    onNavigateToCueShop = {
                        backStack.add(NovaScreen.CueShop)
                    },
                    onNavigateToTableCustomization = {
                        backStack.add(NovaScreen.TableCustomization)
                    },
                    onNavigateToProfile = {
                        backStack.add(NovaScreen.PlayerProfile)
                    },
                    onNavigateToTournament = {
                        backStack.add(NovaScreen.LocalTournament)
                    }
                )
            }
            is NovaScreen.CueShop -> {
                CueShopScreen(
                    progressionState = progressionState,
                    repository = repository,
                    onBackClick = { backStack.removeLast() }
                )
            }
            is NovaScreen.TableCustomization -> {
                TableCustomizationScreen(
                    progressionState = progressionState,
                    repository = repository,
                    onBackClick = { backStack.removeLast() }
                )
            }
            is NovaScreen.PlayerProfile -> {
                PlayerProfileScreen(
                    progressionState = progressionState,
                    repository = repository,
                    onBackClick = { backStack.removeLast() }
                )
            }
            is NovaScreen.LocalTournament -> {
                LocalTournamentScreen(
                    progressionState = progressionState,
                    tournamentState = tournamentState,
                    tournamentManager = tournamentManager,
                    repository = repository,
                    onPlayTournamentMatch = {
                        backStack.add(
                            NovaScreen.Game(
                                gameMode = GameMode.EIGHT_BALL,
                                matchMode = MatchMode.VS_AI,
                                aiDifficulty = AIDifficulty.HARD,
                                isTournamentMatch = true
                            )
                        )
                    },
                    onBackClick = { backStack.removeLast() }
                )
            }
            is NovaScreen.Game -> {
                PoolGameScreen(
                    progressionState = progressionState,
                    repository = repository,
                    tournamentManager = tournamentManager,
                    gameMode = screen.gameMode,
                    matchMode = screen.matchMode,
                    aiDifficulty = screen.aiDifficulty,
                    isTournamentMatch = screen.isTournamentMatch,
                    onExitGame = {
                        backStack.removeLast()
                    }
                )
            }
        }
    }
}
