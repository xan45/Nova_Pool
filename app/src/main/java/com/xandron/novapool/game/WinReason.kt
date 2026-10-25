package com.xandron.novapool.game

enum class WinReason(val text: String) {
    NONE(""),
    EIGHT_BALL_POTTED_LEGALLY("Legally potted the 8-Ball!"),
    EIGHT_BALL_POTTED_PREMATURELY("Opponent potted the 8-Ball prematurely!"),
    EIGHT_BALL_POTTED_ON_FOUL("Opponent scratched or fouled while potting 8-Ball!"),
    NINE_BALL_POTTED_LEGALLY("Legally potted the 9-Ball!"),
    OPPONENT_CONCEDED("Opponent conceded.")
}
