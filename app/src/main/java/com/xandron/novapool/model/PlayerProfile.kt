package com.xandron.novapool.model

import kotlin.math.max

/**
 * Progression and wallet state stored persistently.
 */
data class ProgressionState(
    val playerName: String = "Pool Master",
    val coins: Int = 1000,
    val gems: Int = 50,
    val level: Int = 1,
    val xp: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val totalBallsPotted: Int = 0,
    val currentWinStreak: Int = 0,
    val highestWinStreak: Int = 0,
    val tournamentTrophies: Int = 0,
    val tournamentWins: Int = 0,
    val equippedCueId: String = CueItem.DEFAULT_CUE.id,
    val unlockedCueIds: Set<String> = setOf(CueItem.DEFAULT_CUE.id),
    val equippedFeltId: String = FeltColorOption.EMERALD_GREEN.id,
    val unlockedFeltIds: Set<String> = setOf(FeltColorOption.EMERALD_GREEN.id),
    val equippedFrameId: String = WoodFinishOption.WALNUT.id,
    val unlockedFrameIds: Set<String> = setOf(WoodFinishOption.WALNUT.id)
) {
    val winRatePercentage: Float
        get() = if (matchesPlayed > 0) (matchesWon.toFloat() / matchesPlayed.toFloat()) * 100f else 0f

    val xpForNextLevel: Int
        get() = level * 500

    val xpLevelProgress: Float
        get() = (xp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)

    val equippedCue: CueItem
        get() = CueItem.getById(equippedCueId)

    val equippedFelt: FeltColorOption
        get() = FeltColorOption.fromId(equippedFeltId)

    val equippedFrame: WoodFinishOption
        get() = WoodFinishOption.fromId(equippedFrameId)
}

/**
 * Result data holder when a match ends and rewards are calculated.
 */
data class MatchRewardResult(
    val isWin: Boolean,
    val coinsEarned: Int,
    val xpEarned: Int,
    val ballsPottedBonus: Int,
    val winStreakBonus: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val levelUpBonusCoins: Int,
    val levelUpBonusGems: Int,
    val currentStreak: Int
)
