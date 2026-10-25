package com.xandron.novapool.game

enum class FoulType(val description: String) {
    NONE("No Foul"),
    SCRATCH("Scratch! Cue ball in pocket"),
    WRONG_BALL_FIRST("Foul! Wrong ball hit first"),
    NO_BALL_HIT("Foul! Cue ball missed all object balls"),
    NO_CUSHION_AFTER_CONTACT("Foul! No cushion hit after contact"),
    EIGHT_BALL_PREMATURE("Loss! 8-Ball potted prematurely"),
    EIGHT_BALL_FOUL("Loss! Foul while potting 8-Ball")
}
