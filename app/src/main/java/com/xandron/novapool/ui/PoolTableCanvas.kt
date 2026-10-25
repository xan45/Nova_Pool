package com.xandron.novapool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import com.xandron.novapool.game.PoolGameRulesEngine
import com.xandron.novapool.model.CameraMode
import com.xandron.novapool.model.CueState
import com.xandron.novapool.model.TrajectoryPrediction
import com.xandron.novapool.physics.PoolPhysicsEngine
import com.xandron.novapool.rendering.PoolTable3DRenderer
import com.xandron.novapool.rendering.Vector3
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.PI

import com.xandron.novapool.model.CueItem
import com.xandron.novapool.model.FeltColorOption
import com.xandron.novapool.model.WoodFinishOption

/**
 * Interactive Jetpack Compose Canvas hosting 3D pool table rendering and touch aiming gestures.
 */
@Composable
fun PoolTableCanvas(
    physics: PoolPhysicsEngine,
    gameRules: PoolGameRulesEngine,
    cueState: CueState,
    cameraMode: CameraMode,
    onCueStateChanged: (CueState) -> Unit,
    equippedCue: CueItem = CueItem.DEFAULT_CUE,
    equippedFelt: FeltColorOption = FeltColorOption.EMERALD_GREEN,
    equippedFrame: WoodFinishOption = WoodFinishOption.WALNUT,
    modifier: Modifier = Modifier
) {
    val renderer = remember { PoolTable3DRenderer() }
    val textMeasurer = rememberTextMeasurer()

    var trajectory by remember { mutableStateOf<TrajectoryPrediction?>(null) }
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(physics) {
        while (true) {
            val frameTime = awaitFrame()
            if (lastFrameTimeNanos != 0L) {
                val dt = ((frameTime - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                physics.update(dt)

                if (!physics.isAnyBallMoving()) {
                    trajectory = physics.calculateAimTrajectory(cueState, maxDistance = equippedCue.aimLengthPx)
                }

                if (cueState.isStriking) {
                    val newProgress = cueState.strikeProgress + dt * 6f
                    if (newProgress >= 1.0f) {
                        cueState.isStriking = false
                        cueState.strikeProgress = 0f
                        val effectivePower = if (cueState.power <= 0.05f) 0.5f else cueState.power
                        val boostedImpulse = 1400f * equippedCue.powerMultiplier
                        physics.strikeCueBall(cueState.copy(power = effectivePower), maxImpulse = boostedImpulse)
                        cueState.power = 0.5f
                    } else {
                        cueState.strikeProgress = newProgress
                    }
                    onCueStateChanged(cueState.copy())
                }
            }
            lastFrameTimeNanos = frameTime
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(physics, cueState, cameraMode, gameRules.ballInHand) {
                if (gameRules.ballInHand) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val normX = (offset.x - size.width / 2f) / (size.width / 2f)
                            val normY = -(offset.y - size.height / 2f) / (size.height / 2f)
                            val worldX = normX * (physics.tableWidth / 2f)
                            val worldY = normY * (physics.tableLength / 2f)
                            gameRules.placeCueBallInHand(physics, Vector3(worldX, worldY, 0f))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val normX = (change.position.x - size.width / 2f) / (size.width / 2f)
                            val normY = -(change.position.y - size.height / 2f) / (size.height / 2f)
                            val worldX = normX * (physics.tableWidth / 2f)
                            val worldY = normY * (physics.tableLength / 2f)
                            gameRules.placeCueBallInHand(physics, Vector3(worldX, worldY, 0f))
                        }
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (!physics.isAnyBallMoving()) {
                                val sensitivity = 0.004f * (100f / maxOf(50, equippedCue.stabilityStat))
                                val deltaAngle = dragAmount.x * sensitivity
                                var newAngle = cueState.angle + deltaAngle

                                while (newAngle < 0) newAngle += (2 * PI).toFloat()
                                while (newAngle >= 2 * PI) newAngle -= (2 * PI).toFloat()

                                cueState.angle = newAngle
                                trajectory = physics.calculateAimTrajectory(cueState, maxDistance = equippedCue.aimLengthPx)
                                onCueStateChanged(cueState.copy(angle = newAngle))
                            }
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            renderer.render(
                drawScope = this,
                physics = physics,
                cueState = cueState,
                cameraMode = cameraMode,
                trajectory = trajectory,
                textMeasurer = textMeasurer,
                ballInHand = gameRules.ballInHand,
                ballInHandBehindHeadstring = gameRules.ballInHandBehindHeadstring,
                equippedCue = equippedCue,
                equippedFelt = equippedFelt,
                equippedFrame = equippedFrame
            )
        }
    }
}
