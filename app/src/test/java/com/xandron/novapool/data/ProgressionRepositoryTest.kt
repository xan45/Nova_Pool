package com.xandron.novapool.data

import com.xandron.novapool.model.CueItem
import com.xandron.novapool.model.FeltColorOption
import com.xandron.novapool.model.WoodFinishOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressionRepositoryTest {

    private lateinit var repository: ProgressionRepository

    @Before
    fun setUp() {
        repository = ProgressionRepository(dataStore = null)
    }

    @Test
    fun `initial state has default values`() {
        val state = repository.progressionState.value
        assertEquals("Pool Master", state.playerName)
        assertEquals(1000, state.coins)
        assertEquals(50, state.gems)
        assertEquals(1, state.level)
        assertEquals(0, state.xp)
        assertEquals(CueItem.DEFAULT_CUE.id, state.equippedCueId)
        assertTrue(state.unlockedCueIds.contains(CueItem.DEFAULT_CUE.id))
        assertEquals(FeltColorOption.EMERALD_GREEN.id, state.equippedFeltId)
        assertEquals(WoodFinishOption.WALNUT.id, state.equippedFrameId)
    }

    @Test
    fun `unlock cue with coins succeeds when sufficient balance`() {
        val result = repository.unlockCue("carbon_fiber", useGems = false)
        assertTrue(result.isSuccess)

        val state = repository.progressionState.value
        assertEquals(500, state.coins) // 1000 - 500
        assertTrue(state.unlockedCueIds.contains("carbon_fiber"))
        assertEquals("carbon_fiber", state.equippedCueId)
    }

    @Test
    fun `unlock cue with coins fails when insufficient balance`() {
        val result = repository.unlockCue("golden_royal", useGems = false) // costs 5000 coins
        assertTrue(result.isFailure)

        val state = repository.progressionState.value
        assertEquals(1000, state.coins)
        assertFalse(state.unlockedCueIds.contains("golden_royal"))
    }

    @Test
    fun `unlock cue with gems succeeds when sufficient gems`() {
        val result = repository.unlockCue("crimson_dragon", useGems = true) // costs 20 gems
        assertTrue(result.isSuccess)

        val state = repository.progressionState.value
        assertEquals(30, state.gems) // 50 - 20
        assertTrue(state.unlockedCueIds.contains("crimson_dragon"))
        assertEquals("crimson_dragon", state.equippedCueId)
    }

    @Test
    fun `equip cue fails for locked cue`() {
        val result = repository.equipCue("cyberpunk_neon")
        assertTrue(result.isFailure)

        assertEquals("standard_wood", repository.progressionState.value.equippedCueId)
    }

    @Test
    fun `unlock and equip felt cloth customization`() {
        val unlockResult = repository.unlockFelt(FeltColorOption.TOURNAMENT_BLUE, useGems = false)
        assertTrue(unlockResult.isSuccess)

        val state = repository.progressionState.value
        assertEquals(700, state.coins) // 1000 - 300
        assertEquals(FeltColorOption.TOURNAMENT_BLUE.id, state.equippedFeltId)
        assertTrue(state.unlockedFeltIds.contains(FeltColorOption.TOURNAMENT_BLUE.id))

        // Re-equip default felt
        val equipDefault = repository.equipFelt(FeltColorOption.EMERALD_GREEN)
        assertTrue(equipDefault.isSuccess)
        assertEquals(FeltColorOption.EMERALD_GREEN.id, repository.progressionState.value.equippedFeltId)
    }

    @Test
    fun `unlock and equip wood frame finish`() {
        val unlockResult = repository.unlockFrame(WoodFinishOption.OAK, useGems = false)
        assertTrue(unlockResult.isSuccess)

        val state = repository.progressionState.value
        assertEquals(750, state.coins) // 1000 - 250
        assertEquals(WoodFinishOption.OAK.id, state.equippedFrameId)

        // Equip walnut frame back
        val equipWalnut = repository.equipFrame(WoodFinishOption.WALNUT)
        assertTrue(equipWalnut.isSuccess)
        assertEquals(WoodFinishOption.WALNUT.id, repository.progressionState.value.equippedFrameId)
    }

    @Test
    fun `recordMatchResult calculates coins, XP, streaks, and level up bonuses`() {
        // Record win with 4 balls potted
        val reward = repository.recordMatchResult(isWin = true, ballsPotted = 4)

        assertTrue(reward.isWin)
        assertEquals(1, reward.currentStreak)
        // coins: 200 (win base) + 4*15 (balls) + 1*25 (streak) = 285
        assertEquals(285, reward.coinsEarned)
        // xp: 150 (win base) + 4*20 (balls) = 230
        assertEquals(230, reward.xpEarned)

        val stateAfter1 = repository.progressionState.value
        assertEquals(1, stateAfter1.matchesPlayed)
        assertEquals(1, stateAfter1.matchesWon)
        assertEquals(4, stateAfter1.totalBallsPotted)
        assertEquals(1285, stateAfter1.coins)
        assertEquals(1, stateAfter1.level)
        assertEquals(230, stateAfter1.xp)

        // Record another win with 8 balls potted -> triggers level up (xp threshold level 1 = 500)
        // 230 + (150 + 8*20 = 310) = 540 total XP -> Level 2, 40 XP remaining!
        val reward2 = repository.recordMatchResult(isWin = true, ballsPotted = 8)
        assertTrue(reward2.leveledUp)
        assertEquals(2, reward2.newLevel)
        assertEquals(200, reward2.levelUpBonusCoins)
        assertEquals(10, reward2.levelUpBonusGems)

        val stateAfter2 = repository.progressionState.value
        assertEquals(2, stateAfter2.level)
        assertEquals(40, stateAfter2.xp)
        assertEquals(60, stateAfter2.gems) // 50 + 10 level up bonus
    }
}
