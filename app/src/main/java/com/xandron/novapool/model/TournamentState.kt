package com.xandron.novapool.model

enum class TournamentRound(val title: String) {
    QUARTER_FINALS("Quarter-Finals"),
    SEMI_FINALS("Semi-Finals"),
    FINALS("Finals"),
    COMPLETED("Tournament Complete")
}

data class TournamentPlayer(
    val id: String,
    val name: String,
    val isHuman: Boolean = false,
    val avatarId: Int = 0
)

data class TournamentMatch(
    val matchId: Int,
    val round: TournamentRound,
    val player1: TournamentPlayer,
    val player2: TournamentPlayer,
    val winner: TournamentPlayer? = null,
    val isUserMatch: Boolean = false,
    val scorePlayer1: Int = 0,
    val scorePlayer2: Int = 0
)

data class TournamentState(
    val id: String = "local_tourney_8",
    val title: String = "8-Ball Championship Cup",
    val prizeCoins: Int = 1500,
    val prizeGems: Int = 25,
    val currentRound: TournamentRound = TournamentRound.QUARTER_FINALS,
    val matches: List<TournamentMatch> = emptyList(),
    val champion: TournamentPlayer? = null,
    val isUserEliminated: Boolean = false
)
