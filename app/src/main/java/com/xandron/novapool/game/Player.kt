package com.xandron.novapool.game

enum class PlayerType {
    HUMAN,
    AI_BOT
}

enum class AIDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard / Pro")
}

data class Player(
    val id: Int,
    val name: String,
    val type: PlayerType = PlayerType.HUMAN,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    var group: BallGroup = BallGroup.UNASSIGNED,
    var score: Int = 0
)
