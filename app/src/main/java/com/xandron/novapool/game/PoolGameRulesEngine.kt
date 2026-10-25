package com.xandron.novapool.game

import com.xandron.novapool.model.Ball
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.BallType
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.physics.PoolPhysicsEngine
import com.xandron.novapool.rendering.Vector3

class PoolGameRulesEngine {

    var gameMode: GameMode = GameMode.EIGHT_BALL
        private set
    var matchMode: MatchMode = MatchMode.VS_AI
        private set

    var player1: Player = Player(id = 1, name = "Player 1", type = PlayerType.HUMAN)
        private set
    var player2: Player = Player(id = 2, name = "AI Bot", type = PlayerType.AI_BOT, aiDifficulty = AIDifficulty.MEDIUM)
        private set

    var currentPlayerIndex: Int = 0
        private set

    var isBreakShot: Boolean = true
        private set

    var ballInHand: Boolean = false
        private set

    var ballInHandBehindHeadstring: Boolean = false
        private set

    var lastFoul: FoulType = FoulType.NONE
        private set

    var winner: Player? = null
        private set

    var winReason: WinReason = WinReason.NONE
        private set

    var isGameOver: Boolean = false
        private set

    fun initMatch(
        gameMode: GameMode = GameMode.EIGHT_BALL,
        matchMode: MatchMode = MatchMode.VS_AI,
        aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM
    ) {
        this.gameMode = gameMode
        this.matchMode = matchMode

        player1 = Player(id = 1, name = "Player 1", type = PlayerType.HUMAN, group = BallGroup.UNASSIGNED)

        player2 = when (matchMode) {
            MatchMode.VS_AI -> Player(
                id = 2,
                name = "AI Bot (${aiDifficulty.displayName})",
                type = PlayerType.AI_BOT,
                aiDifficulty = aiDifficulty,
                group = BallGroup.UNASSIGNED
            )
            MatchMode.PASS_AND_PLAY -> Player(
                id = 2,
                name = "Player 2",
                type = PlayerType.HUMAN,
                group = BallGroup.UNASSIGNED
            )
            MatchMode.PRACTICE -> Player(
                id = 2,
                name = "Practice",
                type = PlayerType.HUMAN,
                group = BallGroup.UNASSIGNED
            )
        }

        currentPlayerIndex = 0
        isBreakShot = true
        ballInHand = false
        ballInHandBehindHeadstring = false
        lastFoul = FoulType.NONE
        winner = null
        winReason = WinReason.NONE
        isGameOver = false
    }

    fun getCurrentPlayer(): Player = if (currentPlayerIndex == 0) player1 else player2

    fun getOpponentPlayer(): Player = if (currentPlayerIndex == 0) player2 else player1

    fun switchTurn() {
        if (matchMode == MatchMode.PRACTICE) return
        currentPlayerIndex = if (currentPlayerIndex == 0) 1 else 0
    }

    fun getRemainingGroupBalls(player: Player, physics: PoolPhysicsEngine): List<Ball> {
        return when (player.group) {
            BallGroup.SOLIDS -> physics.balls.filter { it.type == BallType.SOLID && it.state == BallState.ACTIVE }
            BallGroup.STRIPES -> physics.balls.filter { it.type == BallType.STRIPE && it.state == BallState.ACTIVE }
            else -> emptyList()
        }
    }

    fun getLegalTargetBalls(physics: PoolPhysicsEngine): List<Ball> {
        val activeBalls = physics.balls.filter { !it.isCueBall && it.state == BallState.ACTIVE }
        if (activeBalls.isEmpty()) return emptyList()

        if (gameMode == GameMode.PRACTICE) {
            return activeBalls
        }

        if (gameMode == GameMode.NINE_BALL) {
            val lowestNum = activeBalls.minOfOrNull { it.number } ?: return emptyList()
            return activeBalls.filter { it.number == lowestNum }
        }

        // 8-Ball mode
        val curPlayer = getCurrentPlayer()

        if (curPlayer.group == BallGroup.UNASSIGNED) {
            // Open table: any Solid (1-7) or Stripe (9-15) is legal
            val openTargets = activeBalls.filter { it.type == BallType.SOLID || it.type == BallType.STRIPE }
            return if (openTargets.isNotEmpty()) openTargets else activeBalls.filter { it.isEightBall }
        }

        val remainingGroupBalls = getRemainingGroupBalls(curPlayer, physics)
        return if (remainingGroupBalls.isNotEmpty()) {
            remainingGroupBalls
        } else {
            // Group cleared: 8-Ball is the target!
            activeBalls.filter { it.isEightBall }
        }
    }

