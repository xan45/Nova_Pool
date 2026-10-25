package com.xandron.novapool.physics

import com.xandron.novapool.model.Ball
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.CueState
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.rendering.Vector3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI

class PoolPhysicsEngineTest {

    private lateinit var physics: PoolPhysicsEngine

    @Before
    fun setUp() {
        physics = PoolPhysicsEngine(tableWidth = 900f, tableLength = 1800f, ballRadius = 26f)
    }

    @Test
    fun testRackSetup8Ball() {
        physics.setupRack(GameMode.EIGHT_BALL)
        assertEquals(16, physics.balls.size) // 1 Cue ball + 15 object balls

        val cueBall = physics.getCueBall()
        assertNotNull(cueBall)
        assertTrue(cueBall!!.isCueBall)
        assertEquals(BallState.ACTIVE, cueBall.state)
    }

    @Test
    fun testRackSetup9Ball() {
        physics.setupRack(GameMode.NINE_BALL)
        assertEquals(10, physics.balls.size) // 1 Cue ball + 9 object balls
    }

    @Test
    fun testCueBallStrike() {
        physics.setupRack(GameMode.EIGHT_BALL)
        val cueBall = physics.getCueBall()!!

        val cueState = CueState(angle = (PI / 2.0).toFloat(), power = 0.5f)
        physics.strikeCueBall(cueState, maxImpulse = 1000f)

        assertTrue(cueBall.isMoving())
        assertTrue(cueBall.velocity.y > 0)
    }

    @Test
    fun testFrictionDeceleration() {
        physics.setupRack(GameMode.EIGHT_BALL)
        val cueBall = physics.getCueBall()!!
        cueBall.velocity = Vector3(0f, 100f, 0f)

        val initialSpeed = cueBall.velocity.length()

        // Simulate 20 sub-steps
        for (i in 0 until 20) {
            physics.update(0.016f)
        }

        val speedAfter = cueBall.velocity.length()
        assertTrue("Speed should decrease due to friction", speedAfter < initialSpeed)
    }

    @Test
    fun testCushionBounce() {
        physics.setupRack(GameMode.EIGHT_BALL)
        val cueBall = physics.getCueBall()!!

        // Move cue ball close to right cushion moving right
        cueBall.position = Vector3(physics.maxX - physics.ballRadius - 5f, 0f, 0f)
        cueBall.velocity = Vector3(500f, 0f, 0f)

        physics.update(0.02f)

        assertTrue("Cue ball should bounce back left (negative velocity X)", cueBall.velocity.x < 0f)
    }

    @Test
    fun testBallToBallCollision() {
        physics.balls.clear()
        val b1 = Ball.createDefaultBall(0, Vector3(0f, 0f, 0f)).apply {
            velocity = Vector3(0f, 300f, 0f)
        }
        val b2 = Ball.createDefaultBall(1, Vector3(0f, 40f, 0f)).apply {
            velocity = Vector3(0f, 0f, 0f)
        }

        physics.balls.add(b1)
        physics.balls.add(b2)

        var collisionOccurred = false
        physics.onCollisionListener = { _, _, _ ->
            collisionOccurred = true
        }

        physics.update(0.02f)

        assertTrue("Collision should be detected", collisionOccurred)
        assertTrue("Target ball should gain forward momentum", b2.velocity.y > 0f)
    }

    @Test
    fun testPocketEntryDetection() {
        physics.balls.clear()
        val cornerPocket = physics.pockets.first()

        val ball = Ball.createDefaultBall(1, cornerPocket.position + Vector3(5f, 5f, 0f)).apply {
            velocity = Vector3(-10f, -10f, 0f)
        }
        physics.balls.add(ball)

        var pocketed = false
        physics.onPocketDropListener = { _, _ ->
            pocketed = true
        }

        physics.update(0.02f)

        assertTrue("Ball should be detected entering pocket", pocketed || ball.state == BallState.POCKETING)
    }

    @Test
    fun testAimTrajectoryCalculation() {
        physics.setupRack(GameMode.EIGHT_BALL)
        val cueState = CueState(angle = (PI / 2.0).toFloat(), power = 0.5f)

        val prediction = physics.calculateAimTrajectory(cueState)
        assertTrue(prediction.cueBallPath.isNotEmpty())
    }
}
