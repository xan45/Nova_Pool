package com.xandron.novapool.ai

import com.xandron.novapool.game.AIDifficulty
import com.xandron.novapool.game.PoolGameRulesEngine
import com.xandron.novapool.model.Ball
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.Pocket
import com.xandron.novapool.physics.PoolPhysicsEngine
import com.xandron.novapool.rendering.Vector3
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2

data class AIShotDecision(
    val targetBall: Ball,
    val pocket: Pocket,
    val targetAngle: Float,
    val power: Float,
    val ghostBallPos: Vector3,
    val cuePlacementPos: Vector3? = null
)

data class AIShotCandidate(
    val targetBall: Ball,
    val pocket: Pocket,
    val aimAngle: Float,
    val score: Float,
    val distance: Float,
    val cutAngle: Float,
    val ghostPos: Vector3
)

class PoolAIEngine {

    fun calculateBestShot(
        physics: PoolPhysicsEngine,
        gameRules: PoolGameRulesEngine,
        difficulty: AIDifficulty = AIDifficulty.MEDIUM
    ): AIShotDecision? {
        val cueBall = physics.getCueBall() ?: return null
        val legalTargets = gameRules.getLegalTargetBalls(physics)
        if (legalTargets.isEmpty()) return null

        var cuePos = cueBall.position
        var cuePlacement: Vector3? = null

        // If AI has ball in hand, find an optimal placement for cue ball
        if (gameRules.ballInHand) {
            cuePlacement = findOptimalCuePlacement(physics, legalTargets, gameRules.ballInHandBehindHeadstring)
            if (cuePlacement != null) {
                cuePos = cuePlacement
            }
        }

        val candidates = mutableListOf<AIShotCandidate>()
        val ballRadius = physics.ballRadius

        for (target in legalTargets) {
            for (pocket in physics.pockets) {
                val dirTP = (pocket.position - target.position).normalize()
                val ghostPos = target.position - dirTP * (ballRadius * 2f)

                val cueToGhost = ghostPos - cuePos
                val distCueToGhost = cueToGhost.length()
                if (distCueToGhost <= 0.001f) continue

                val aimVec = cueToGhost.normalize()
                var aimAngle = atan2(aimVec.y, aimVec.x)
                if (aimAngle < 0) aimAngle += (2 * PI).toFloat()

                val cosCut = aimVec.dot(dirTP).coerceIn(-1f, 1f)
                val cutAngle = acos(cosCut)

                // Skip extremely sharp cut angles (> ~75 degrees = 1.3 rad)
                if (cutAngle > 1.3f) continue

                // Check path obstruction from cue ball to ghost ball
                if (isPathObstructed(cuePos, ghostPos, ballRadius * 1.8f, listOf(0, target.id), physics.balls)) {
                    continue
                }

                // Check path obstruction from target ball to pocket
                if (isPathObstructed(target.position, pocket.position, ballRadius * 1.8f, listOf(target.id), physics.balls)) {
                    continue
                }

                val distTargetToPocket = target.position.distanceTo2D(pocket.position)
                val score = distCueToGhost * 0.8f + distTargetToPocket * 1.2f + cutAngle * 350f

                candidates.add(
                    AIShotCandidate(
                        targetBall = target,
                        pocket = pocket,
                        aimAngle = aimAngle,
                        score = score,
                        distance = distCueToGhost + distTargetToPocket,
                        cutAngle = cutAngle,
                        ghostPos = ghostPos
                    )
                )
            }
        }

        if (candidates.isEmpty()) {
            // Fallback: target closest legal ball directly without pocket line
            val closestTarget = legalTargets.minByOrNull { it.position.distanceTo2D(cuePos) } ?: legalTargets.first()
            val dirToTarget = (closestTarget.position - cuePos).normalize()
            var fallbackAngle = atan2(dirToTarget.y, dirToTarget.x)
            if (fallbackAngle < 0) fallbackAngle += (2 * PI).toFloat()

            return AIShotDecision(
                targetBall = closestTarget,
                pocket = physics.pockets.first(),
                targetAngle = fallbackAngle,
                power = 0.5f,
                ghostBallPos = closestTarget.position - dirToTarget * (ballRadius * 2f),
                cuePlacementPos = cuePlacement
            )
        }

        // Sort candidates by score ascending (best shot first)
        candidates.sortBy { it.score }

        val selected: AIShotCandidate = when (difficulty) {
            AIDifficulty.HARD -> candidates.first()
            AIDifficulty.MEDIUM -> {
                val topCount = minOf(3, candidates.size)
                candidates.subList(0, topCount).random()
            }
            AIDifficulty.EASY -> {
                candidates.random()
            }
        }

        // Apply aiming accuracy noise based on difficulty
        val maxNoiseRad = when (difficulty) {
            AIDifficulty.HARD -> 0.005f  // ~0.3 degrees
            AIDifficulty.MEDIUM -> 0.035f // ~2.0 degrees
            AIDifficulty.EASY -> 0.085f   // ~4.9 degrees
        }

        val noise = (Math.random().toFloat() * 2f - 1f) * maxNoiseRad
        var finalAngle = selected.aimAngle + noise
        while (finalAngle < 0) finalAngle += (2 * PI).toFloat()
        while (finalAngle >= 2 * PI) finalAngle -= (2 * PI).toFloat()

        val basePower = (selected.distance / 1400f).coerceIn(0.35f, 0.9f)
        val finalPower = when (difficulty) {
            AIDifficulty.HARD -> basePower
            AIDifficulty.MEDIUM -> (basePower + (Math.random().toFloat() * 0.1f - 0.05f)).coerceIn(0.3f, 0.95f)
            AIDifficulty.EASY -> (0.35f + Math.random().toFloat() * 0.45f).coerceIn(0.3f, 0.9f)
        }

        return AIShotDecision(
            targetBall = selected.targetBall,
            pocket = selected.pocket,
            targetAngle = finalAngle,
            power = finalPower,
            ghostBallPos = selected.ghostPos,
            cuePlacementPos = cuePlacement
        )
    }

