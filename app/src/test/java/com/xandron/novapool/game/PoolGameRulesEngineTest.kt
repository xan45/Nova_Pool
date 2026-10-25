package com.xandron.novapool.game

import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.physics.PoolPhysicsEngine
import com.xandron.novapool.rendering.Vector3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PoolGameRulesEngineTest {

    private lateinit var physics: PoolPhysicsEngine
    private lateinit var gameRules: PoolGameRulesEngine

    @Before
    fun setUp() {
        physics = PoolPhysicsEngine(tableWidth = 900f, tableLength = 1800f, ballRadius = 26f)
        gameRules = PoolGameRulesEngine()
        gameRules.initMatch(GameMode.EIGHT_BALL, MatchMode.PASS_AND_PLAY)
    }

    private fun simulateBreakShot() {
        val ball1 = physics.balls.first { it.number == 1 }
        val breakResult = ShotResult(
            firstBallHit = ball1,
            pottedBalls = mutableListOf(ball1),
            cushionHitAfterContact = true,
            isBreakShot = true
        )
        gameRules.evaluateShot(breakResult, physics)
    }

    @Test
    fun testOpenTableTargetBalls() {
        physics.setupRack(GameMode.EIGHT_BALL)

        // Open table initially
        val legalTargets = gameRules.getLegalTargetBalls(physics)

        // All active Solids (1-7) and Stripes (9-15) are legal targets
        assertEquals(14, legalTargets.size)
        assertFalse(legalTargets.any { it.isEightBall })
    }

    @Test
    fun test8BallGroupAssignmentOnFirstPocket() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot() // Complete break shot

        // Simulate shot where Solid ball 2 is potted on a normal turn (not break)
        val solid2 = physics.balls.first { it.number == 2 }
        val shotResult = ShotResult(
            firstBallHit = solid2,
            pottedBalls = mutableListOf(solid2),
            cueBallScratched = false,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        val foul = gameRules.evaluateShot(shotResult, physics)

        assertEquals(FoulType.NONE, foul)
        assertEquals(BallGroup.SOLIDS, gameRules.player1.group)
        assertEquals(BallGroup.STRIPES, gameRules.player2.group)
        // Player 1 potted solid legally, keeps turn!
        assertEquals(0, gameRules.currentPlayerIndex)
    }

    @Test
    fun testWrongBallFirstFoul() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot()
        gameRules.player1.group = BallGroup.SOLIDS
        gameRules.player2.group = BallGroup.STRIPES

        // Player 1 hits a Stripe ball (number 10) first
        val stripeBall = physics.balls.first { it.number == 10 }
        val shotResult = ShotResult(
            firstBallHit = stripeBall,
            pottedBalls = mutableListOf(),
            cueBallScratched = false,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        val foul = gameRules.evaluateShot(shotResult, physics)

        assertEquals(FoulType.WRONG_BALL_FIRST, foul)
        assertTrue(gameRules.ballInHand)
        // Turn switches to Player 2
        assertEquals(1, gameRules.currentPlayerIndex)
    }

    @Test
    fun testScratchFoul() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot()

        val solidBall = physics.balls.first { it.number == 2 }
        val cueBall = physics.getCueBall()!!

        val shotResult = ShotResult(
            firstBallHit = solidBall,
            pottedBalls = mutableListOf(cueBall),
            cueBallScratched = true,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        val foul = gameRules.evaluateShot(shotResult, physics)

        assertEquals(FoulType.SCRATCH, foul)
        assertTrue(gameRules.ballInHand)
        assertEquals(1, gameRules.currentPlayerIndex)
    }

    @Test
    fun testNoCushionHitFoul() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot()

        val solidBall = physics.balls.first { it.number == 2 }
        val shotResult = ShotResult(
            firstBallHit = solidBall,
            pottedBalls = mutableListOf(),
            cueBallScratched = false,
            cushionHitAfterContact = false,
            isBreakShot = false
        )

        val foul = gameRules.evaluateShot(shotResult, physics)

        assertEquals(FoulType.NO_CUSHION_AFTER_CONTACT, foul)
        assertTrue(gameRules.ballInHand)
        assertEquals(1, gameRules.currentPlayerIndex)
    }

    @Test
    fun testPrematureEightBallPottedLoss() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot()
        gameRules.player1.group = BallGroup.SOLIDS
        gameRules.player2.group = BallGroup.STRIPES

        val eightBall = physics.balls.first { it.isEightBall }

        // Player 1 still has solids left on table, but pots 8-ball
        val shotResult = ShotResult(
            firstBallHit = physics.balls.first { it.number == 2 },
            pottedBalls = mutableListOf(eightBall),
            cueBallScratched = false,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        gameRules.evaluateShot(shotResult, physics)

        assertTrue(gameRules.isGameOver)
        assertEquals(gameRules.player2, gameRules.winner)
        assertEquals(WinReason.EIGHT_BALL_POTTED_PREMATURELY, gameRules.winReason)
    }

    @Test
    fun testLegalEightBallWin() {
        physics.setupRack(GameMode.EIGHT_BALL)
        simulateBreakShot()
        gameRules.player1.group = BallGroup.SOLIDS
        gameRules.player2.group = BallGroup.STRIPES

        // Mark all P1 Solids as potted
        physics.balls.filter { it.number in 1..7 }.forEach { it.state = BallState.POTTED }

        val eightBall = physics.balls.first { it.isEightBall }
        val shotResult = ShotResult(
            firstBallHit = eightBall,
            pottedBalls = mutableListOf(eightBall),
            cueBallScratched = false,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        gameRules.evaluateShot(shotResult, physics)

        assertTrue(gameRules.isGameOver)
        assertEquals(gameRules.player1, gameRules.winner)
        assertEquals(WinReason.EIGHT_BALL_POTTED_LEGALLY, gameRules.winReason)
    }

    @Test
    fun testNineBallLowestBallRule() {
        gameRules.initMatch(GameMode.NINE_BALL, MatchMode.PASS_AND_PLAY)
        physics.setupRack(GameMode.NINE_BALL)

        val legalTargets = gameRules.getLegalTargetBalls(physics)

        assertEquals(1, legalTargets.size)
        assertEquals(1, legalTargets.first().number)
    }

    @Test
    fun testNineBallPottedWin() {
        gameRules.initMatch(GameMode.NINE_BALL, MatchMode.PASS_AND_PLAY)
        physics.setupRack(GameMode.NINE_BALL)

        val ball1 = physics.balls.first { it.number == 1 }
        val breakResult = ShotResult(
            firstBallHit = ball1,
            pottedBalls = mutableListOf(ball1),
            cushionHitAfterContact = true,
            isBreakShot = true
        )
        gameRules.evaluateShot(breakResult, physics)

        val ball2 = physics.balls.first { it.number == 2 }
        val ball9 = physics.balls.first { it.number == 9 }

        // Combination shot: hit lowest ball (2) first, 9 ball potted!
        val shotResult = ShotResult(
            firstBallHit = ball2,
            pottedBalls = mutableListOf(ball9),
            cueBallScratched = false,
            cushionHitAfterContact = true,
            isBreakShot = false
        )

        gameRules.evaluateShot(shotResult, physics)

        assertTrue(gameRules.isGameOver)
        assertEquals(gameRules.player1, gameRules.winner)
        assertEquals(WinReason.NINE_BALL_POTTED_LEGALLY, gameRules.winReason)
    }

    @Test
    fun testCueBallInHandPlacement() {
        physics.setupRack(GameMode.EIGHT_BALL)
        gameRules.initMatch(GameMode.EIGHT_BALL, MatchMode.PASS_AND_PLAY)

        val targetPos = Vector3(100f, -300f, 0f)
        val success = gameRules.placeCueBallInHand(physics, targetPos)

        assertTrue(success)

        val cue = physics.getCueBall()
        assertNotNull(cue)
        assertEquals(100f, cue!!.position.x, 0.1f)
        assertEquals(-300f, cue.position.y, 0.1f)
    }
}
