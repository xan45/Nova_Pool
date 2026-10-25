package com.xandron.novapool.physics

import com.xandron.novapool.game.ShotResult
import com.xandron.novapool.model.Ball
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.CueState
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.model.Pocket
import com.xandron.novapool.model.TrajectoryPrediction
import com.xandron.novapool.rendering.Vector3
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance 3D Physics engine for Nova Pool.
 * Handles ball dynamics, ball-ball collisions, cushion bounces, pocket gravity drops,
 * friction decay, and aiming trajectory raycasting.
 */
class PoolPhysicsEngine(
    val tableWidth: Float = 900f,
    val tableLength: Float = 1800f,
    val ballRadius: Float = 26f
) {
    val minX: Float = -tableWidth / 2f + 40f
    val maxX: Float = tableWidth / 2f - 40f
    val minY: Float = -tableLength / 2f + 40f
    val maxY: Float = tableLength / 2f - 40f

    val pockets: List<Pocket> = Pocket.createStandardPockets(tableWidth, tableLength)
    val balls = mutableListOf<Ball>()

    // Physics parameters
    var restitution: Float = 0.96f
    var cushionRestitution: Float = 0.88f
    var rollingFriction: Float = 0.988f
    var minSpeedThreshold: Float = 0.2f

    // Event Callbacks
    var onCollisionListener: ((ball1Id: Int, ball2Id: Int, speed: Float) -> Unit)? = null
    var onCushionBounceListener: ((ballId: Int, speed: Float) -> Unit)? = null
    var onPocketDropListener: ((ball: Ball, pocket: Pocket) -> Unit)? = null

    // Shot tracking & rules integration
    val currentShotResult = ShotResult()
    var isShotInProgress: Boolean = false
    var onShotCompletedListener: ((ShotResult) -> Unit)? = null

    init {
        setupShotListeners()
        setupRack(GameMode.EIGHT_BALL)
    }

    private fun setupShotListeners() {
        var localCollisionListener: ((Int, Int, Float) -> Unit)? = null
        var localCushionListener: ((Int, Float) -> Unit)? = null
        var localPocketListener: ((Ball, Pocket) -> Unit)? = null

        localCollisionListener = { b1Id, b2Id, speed ->
            if (isShotInProgress && currentShotResult.firstBallHit == null) {
                if (b1Id == 0) {
                    currentShotResult.firstBallHit = balls.firstOrNull { it.id == b2Id }
                } else if (b2Id == 0) {
                    currentShotResult.firstBallHit = balls.firstOrNull { it.id == b1Id }
                }
            }
        }

        localCushionListener = { ballId, speed ->
            if (isShotInProgress && currentShotResult.firstBallHit != null) {
                currentShotResult.cushionHitAfterContact = true
            }
        }

        localPocketListener = { ball, pocket ->
            if (isShotInProgress) {
                if (ball.isCueBall) {
                    currentShotResult.cueBallScratched = true
                } else if (!currentShotResult.pottedBalls.any { it.id == ball.id }) {
                    currentShotResult.pottedBalls.add(ball)
                }
            }
        }

        onCollisionListener = localCollisionListener
        onCushionBounceListener = localCushionListener
        onPocketDropListener = localPocketListener
    }

    fun setupRack(mode: GameMode) {
        balls.clear()
        val cueBallPos = Vector3(0f, -tableLength / 4f, 0f)
        balls.add(Ball.createDefaultBall(0, cueBallPos))

        when (mode) {
            GameMode.EIGHT_BALL -> setup8BallRack()
            GameMode.NINE_BALL -> setup9BallRack()
            GameMode.PRACTICE -> setupPracticeRack()
        }
    }

    private fun setup8BallRack() {
        val rackStartY = tableLength / 4f
        val r = ballRadius
        val spacing = r * 2.05f
        val rowHeight = spacing * sqrt(3f) / 2f

        val ballNumbers = intArrayOf(
            1,
            9, 2,
            3, 8, 10,
            11, 4, 12, 5,
            13, 6, 14, 7, 15
        )

        var index = 0
        for (row in 0..4) {
            val count = row + 1
            val startX = -((count - 1) * spacing) / 2f
            val y = rackStartY + row * rowHeight

            for (col in 0 until count) {
                val num = ballNumbers[index++]
                val x = startX + col * spacing
                balls.add(Ball.createDefaultBall(num, Vector3(x, y, 0f)))
            }
        }
    }

    private fun setup9BallRack() {
        val rackStartY = tableLength / 4f
        val r = ballRadius
        val spacing = r * 2.05f
        val rowHeight = spacing * sqrt(3f) / 2f

        val rows = intArrayOf(1, 2, 3, 2, 1)
        val numbers = intArrayOf(1, 2, 3, 9, 5, 6, 7, 8, 4)

        var index = 0
        for (rIndex in rows.indices) {
            val count = rows[rIndex]
            val startX = -((count - 1) * spacing) / 2f
            val y = rackStartY + rIndex * rowHeight

            for (col in 0 until count) {
                val num = numbers[index++]
                val x = startX + col * spacing
                balls.add(Ball.createDefaultBall(num, Vector3(x, y, 0f)))
            }
        }
    }

    private fun setupPracticeRack() {
        val positions = listOf(
            Vector3(0f, 200f, 0f),
            Vector3(-150f, 300f, 0f),
            Vector3(150f, 350f, 0f),
            Vector3(-100f, -200f, 0f),
            Vector3(120f, -150f, 0f),
            Vector3(0f, 400f, 0f)
        )
        for ((idx, pos) in positions.withIndex()) {
            val num = idx + 1
            balls.add(Ball.createDefaultBall(num, pos))
        }
        balls.add(Ball.createDefaultBall(8, Vector3(0f, 500f, 0f)))
    }

    fun getCueBall(): Ball? {
        return balls.firstOrNull { it.isCueBall && it.state == BallState.ACTIVE }
            ?: balls.firstOrNull { it.isCueBall }
    }

    fun strikeCueBall(cueState: CueState, maxImpulse: Float = 1400f) {
        val cueBall = getCueBall() ?: return
        if (cueBall.state != BallState.ACTIVE) return

        currentShotResult.reset()
        isShotInProgress = true

        val dir = cueState.getDirectionVector()
        val impulseMag = cueState.power * maxImpulse
        val vel = dir * impulseMag

        val spinX = cueState.spinOffset.x * 200f
        val spinY = cueState.spinOffset.y * 200f

        cueBall.velocity = vel
        cueBall.angularVelocity = Vector3(-dir.y * impulseMag * 0.05f + spinX, dir.x * impulseMag * 0.05f + spinY, 0f)
    }

    fun isAnyBallMoving(): Boolean {
        return balls.any { it.state == BallState.POCKETING || (it.state == BallState.ACTIVE && it.isMoving(minSpeedThreshold)) }
    }

    fun update(deltaTime: Float, subSteps: Int = 8) {
        val subDt = deltaTime / subSteps.toFloat()
        repeat(subSteps) {
            updateSubStep(subDt)
        }

        if (isShotInProgress && !isAnyBallMoving()) {
            isShotInProgress = false
            onShotCompletedListener?.invoke(currentShotResult)
        }
    }

    private fun updateSubStep(dt: Float) {
        for (ball in balls) {
            if (ball.state == BallState.ACTIVE) {
                ball.position = ball.position + ball.velocity * dt

                val speed = ball.velocity.length()
                if (speed > 0f) {
                    val newSpeed = max(0f, speed * rollingFriction - 2f * dt)
                    if (newSpeed <= minSpeedThreshold) {
                        ball.velocity = Vector3.ZERO
                        ball.angularVelocity = Vector3.ZERO
                    } else {
                        ball.velocity = ball.velocity * (newSpeed / speed)
                    }
                }

                val vLen = ball.velocity.length()
                if (vLen > 0.1f) {
                    val rotSpeed = vLen / ballRadius
                    ball.rotationX += (ball.velocity.y / vLen) * rotSpeed * dt
                    ball.rotationY += (-ball.velocity.x / vLen) * rotSpeed * dt
                }

                checkCushionBounce(ball)
                checkPocketEntry(ball)
            } else if (ball.state == BallState.POCKETING) {
                ball.pocketProgress = min(1.0f, ball.pocketProgress + dt * 4f)
                val zDrop = -120f * ball.pocketProgress
                ball.position = Vector3(ball.position.x, ball.position.y, zDrop)
                ball.velocity = ball.velocity * 0.85f

                if (ball.pocketProgress >= 1.0f) {
                    ball.state = BallState.POTTED
                }
            }
        }
        checkBallCollisions()
    }

    private fun checkCushionBounce(ball: Ball) {
        val r = ball.radius
        var bounced = false
        val speedBefore = ball.velocity.length()

        if (ball.position.x < minX + r && ball.velocity.x < 0) {
            if (!isNearPocketMouth(ball.position)) {
                ball.position = Vector3(minX + r, ball.position.y, ball.position.z)
                ball.velocity = Vector3(-ball.velocity.x * cushionRestitution, ball.velocity.y * 0.98f, ball.velocity.z)
                bounced = true
            }
        } else if (ball.position.x > maxX - r && ball.velocity.x > 0) {
            if (!isNearPocketMouth(ball.position)) {
                ball.position = Vector3(maxX - r, ball.position.y, ball.position.z)
                ball.velocity = Vector3(-ball.velocity.x * cushionRestitution, ball.velocity.y * 0.98f, ball.velocity.z)
                bounced = true
            }
        }

        if (ball.position.y < minY + r && ball.velocity.y < 0) {
            if (!isNearPocketMouth(ball.position)) {
                ball.position = Vector3(ball.position.x, minY + r, ball.position.z)
                ball.velocity = Vector3(ball.velocity.x * 0.98f, -ball.velocity.y * cushionRestitution, ball.velocity.z)
                bounced = true
            }
        } else if (ball.position.y > maxY - r && ball.velocity.y > 0) {
            if (!isNearPocketMouth(ball.position)) {
                ball.position = Vector3(ball.position.x, maxY - r, ball.position.z)
                ball.velocity = Vector3(ball.velocity.x * 0.98f, -ball.velocity.y * cushionRestitution, ball.velocity.z)
                bounced = true
            }
        }

        if (bounced) {
            onCushionBounceListener?.invoke(ball.id, speedBefore)
        }
    }

    private fun isNearPocketMouth(pos: Vector3): Boolean {
        return pockets.any { pocket -> pos.distanceTo2D(pocket.position) < pocket.mouthRadius }
    }

    private fun checkPocketEntry(ball: Ball) {
        for (pocket in pockets) {
            val dist = ball.position.distanceTo2D(pocket.position)
            if (dist < pocket.dropRadius) {
                ball.state = BallState.POCKETING
                ball.pocketProgress = 0f
                onPocketDropListener?.invoke(ball, pocket)

                val dirToCenter = (pocket.position - ball.position).normalize()
                val speed = max(100f, ball.velocity.length() * 0.5f)
                ball.velocity = dirToCenter * speed
                break
            }
        }
    }

    private fun checkBallCollisions() {
        val count = balls.size
        val r = ballRadius

        for (i in 0 until count) {
            val b1 = balls[i]
            if (b1.state != BallState.ACTIVE) continue

            for (j in i + 1 until count) {
                val b2 = balls[j]
                if (b2.state != BallState.ACTIVE) continue

                val dx = b2.position.x - b1.position.x
                val dy = b2.position.y - b1.position.y
                val distSq = dx * dx + dy * dy
                val minDist = r * 2f

                if (distSq < minDist * minDist && distSq > 0.0001f) {
                    val dist = sqrt(distSq)
                    val nx = dx / dist
                    val ny = dy / dist

                    val overlap = 0.5f * (minDist - dist)
                    b1.position = Vector3(b1.position.x - nx * overlap, b1.position.y - ny * overlap, b1.position.z)
                    b2.position = Vector3(b2.position.x + nx * overlap, b2.position.y + ny * overlap, b2.position.z)

                    val kx = b1.velocity.x - b2.velocity.x
                    val ky = b1.velocity.y - b2.velocity.y
                    val p = 2f * (nx * kx + ny * ky) / (b1.mass + b2.mass)

                    if (nx * kx + ny * ky > 0f) {
                        val impulse = p * restitution

                        val v1x = b1.velocity.x - impulse * b2.mass * nx
                        val v1y = b1.velocity.y - impulse * b2.mass * ny
                        val v2x = b2.velocity.x + impulse * b1.mass * nx
                        val v2y = b2.velocity.y + impulse * b1.mass * ny

                        b1.velocity = Vector3(v1x, v1y, b1.velocity.z)
                        b2.velocity = Vector3(v2x, v2y, b2.velocity.z)

                        onCollisionListener?.invoke(b1.id, b2.id, impulse)
                    }
                }
            }
        }
    }

    fun calculateAimTrajectory(cueState: CueState, maxDistance: Float = 1500f): TrajectoryPrediction {
        val cueBall = getCueBall()
            ?: return TrajectoryPrediction(emptyList(), emptyList())

        val dir = cueState.getDirectionVector()
        val cueStart = cueBall.position
        val r2 = ballRadius * 2f

        var closestHitDist = maxDistance
        var hitBall: Ball? = null

        for (ball in balls) {
            if (ball.isCueBall || ball.state != BallState.ACTIVE) continue

            val vToBall = ball.position - cueStart
            val proj = vToBall.x * dir.x + vToBall.y * dir.y
            if (proj > 0) {
                val perpSq = vToBall.lengthSquared() - proj * proj
                if (perpSq < r2 * r2) {
                    val dHit = proj - sqrt(r2 * r2 - perpSq)
                    if (dHit > 0 && dHit < closestHitDist) {
                        closestHitDist = dHit
                        hitBall = ball
                    }
                }
            }
        }

        var cushionHitDist = Float.MAX_VALUE
        var cushionHitPt: Vector3? = null

        if (dir.x > 0) {
            val d = (maxX - ballRadius - cueStart.x) / dir.x
            if (d in 0.0f..cushionHitDist) {
                cushionHitDist = d
                cushionHitPt = cueStart + dir * d
            }
        } else if (dir.x < 0) {
            val d = (minX + ballRadius - cueStart.x) / dir.x
            if (d in 0.0f..cushionHitDist) {
                cushionHitDist = d
                cushionHitPt = cueStart + dir * d
            }
        }

        if (dir.y > 0) {
            val d = (maxY - ballRadius - cueStart.y) / dir.y
            if (d in 0.0f..cushionHitDist) {
                cushionHitDist = d
                cushionHitPt = cueStart + dir * d
            }
        } else if (dir.y < 0) {
            val d = (minY + ballRadius - cueStart.y) / dir.y
            if (d in 0.0f..cushionHitDist) {
                cushionHitDist = d
                cushionHitPt = cueStart + dir * d
            }
        }

        val cuePath = mutableListOf<Vector3>()
        cuePath.add(cueStart)

        if (hitBall != null && closestHitDist < cushionHitDist) {
            val ghostPos = cueStart + dir * closestHitDist
            cuePath.add(ghostPos)

            val hitNormal = (hitBall.position - ghostPos).normalize()
            val targetPath = listOf(
                hitBall.position,
                hitBall.position + hitNormal * 300f
            )

            val dotNorm = dir.x * hitNormal.x + dir.y * hitNormal.y
            val tangent = (dir - hitNormal * dotNorm).normalize()
            if (tangent.lengthSquared() > 0.01f) {
                cuePath.add(ghostPos + tangent * 200f)
            }

            return TrajectoryPrediction(
                cueBallPath = cuePath,
                targetBallPath = targetPath,
                hitBallId = hitBall.id,
                ghostBallPosition = ghostPos
            )
        } else if (cushionHitPt != null) {
            cuePath.add(cushionHitPt)

            val reflectDir = if (abs(cushionHitPt.x - maxX + ballRadius) < 1f || abs(cushionHitPt.x - minX - ballRadius) < 1f) {
                Vector3(-dir.x, dir.y, 0f)
            } else {
                Vector3(dir.x, -dir.y, 0f)
            }
            cuePath.add(cushionHitPt + reflectDir * 250f)

            return TrajectoryPrediction(
                cueBallPath = cuePath,
                targetBallPath = emptyList(),
                cushionHitPoint = cushionHitPt
            )
        }

        cuePath.add(cueStart + dir * maxDistance)
        return TrajectoryPrediction(cuePath, emptyList())
    }

    fun respawnCueBallIfNeeded() {
        val cue = balls.firstOrNull { it.isCueBall }
        if (cue == null || cue.state != BallState.ACTIVE) {
            balls.removeAll { it.isCueBall }
            val newCue = Ball.createDefaultBall(0, Vector3(0f, -tableLength / 4f, 0f))
            balls.add(newCue)
        }
    }
}
