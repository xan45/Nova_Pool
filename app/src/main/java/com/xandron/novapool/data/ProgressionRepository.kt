package com.xandron.novapool.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xandron.novapool.model.CueItem
import com.xandron.novapool.model.FeltColorOption
import com.xandron.novapool.model.MatchRewardResult
import com.xandron.novapool.model.ProgressionState
import com.xandron.novapool.model.WoodFinishOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_pool_progression")

/**
 * Repository for managing persistent player progression, currency wallet, stats, and unlocked shop items.
 */
class ProgressionRepository(
    private val dataStore: DataStore<Preferences>? = null,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _progressionState = MutableStateFlow(ProgressionState())
    val progressionState: StateFlow<ProgressionState> = _progressionState.asStateFlow()

    init {
        if (dataStore != null) {
            externalScope.launch {
                loadFromDataStore()
            }
        }
    }

    private suspend fun loadFromDataStore() {
        if (dataStore == null) return
        val prefs = dataStore.data.first()
        val loadedState = ProgressionState(
            playerName = prefs[KEY_PLAYER_NAME] ?: "Pool Master",
            coins = prefs[KEY_COINS] ?: 1000,
            gems = prefs[KEY_GEMS] ?: 50,
            level = prefs[KEY_LEVEL] ?: 1,
            xp = prefs[KEY_XP] ?: 0,
            matchesPlayed = prefs[KEY_MATCHES_PLAYED] ?: 0,
            matchesWon = prefs[KEY_MATCHES_WON] ?: 0,
            totalBallsPotted = prefs[KEY_BALLS_POTTED] ?: 0,
            currentWinStreak = prefs[KEY_CURRENT_STREAK] ?: 0,
            highestWinStreak = prefs[KEY_HIGHEST_STREAK] ?: 0,
            tournamentTrophies = prefs[KEY_TOURNAMENT_TROPHIES] ?: 0,
            tournamentWins = prefs[KEY_TOURNAMENT_WINS] ?: 0,
            equippedCueId = prefs[KEY_EQUIPPED_CUE] ?: CueItem.DEFAULT_CUE.id,
            unlockedCueIds = prefs[KEY_UNLOCKED_CUES] ?: setOf(CueItem.DEFAULT_CUE.id),
            equippedFeltId = prefs[KEY_EQUIPPED_FELT] ?: FeltColorOption.EMERALD_GREEN.id,
            unlockedFeltIds = prefs[KEY_UNLOCKED_FELTS] ?: setOf(FeltColorOption.EMERALD_GREEN.id),
            equippedFrameId = prefs[KEY_EQUIPPED_FRAME] ?: WoodFinishOption.WALNUT.id,
            unlockedFrameIds = prefs[KEY_UNLOCKED_FRAMES] ?: setOf(WoodFinishOption.WALNUT.id)
        )
        _progressionState.value = loadedState
    }

    private fun persistState(state: ProgressionState) {
        _progressionState.value = state
        if (dataStore != null) {
            externalScope.launch {
                dataStore.edit { prefs ->
                    prefs[KEY_PLAYER_NAME] = state.playerName
                    prefs[KEY_COINS] = state.coins
                    prefs[KEY_GEMS] = state.gems
                    prefs[KEY_LEVEL] = state.level
                    prefs[KEY_XP] = state.xp
                    prefs[KEY_MATCHES_PLAYED] = state.matchesPlayed
                    prefs[KEY_MATCHES_WON] = state.matchesWon
                    prefs[KEY_BALLS_POTTED] = state.totalBallsPotted
                    prefs[KEY_CURRENT_STREAK] = state.currentWinStreak
                    prefs[KEY_HIGHEST_STREAK] = state.highestWinStreak
                    prefs[KEY_TOURNAMENT_TROPHIES] = state.tournamentTrophies
                    prefs[KEY_TOURNAMENT_WINS] = state.tournamentWins
                    prefs[KEY_EQUIPPED_CUE] = state.equippedCueId
                    prefs[KEY_UNLOCKED_CUES] = state.unlockedCueIds
                    prefs[KEY_EQUIPPED_FELT] = state.equippedFeltId
                    prefs[KEY_UNLOCKED_FELTS] = state.unlockedFeltIds
                    prefs[KEY_EQUIPPED_FRAME] = state.equippedFrameId
                    prefs[KEY_UNLOCKED_FRAMES] = state.unlockedFrameIds
                }
            }
        }
    }

    fun unlockCue(cueId: String, useGems: Boolean): Result<Unit> {
        val current = _progressionState.value
        val cue = CueItem.getById(cueId)
        if (current.unlockedCueIds.contains(cueId)) {
            return Result.success(Unit)
        }

        if (useGems) {
            if (cue.priceGems <= 0 || current.gems < cue.priceGems) {
                return Result.failure(IllegalStateException("Insufficient gems to purchase ${cue.name}"))
            }
            val newState = current.copy(
                gems = current.gems - cue.priceGems,
                unlockedCueIds = current.unlockedCueIds + cueId,
                equippedCueId = cueId
            )
            persistState(newState)
        } else {
            if (cue.priceCoins <= 0 || current.coins < cue.priceCoins) {
                return Result.failure(IllegalStateException("Insufficient coins to purchase ${cue.name}"))
            }
            val newState = current.copy(
                coins = current.coins - cue.priceCoins,
                unlockedCueIds = current.unlockedCueIds + cueId,
                equippedCueId = cueId
            )
            persistState(newState)
        }
        return Result.success(Unit)
    }

    fun equipCue(cueId: String): Result<Unit> {
        val current = _progressionState.value
        if (!current.unlockedCueIds.contains(cueId)) {
            return Result.failure(IllegalStateException("Cue $cueId is locked."))
        }
        persistState(current.copy(equippedCueId = cueId))
        return Result.success(Unit)
    }

    fun unlockFelt(felt: FeltColorOption, useGems: Boolean): Result<Unit> {
        val current = _progressionState.value
        if (current.unlockedFeltIds.contains(felt.id)) {
            return Result.success(Unit)
        }

        if (useGems) {
            if (felt.priceGems <= 0 || current.gems < felt.priceGems) {
                return Result.failure(IllegalStateException("Insufficient gems for felt ${felt.displayName}"))
            }
            val newState = current.copy(
                gems = current.gems - felt.priceGems,
                unlockedFeltIds = current.unlockedFeltIds + felt.id,
                equippedFeltId = felt.id
            )
            persistState(newState)
        } else {
            if (felt.priceCoins <= 0 || current.coins < felt.priceCoins) {
                return Result.failure(IllegalStateException("Insufficient coins for felt ${felt.displayName}"))
            }
            val newState = current.copy(
                coins = current.coins - felt.priceCoins,
                unlockedFeltIds = current.unlockedFeltIds + felt.id,
                equippedFeltId = felt.id
            )
            persistState(newState)
        }
        return Result.success(Unit)
    }

    fun equipFelt(felt: FeltColorOption): Result<Unit> {
        val current = _progressionState.value
        if (!current.unlockedFeltIds.contains(felt.id)) {
            return Result.failure(IllegalStateException("Felt ${felt.displayName} is locked."))
        }
        persistState(current.copy(equippedFeltId = felt.id))
        return Result.success(Unit)
    }

    fun unlockFrame(frame: WoodFinishOption, useGems: Boolean): Result<Unit> {
        val current = _progressionState.value
        if (current.unlockedFrameIds.contains(frame.id)) {
            return Result.success(Unit)
        }

        if (useGems) {
            if (frame.priceGems <= 0 || current.gems < frame.priceGems) {
                return Result.failure(IllegalStateException("Insufficient gems for frame ${frame.displayName}"))
            }
            val newState = current.copy(
                gems = current.gems - frame.priceGems,
                unlockedFrameIds = current.unlockedFrameIds + frame.id,
                equippedFrameId = frame.id
            )
            persistState(newState)
        } else {
            if (frame.priceCoins <= 0 || current.coins < frame.priceCoins) {
                return Result.failure(IllegalStateException("Insufficient coins for frame ${frame.displayName}"))
            }
            val newState = current.copy(
                coins = current.coins - frame.priceCoins,
                unlockedFrameIds = current.unlockedFrameIds + frame.id,
                equippedFrameId = frame.id
            )
            persistState(newState)
        }
        return Result.success(Unit)
    }

    fun equipFrame(frame: WoodFinishOption): Result<Unit> {
        val current = _progressionState.value
        if (!current.unlockedFrameIds.contains(frame.id)) {
            return Result.failure(IllegalStateException("Frame ${frame.displayName} is locked."))
        }
        persistState(current.copy(equippedFrameId = frame.id))
        return Result.success(Unit)
    }

    fun recordMatchResult(isWin: Boolean, ballsPotted: Int): MatchRewardResult {
        val current = _progressionState.value

        // Reward logic
        val baseCoins = if (isWin) 200 else 50
        val baseXp = if (isWin) 150 else 50
        val ballsBonusCoins = ballsPotted * 15
        val ballsBonusXp = ballsPotted * 20

        val newStreak = if (isWin) current.currentWinStreak + 1 else 0
        val streakBonusCoins = if (isWin) newStreak * 25 else 0
        val highestStreak = maxOf(current.highestWinStreak, newStreak)

        val totalEarnedCoins = baseCoins + ballsBonusCoins + streakBonusCoins
        val totalEarnedXp = baseXp + ballsBonusXp

        var newXp = current.xp + totalEarnedXp
        var newLevel = current.level
        var leveledUp = false
        var levelUpBonusCoins = 0
        var levelUpBonusGems = 0

        var neededXp = newLevel * 500
        while (newXp >= neededXp) {
            newXp -= neededXp
            newLevel += 1
            leveledUp = true
            levelUpBonusCoins += 200
            levelUpBonusGems += 10
            neededXp = newLevel * 500
        }

        val finalCoins = current.coins + totalEarnedCoins + levelUpBonusCoins
        val finalGems = current.gems + levelUpBonusGems

        val updatedState = current.copy(
            coins = finalCoins,
            gems = finalGems,
            level = newLevel,
            xp = newXp,
            matchesPlayed = current.matchesPlayed + 1,
            matchesWon = current.matchesWon + (if (isWin) 1 else 0),
            totalBallsPotted = current.totalBallsPotted + ballsPotted,
            currentWinStreak = newStreak,
            highestWinStreak = highestStreak
        )

        persistState(updatedState)

        return MatchRewardResult(
            isWin = isWin,
            coinsEarned = totalEarnedCoins,
            xpEarned = totalEarnedXp,
            ballsPottedBonus = ballsBonusCoins,
            winStreakBonus = streakBonusCoins,
            leveledUp = leveledUp,
            newLevel = newLevel,
            levelUpBonusCoins = levelUpBonusCoins,
            levelUpBonusGems = levelUpBonusGems,
            currentStreak = newStreak
        )
    }

    fun addCoinsAndGems(coins: Int, gems: Int) {
        val current = _progressionState.value
        persistState(current.copy(
            coins = maxOf(0, current.coins + coins),
            gems = maxOf(0, current.gems + gems)
        ))
    }

    fun addTournamentWin(prizeCoins: Int, prizeGems: Int) {
        val current = _progressionState.value
        persistState(current.copy(
            coins = current.coins + prizeCoins,
            gems = current.gems + prizeGems,
            tournamentTrophies = current.tournamentTrophies + 1,
            tournamentWins = current.tournamentWins + 1
        ))
    }

    fun resetState() {
        persistState(ProgressionState())
    }

    companion object {
        private val KEY_PLAYER_NAME = stringPreferencesKey("player_name")
        private val KEY_COINS = intPreferencesKey("coins")
        private val KEY_GEMS = intPreferencesKey("gems")
        private val KEY_LEVEL = intPreferencesKey("level")
        private val KEY_XP = intPreferencesKey("xp")
        private val KEY_MATCHES_PLAYED = intPreferencesKey("matches_played")
        private val KEY_MATCHES_WON = intPreferencesKey("matches_won")
        private val KEY_BALLS_POTTED = intPreferencesKey("balls_potted")
        private val KEY_CURRENT_STREAK = intPreferencesKey("current_streak")
        private val KEY_HIGHEST_STREAK = intPreferencesKey("highest_streak")
        private val KEY_TOURNAMENT_TROPHIES = intPreferencesKey("tournament_trophies")
        private val KEY_TOURNAMENT_WINS = intPreferencesKey("tournament_wins")
        private val KEY_EQUIPPED_CUE = stringPreferencesKey("equipped_cue")
        private val KEY_UNLOCKED_CUES = stringSetPreferencesKey("unlocked_cues")
        private val KEY_EQUIPPED_FELT = stringPreferencesKey("equipped_felt")
        private val KEY_UNLOCKED_FELTS = stringSetPreferencesKey("unlocked_felts")
        private val KEY_EQUIPPED_FRAME = stringPreferencesKey("equipped_frame")
        private val KEY_UNLOCKED_FRAMES = stringSetPreferencesKey("unlocked_frames")
    }
}