    fun isLegalTargetBall(ball: Ball, physics: PoolPhysicsEngine): Boolean {
        if (ball.isCueBall) return false
        val legalTargets = getLegalTargetBalls(physics)
        return legalTargets.any { it.id == ball.id }
    }

    fun evaluateShot(shotResult: ShotResult, physics: PoolPhysicsEngine): FoulType {
        if (isGameOver) return FoulType.NONE

        var foul = FoulType.NONE

        // 1. Check Scratch / Cue ball potted
        if (shotResult.cueBallScratched) {
            foul = FoulType.SCRATCH
        }
        // 2. Check First Ball Hit
        else if (shotResult.firstBallHit == null) {
            foul = FoulType.NO_BALL_HIT
        }
        // 3. Check Legal Contact
        else if (!isLegalTargetBall(shotResult.firstBallHit!!, physics)) {
            foul = FoulType.WRONG_BALL_FIRST
        }
        // 4. Check Cushion Hit after Contact (if no ball was potted)
        else if (shotResult.pottedBalls.isEmpty() && !shotResult.cushionHitAfterContact) {
            foul = FoulType.NO_CUSHION_AFTER_CONTACT
        }

        // Ensure potted ball states are synced after foul evaluation
        for (potted in shotResult.pottedBalls) {
            if (potted.state == BallState.ACTIVE) {
                potted.state = BallState.POTTED
            }
        }

        lastFoul = foul

        if (gameMode == GameMode.EIGHT_BALL) {
            evaluateEightBallRules(shotResult, foul, physics)
        } else if (gameMode == GameMode.NINE_BALL) {
            evaluateNineBallRules(shotResult, foul, physics)
        } else {
            // Practice mode
            if (foul != FoulType.NONE) {
                ballInHand = true
            }
        }

        isBreakShot = false
        return foul
    }

    private fun evaluateEightBallRules(shotResult: ShotResult, foul: FoulType, physics: PoolPhysicsEngine) {
        val curPlayer = getCurrentPlayer()
        val oppPlayer = getOpponentPlayer()

        val eightBallPotted = shotResult.pottedBalls.any { it.isEightBall }
        val pottedObjectBalls = shotResult.pottedBalls.filter { !it.isCueBall && !it.isEightBall }

        if (eightBallPotted) {
            if (isBreakShot) {
                // 8-Ball potted on break: Respawn 8-Ball on foot spot
                respawnEightBall(physics)
                if (foul != FoulType.NONE) {
                    ballInHand = true
                    ballInHandBehindHeadstring = true
                    switchTurn()
                }
                return
            }

            val remainingOwnBalls = getRemainingGroupBalls(curPlayer, physics)
            val hadGroupCleared = (curPlayer.group != BallGroup.UNASSIGNED) && remainingOwnBalls.isEmpty()

            if (foul != FoulType.NONE) {
                // Foul while potting 8-Ball -> Instant Loss!
                winner = oppPlayer
                winReason = WinReason.EIGHT_BALL_POTTED_ON_FOUL
                isGameOver = true
            } else if (!hadGroupCleared) {
                // Potting 8-Ball prematurely -> Instant Loss!
                winner = oppPlayer
                winReason = WinReason.EIGHT_BALL_POTTED_PREMATURELY
                isGameOver = true
            } else {
                // Legally potted 8-Ball -> Victory!
                winner = curPlayer
                winReason = WinReason.EIGHT_BALL_POTTED_LEGALLY
                isGameOver = true
            }
            return
        }

        // Group Assignment (Open Table)
        if (curPlayer.group == BallGroup.UNASSIGNED && foul == FoulType.NONE && pottedObjectBalls.isNotEmpty() && !isBreakShot) {
            val firstPotted = pottedObjectBalls.first()
            if (firstPotted.type == BallType.SOLID) {
                curPlayer.group = BallGroup.SOLIDS
                oppPlayer.group = BallGroup.STRIPES
            } else if (firstPotted.type == BallType.STRIPE) {
                curPlayer.group = BallGroup.STRIPES
                oppPlayer.group = BallGroup.SOLIDS
            }
        }

        // Turn Management & Ball in Hand
        if (foul != FoulType.NONE) {
            ballInHand = true
            ballInHandBehindHeadstring = (isBreakShot && foul == FoulType.SCRATCH)
            switchTurn()
        } else {
            // Check if player potted at least one of their legal target balls
            val legalPotted = if (curPlayer.group == BallGroup.UNASSIGNED) {
                pottedObjectBalls.isNotEmpty()
            } else {
                pottedObjectBalls.any {
                    (curPlayer.group == BallGroup.SOLIDS && it.type == BallType.SOLID) ||
                    (curPlayer.group == BallGroup.STRIPES && it.type == BallType.STRIPE)
                }
            }

            if (legalPotted) {
                // Player continues turn
                ballInHand = false
            } else {
                // Turn switches
                ballInHand = false
                switchTurn()
            }
        }
    }

