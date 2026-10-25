package com.xandron.novapool.game

import com.xandron.novapool.model.Ball

data class ShotResult(
    var firstBallHit: Ball? = null,
    val pottedBalls: MutableList<Ball> = mutableListOf(),
    var cueBallScratched: Boolean = false,
    var cushionHitAfterContact: Boolean = false,
    var isBreakShot: Boolean = false
) {
    fun reset() {
        firstBallHit = null
        pottedBalls.clear()
        cueBallScratched = false
        cushionHitAfterContact = false
        isBreakShot = false
    }
}
