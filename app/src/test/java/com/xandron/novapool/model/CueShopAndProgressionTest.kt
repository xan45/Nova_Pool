package com.xandron.novapool.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CueShopAndProgressionTest {

    @Test
    fun `CueItem catalog contains expected default and premium cues`() {
        val catalog = CueItem.CATALOG
        assertTrue(catalog.isNotEmpty())
        assertTrue(catalog.size >= 5)

        val defaultCue = CueItem.getById("standard_wood")
        assertEquals("Standard Wooden", defaultCue.name)
        assertEquals(0, defaultCue.priceCoins)

        val goldenRoyal = CueItem.getById("golden_royal")
        assertEquals("Golden Royal", goldenRoyal.name)
        assertEquals(5000, goldenRoyal.priceCoins)
        assertEquals(100, goldenRoyal.priceGems)
        assertEquals(CueRarity.MYTHIC, goldenRoyal.rarity)
    }

    @Test
    fun `CueItem multipliers scale correctly with stats`() {
        val standardCue = CueItem.getById("standard_wood") // stats 50
        val goldenRoyal = CueItem.getById("golden_royal") // stats 98

        assertTrue(goldenRoyal.powerMultiplier > standardCue.powerMultiplier)
        assertTrue(goldenRoyal.spinMultiplier > standardCue.spinMultiplier)
        assertTrue(goldenRoyal.aimLengthPx > standardCue.aimLengthPx)
    }

    @Test
    fun `TableCustomization options default to emerald green and walnut`() {
        val defaultFelt = FeltColorOption.fromId("felt_emerald")
        assertEquals(FeltColorOption.EMERALD_GREEN, defaultFelt)

        val blueFelt = FeltColorOption.fromId("felt_blue")
        assertEquals(FeltColorOption.TOURNAMENT_BLUE, blueFelt)

        val defaultFrame = WoodFinishOption.fromId("wood_walnut")
        assertEquals(WoodFinishOption.WALNUT, defaultFrame)

        val oakFrame = WoodFinishOption.fromId("wood_oak")
        assertEquals(WoodFinishOption.OAK, oakFrame)
    }

    @Test
    fun `ProgressionState win rate and level progress calculations`() {
        val profile0 = ProgressionState()
        assertEquals(0f, profile0.winRatePercentage, 0.01f)

        val profilePlayed = ProgressionState(matchesPlayed = 10, matchesWon = 7)
        assertEquals(70f, profilePlayed.winRatePercentage, 0.01f)

        val profileLvl1 = ProgressionState(level = 1, xp = 250)
        assertEquals(500, profileLvl1.xpForNextLevel)
        assertEquals(0.5f, profileLvl1.xpLevelProgress, 0.01f)
    }
}