    private fun findOptimalCuePlacement(
        physics: PoolPhysicsEngine,
        legalTargets: List<Ball>,
        behindHeadstring: Boolean
    ): Vector3 {
        val r = physics.ballRadius
        val minX = physics.minX + r + 20f
        val maxX = physics.maxX - r - 20f
        val minY = physics.minY + r + 20f
        val maxY = if (behindHeadstring) -physics.tableLength / 4f else physics.maxY - r - 20f

        val stepX = (maxX - minX) / 8f
        val stepY = (maxY - minY) / 8f

        var bestPos = Vector3(0f, -physics.tableLength / 4f, 0f)
        var minObstructionCount = Int.MAX_VALUE

        for (ix in 0..8) {
            val x = minX + ix * stepX
            for (iy in 0..8) {
                val y = minY + iy * stepY
                val pos = Vector3(x, y, 0f)

                // Avoid collision with active balls
                if (physics.balls.any { !it.isCueBall && it.state == BallState.ACTIVE && it.position.distanceTo2D(pos) < r * 2f }) {
                    continue
                }

                var obsCount = 0
                for (target in legalTargets) {
                    if (isPathObstructed(pos, target.position, r * 1.8f, listOf(0, target.id), physics.balls)) {
                        obsCount++
                    }
                }

                if (obsCount < minObstructionCount) {
                    minObstructionCount = obsCount
                    bestPos = pos
                }
            }
        }

        return bestPos
    }

    private fun isPathObstructed(
        start: Vector3,
        end: Vector3,
        clearanceRadius: Float,
        ignoreBallIds: List<Int>,
        balls: List<Ball>
    ): Boolean {
        val segmentVec = end - start
        val segmentLen = segmentVec.length()
        if (segmentLen < 0.001f) return false

        val dir = segmentVec / segmentLen

        for (ball in balls) {
            if (ball.state != BallState.ACTIVE || ignoreBallIds.contains(ball.id)) continue

            val toBall = ball.position - start
            val proj = toBall.x * dir.x + toBall.y * dir.y

            if (proj >= -clearanceRadius && proj <= segmentLen + clearanceRadius) {
                val perpDistSq = toBall.lengthSquared() - proj * proj
                if (perpDistSq < clearanceRadius * clearanceRadius) {
                    return true
                }
            }
        }
        return false
    }
}
