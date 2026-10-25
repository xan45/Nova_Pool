package com.xandron.novapool.game

import com.xandron.novapool.model.TournamentMatch
import com.xandron.novapool.model.TournamentPlayer
import com.xandron.novapool.model.TournamentRound
import com.xandron.novapool.model.TournamentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Manager for local 8-player knockout tournament mode.
 */
class TournamentManager {

    private val _tournamentState = MutableStateFlow<TournamentState?>(null)
    val tournamentState: StateFlow<TournamentState?> = _tournamentState.asStateFlow()

    fun startNewTournament(userName: String = "You") {
        val userPlayer = TournamentPlayer(id = "user", name = userName, isHuman = true)
        val aiPlayers = listOf(
            TournamentPlayer("ai_1", "Alex 'Shark' Vance"),
            TournamentPlayer("ai_2", "Elena 'Apex' Rostova"),
            TournamentPlayer("ai_3", "Marcus '8-Ball' Sterling"),
            TournamentPlayer("ai_4", "Chloe 'Cue Queen' Dubois"),
            TournamentPlayer("ai_5", "Dmitri 'Trigon' Volkov"),
            TournamentPlayer("ai_6", "Kenji 'Zero' Sato"),
            TournamentPlayer("ai_7", "Samantha 'Trick' Walsh")
        ).shuffled()

        val qfMatches = listOf(
            TournamentMatch(1, TournamentRound.QUARTER_FINALS, userPlayer, aiPlayers[0], isUserMatch = true),
            TournamentMatch(2, TournamentRound.QUARTER_FINALS, aiPlayers[1], aiPlayers[2]),
            TournamentMatch(3, TournamentRound.QUARTER_FINALS, aiPlayers[3], aiPlayers[4]),
            TournamentMatch(4, TournamentRound.QUARTER_FINALS, aiPlayers[5], aiPlayers[6])
        )

        _tournamentState.value = TournamentState(
            currentRound = TournamentRound.QUARTER_FINALS,
            matches = qfMatches,
            isUserEliminated = false
        )
    }

    fun getCurrentUserMatch(): TournamentMatch? {
        val state = _tournamentState.value ?: return null
        return state.matches.firstOrNull { it.round == state.currentRound && it.isUserMatch && it.winner == null }
    }

    fun recordUserMatchResult(userWon: Boolean): TournamentState? {
        val current = _tournamentState.value ?: return null
        val userMatch = getCurrentUserMatch() ?: return current

        val userPlayer = if (userMatch.player1.isHuman) userMatch.player1 else userMatch.player2
        val opponent = if (userMatch.player1.isHuman) userMatch.player2 else userMatch.player1

        val winner = if (userWon) userPlayer else opponent
        val scoreP1 = if (userWon) 1 else 0
        val scoreP2 = if (userWon) 0 else 1

        val updatedMatches = current.matches.map { m ->
            if (m.matchId == userMatch.matchId) {
                m.copy(winner = winner, scorePlayer1 = scoreP1, scorePlayer2 = scoreP2)
            } else {
                m
            }
        }

        val updatedState = current.copy(
            matches = updatedMatches,
            isUserEliminated = !userWon
        )
        _tournamentState.value = updatedState

        // Simulate other AI matches in current round and advance round if ready
        simulateOtherCurrentRoundMatches()
        return _tournamentState.value
    }

    private fun simulateOtherCurrentRoundMatches() {
        val current = _tournamentState.value ?: return
        val currentRoundMatches = current.matches.filter { it.round == current.currentRound }

        val simulatedMatches = current.matches.map { m ->
            if (m.round == current.currentRound && m.winner == null && !m.isUserMatch) {
                val p1Wins = Random.nextBoolean()
                val winner = if (p1Wins) m.player1 else m.player2
                m.copy(winner = winner, scorePlayer1 = if (p1Wins) 1 else 0, scorePlayer2 = if (p1Wins) 0 else 1)
            } else {
                m
            }
        }

        val allCurrentCompleted = simulatedMatches.filter { it.round == current.currentRound }.all { it.winner != null }
        if (!allCurrentCompleted) {
            _tournamentState.value = current.copy(matches = simulatedMatches)
            return
        }

        // Advance round
        when (current.currentRound) {
            TournamentRound.QUARTER_FINALS -> advanceToSemiFinals(current, simulatedMatches)
            TournamentRound.SEMI_FINALS -> advanceToFinals(current, simulatedMatches)
            TournamentRound.FINALS -> completeTournament(current, simulatedMatches)
            TournamentRound.COMPLETED -> {}
        }
    }

    private fun advanceToSemiFinals(state: TournamentState, currentMatches: List<TournamentMatch>) {
        val qfWinners = currentMatches.filter { it.round == TournamentRound.QUARTER_FINALS }.mapNotNull { it.winner }
        if (qfWinners.size < 4) return

        val sfMatch1 = TournamentMatch(
            5, TournamentRound.SEMI_FINALS, qfWinners[0], qfWinners[1],
            isUserMatch = qfWinners[0].isHuman || qfWinners[1].isHuman
        )
        val sfMatch2 = TournamentMatch(
            6, TournamentRound.SEMI_FINALS, qfWinners[2], qfWinners[3],
            isUserMatch = qfWinners[2].isHuman || qfWinners[3].isHuman
        )

        _tournamentState.value = state.copy(
            currentRound = TournamentRound.SEMI_FINALS,
            matches = currentMatches + listOf(sfMatch1, sfMatch2)
        )
    }

    private fun advanceToFinals(state: TournamentState, currentMatches: List<TournamentMatch>) {
        val sfWinners = currentMatches.filter { it.round == TournamentRound.SEMI_FINALS }.mapNotNull { it.winner }
        if (sfWinners.size < 2) return

        val finalMatch = TournamentMatch(
            7, TournamentRound.FINALS, sfWinners[0], sfWinners[1],
            isUserMatch = sfWinners[0].isHuman || sfWinners[1].isHuman
        )

        _tournamentState.value = state.copy(
            currentRound = TournamentRound.FINALS,
            matches = currentMatches + finalMatch
        )
    }

    private fun completeTournament(state: TournamentState, currentMatches: List<TournamentMatch>) {
        val finalMatch = currentMatches.firstOrNull { it.round == TournamentRound.FINALS }
        val champion = finalMatch?.winner

        _tournamentState.value = state.copy(
            currentRound = TournamentRound.COMPLETED,
            matches = currentMatches,
            champion = champion
        )
    }

    fun resetTournament() {
        _tournamentState.value = null
    }
}