    private fun evaluateNineBallRules(shotResult: ShotResult, foul: FoulType, physics: PoolPhysicsEngine) {
        val curPlayer = getCurrentPlayer()

        val nineBallPotted = shotResult.pottedBalls.any { it.number == 9 }

        if (nineBallPotted) {
            if (foul == FoulType.NONE) {
                // Legally potted 9-Ball -> Victory!
                winner = curPlayer
                winReason = WinReason.NINE_BALL_POTTED_LEGALLY
                isGameOver = true
                return
            } else {
                // 9-Ball potted on foul -> Respawn 9-ball on foot spot
                respawnNineBall(physics)
            }
        }

        if (foul != FoulType.NONE) {
            ballInHand = true
            switchTurn()
        } else {
            val anyObjectPotted = shotResult.pottedBalls.any { !it.isCueBall }
            if (anyObjectPotted) {
                ballInHand = false
            } else {
                ballInHand = false
                switchTurn()
            }
        }
    }

    private fun respawnEightBall(physics: PoolPhysicsEngine) {
        physics.balls.removeAll { it.isEightBall }
        val footSpot = Vector3(0f, physics.tableLength / 4f, 0f)
        physics.balls.add(Ball.createDefaultBall(8, footSpot))
    }

    private fun respawnNineBall(physics: PoolPhysicsEngine) {
        physics.balls.removeAll { it.number == 9 }
        val footSpot = Vector3(0f, physics.tableLength / 4f, 0f)
        physics.balls.add(Ball.createDefaultBall(9, footSpot))
    }

    fun placeCueBallInHand(physics: PoolPhysicsEngine, targetPos: Vector3): Boolean {
        val r = physics.ballRadius
        val minX = physics.minX + r
        val maxX = physics.maxX - r
        val minY = physics.minY + r
        val maxY = physics.maxY - r

        if (ballInHandBehindHeadstring) {
            val headstringY = -physics.tableLength / 4f
            if (targetPos.y > headstringY) return false
        }

        val clampedX = targetPos.x.coerceIn(minX, maxX)
        val clampedY = targetPos.y.coerceIn(minY, maxY)
        val finalPos = Vector3(clampedX, clampedY, 0f)

        val collision = physics.balls.any { !it.isCueBall && it.state == BallState.ACTIVE && it.position.distanceTo2D(finalPos) < r * 2f }
        if (collision) return false

        val cueBall = physics.getCueBall()
        if (cueBall != null) {
            cueBall.position = finalPos
            cueBall.velocity = Vector3.ZERO
            cueBall.angularVelocity = Vector3.ZERO
            cueBall.state = BallState.ACTIVE
        } else {
            physics.balls.removeAll { it.isCueBall }
            physics.balls.add(Ball.createDefaultBall(0, finalPos))
        }

        return true
    }

    fun clearBallInHand() {
        ballInHand = false
        ballInHandBehindHeadstring = false
    }
}
