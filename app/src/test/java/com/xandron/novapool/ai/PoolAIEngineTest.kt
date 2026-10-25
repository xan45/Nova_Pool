package com.xandron.novapool.ai

import com.xandron.novapool.game.AIDifficulty
import com.xandron.novapool.game.MatchMode
import com.xandron.novapool.game.PoolGameRulesEngine
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.physics.PoolPhysicsEngine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PoolAIEngineTest {

    private lateinit var physics: PoolPhysicsEngine
    private lateinit var gameRules: PoolGameRulesEngine
    private lateinit var aiEngine: PoolAIEngine

    @Before
    fun setUp() {
        physics = PoolPhysicsEngine(tableWidth = 900f, tableLength = 1800f, ballRadius = 26f)
        gameRules = PoolGameRulesEngine()
        gameRules.initMatch(GameMode.EIGHT_BALL, MatchMode.VS_AI, AIDifficulty.MEDIUM)
        aiEngine = PoolAIEngine()
    }

    @Test
    fun testAICalculatesShotForOpenTable() {
        physics.setupRack(GameMode.EIGHT_BALL)

        val decision = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.HARD)

        assertNotNull(decision)
        assertTrue(decision!!.power in 0.2f..1.0f)
        assertTrue(decision.targetAngle >= 0f)
    }

    @Test
    fun testAIDifficultyVariations() {
        physics.setupRack(GameMode.EIGHT_BALL)

        val easyShot = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.EASY)
        val medShot = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.MEDIUM)
        val hardShot = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.HARD)

        assertNotNull(easyShot)
        assertNotNull(medShot)
        assertNotNull(hardShot)
    }

    @Test
    fun testAIBallInHandPlacement() {
        physics.setupRack(GameMode.EIGHT_BALL)

        // Force ball in hand state
        val shotResult = com.xandron.novapool.game.ShotResult(
            firstBallHit = null,
            cueBallScratched = true
        )
        gameRules.evaluateShot(shotResult, physics)

        assertTrue(gameRules.ballInHand)

        val decision = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.MEDIUM)

        assertNotNull(decision)
        assertNotNull(decision!!.cuePlacementPos)
    }

    @Test
    fun testAI9BallTargetSelection() {
        gameRules.initMatch(GameMode.NINE_BALL, MatchMode.VS_AI, AIDifficulty.HARD)
        physics.setupRack(GameMode.NINE_BALL)

        val decision = aiEngine.calculateBestShot(physics, gameRules, AIDifficulty.HARD)

        assertNotNull(decision)
        // In 9-ball, target ball must be 1-ball initially
        assertTrue(decision!!.targetBall.number == 1)
    }
}
