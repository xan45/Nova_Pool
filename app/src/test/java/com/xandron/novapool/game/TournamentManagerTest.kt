package com.xandron.novapool.game

import com.xandron.novapool.model.TournamentRound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TournamentManagerTest {

    private lateinit var tournamentManager: TournamentManager

    @Before
    fun setUp() {
        tournamentManager = TournamentManager()
    }

    @Test
    fun `startNewTournament initializes 8 player bracket in quarter finals`() {
        tournamentManager.startNewTournament("TestPlayer")

        val state = tournamentManager.tournamentState.value
        assertNotNull(state)
        assertEquals(TournamentRound.QUARTER_FINALS, state!!.currentRound)
        assertEquals(4, state.matches.size)

        val userMatch = tournamentManager.getCurrentUserMatch()
        assertNotNull(userMatch)
        assertTrue(userMatch!!.isUserMatch)
        assertEquals("TestPlayer", userMatch.player1.name)
    }

    @Test
    fun `recordUserMatchResult win advances user to semi finals`() {
        tournamentManager.startNewTournament("TestPlayer")

        // User wins QF match
        val stateAfterQF = tournamentManager.recordUserMatchResult(userWon = true)
        assertNotNull(stateAfterQF)
        assertFalse(stateAfterQF!!.isUserEliminated)
        assertEquals(TournamentRound.SEMI_FINALS, stateAfterQF.currentRound)

        val sfUserMatch = tournamentManager.getCurrentUserMatch()
        assertNotNull(sfUserMatch)
        assertEquals(TournamentRound.SEMI_FINALS, sfUserMatch!!.round)
    }

    @Test
    fun `recordUserMatchResult loss marks user eliminated`() {
        tournamentManager.startNewTournament("TestPlayer")

        // User loses QF match
        val stateAfterQF = tournamentManager.recordUserMatchResult(userWon = false)
        assertNotNull(stateAfterQF)
        assertTrue(stateAfterQF!!.isUserEliminated)
    }

    @Test
    fun `full tournament win culminates in COMPLETED round with user as champion`() {
        tournamentManager.startNewTournament("TestPlayer")

        // Win QF
        tournamentManager.recordUserMatchResult(userWon = true)
        assertEquals(TournamentRound.SEMI_FINALS, tournamentManager.tournamentState.value?.currentRound)

        // Win SF
        tournamentManager.recordUserMatchResult(userWon = true)
        assertEquals(TournamentRound.FINALS, tournamentManager.tournamentState.value?.currentRound)

        // Win Finals
        val finalState = tournamentManager.recordUserMatchResult(userWon = true)
        assertEquals(TournamentRound.COMPLETED, finalState?.currentRound)
        assertNotNull(finalState?.champion)
        assertEquals("TestPlayer", finalState?.champion?.name)
    }
}
