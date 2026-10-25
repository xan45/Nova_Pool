package com.xandron.novapool.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xandron.novapool.ai.PoolAIEngine
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.game.AIDifficulty
import com.xandron.novapool.game.BallGroup
import com.xandron.novapool.game.FoulType
import com.xandron.novapool.game.MatchMode
import com.xandron.novapool.game.PlayerType
import com.xandron.novapool.game.PoolGameRulesEngine
import com.xandron.novapool.game.TournamentManager
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.CameraMode
import com.xandron.novapool.model.CueState
import com.xandron.novapool.model.GameMode
import com.xandron.novapool.model.MatchRewardResult
import com.xandron.novapool.model.ProgressionState
import com.xandron.novapool.physics.PoolPhysicsEngine
import com.xandron.novapool.rendering.Vector3
import kotlinx.coroutines.delay

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun PoolGameScreen(
    progressionState: ProgressionState = ProgressionState(),
    repository: ProgressionRepository? = null,
    tournamentManager: TournamentManager? = null,
    gameMode: GameMode = GameMode.EIGHT_BALL,
    matchMode: MatchMode = MatchMode.VS_AI,
    aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    isTournamentMatch: Boolean = false,
    onExitGame: () -> Unit = {}
) {
    // Lock screen to Landscape orientation while playing pool game
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    val physics = remember { PoolPhysicsEngine() }
    val gameRules = remember { PoolGameRulesEngine().apply { initMatch(gameMode, matchMode, aiDifficulty) } }
    val aiEngine = remember { PoolAIEngine() }

    // Initialize cue state with 50% default power so initial strike is ready
    var cueState by remember { mutableStateOf(CueState(power = 0.5f)) }
    var cameraMode by remember { mutableStateOf(CameraMode.AIM_PERSPECTIVE) }

    var showSpinModal by remember { mutableStateOf(false) }
    var showMatchSetupDialog by remember { mutableStateOf(false) }

    var showFoulBanner by remember { mutableStateOf(false) }
    var foulBannerText by remember { mutableStateOf("") }
    var isAiThinking by remember { mutableStateOf(false) }

    var rewardResult by remember { mutableStateOf<MatchRewardResult?>(null) }

    LaunchedEffect(gameRules.isGameOver) {
        if (gameRules.isGameOver && rewardResult == null) {
            val userWon = gameRules.winner == gameRules.player1
            val pottedCount = physics.balls.count { !it.isCueBall && it.state == BallState.POTTED }
            rewardResult = repository?.recordMatchResult(userWon, pottedCount)

            if (isTournamentMatch && tournamentManager != null) {
                tournamentManager.recordUserMatchResult(userWon)
            }
        }
    }

    // Wire shot completed listener
    LaunchedEffect(physics, gameRules) {
        physics.onShotCompletedListener = { shotResult ->
            val foul = gameRules.evaluateShot(shotResult, physics)
            if (foul != FoulType.NONE) {
                foulBannerText = foul.description
                showFoulBanner = true
            }
            if (foul == FoulType.SCRATCH) {
                physics.respawnCueBallIfNeeded()
            }
        }
    }

    // Auto-dismiss foul banner after 3 seconds
    LaunchedEffect(showFoulBanner) {
        if (showFoulBanner) {
            delay(3000)
            showFoulBanner = false
        }
    }

    // AI Turn Automation Loop
    LaunchedEffect(gameRules.currentPlayerIndex, gameRules.isGameOver, physics.isShotInProgress) {
        val curPlayer = gameRules.getCurrentPlayer()
        if (!gameRules.isGameOver && curPlayer.type == PlayerType.AI_BOT && !physics.isAnyBallMoving() && !cueState.isStriking) {
            isAiThinking = true
            delay(600)

            val decision = aiEngine.calculateBestShot(physics, gameRules, curPlayer.aiDifficulty)
            if (decision != null) {
                if (decision.cuePlacementPos != null && gameRules.ballInHand) {
                    gameRules.placeCueBallInHand(physics, decision.cuePlacementPos)
                    gameRules.clearBallInHand()
                }

                // Aiming animation
                val startAngle = cueState.angle
                val targetAngle = decision.targetAngle
                var diff = targetAngle - startAngle
                while (diff < -Math.PI) diff += (2 * Math.PI).toFloat()
                while (diff > Math.PI) diff -= (2 * Math.PI).toFloat()

                val steps = 15
                for (step in 1..steps) {
                    delay(30)
                    val curA = startAngle + (diff * (step / steps.toFloat()))
                    cueState = cueState.copy(angle = curA)
                }

                // Power charging animation
                val pSteps = 10
                for (step in 1..pSteps) {
                    delay(25)
                    cueState = cueState.copy(power = decision.power * (step / pSteps.toFloat()))
                }

                delay(150)
                isAiThinking = false
                cueState = cueState.copy(isStriking = true, strikeProgress = 0f)
            } else {
                isAiThinking = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 1. 3D Pool Table Canvas (Full view)
        PoolTableCanvas(
            physics = physics,
            gameRules = gameRules,
            cueState = cueState,
            cameraMode = cameraMode,
            onCueStateChanged = { newCueState -> cueState = newCueState },
            equippedCue = progressionState.equippedCue,
            equippedFelt = progressionState.equippedFelt,
            equippedFrame = progressionState.equippedFrame
        )

        // 2. Custom Sleek Floating HUD Header (Top)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit / Pause Icon Button
                IconButton(
                    onClick = onExitGame,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Exit Match",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Player 1 HUD Card
                PlayerHUDCard(
                    player = gameRules.player1,
                    isActive = gameRules.currentPlayerIndex == 0 && !gameRules.isGameOver,
                    modifier = Modifier.weight(1f)
                )

                // Center VS & Game Mode Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0EA5E9).copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = gameRules.gameMode.name.replace("_", "-"),
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "VS",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }

                // Player 2 / AI HUD Card
                PlayerHUDCard(
                    player = gameRules.player2,
                    isActive = gameRules.currentPlayerIndex == 1 && !gameRules.isGameOver,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Action Buttons: Camera view, Match settings, Reset rack
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            cameraMode = when (cameraMode) {
                                CameraMode.AIM_PERSPECTIVE -> CameraMode.TOP_DOWN
                                CameraMode.TOP_DOWN -> CameraMode.ISOMETRIC_3D
                                CameraMode.ISOMETRIC_3D -> CameraMode.AIM_PERSPECTIVE
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = when (cameraMode) {
                                CameraMode.AIM_PERSPECTIVE -> Icons.Rounded.CenterFocusStrong
                                CameraMode.TOP_DOWN -> Icons.Rounded.GridOn
                                CameraMode.ISOMETRIC_3D -> Icons.Rounded.SportsEsports
                            },
                            contentDescription = "Camera View",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showMatchSetupDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            gameRules.initMatch(gameRules.gameMode, gameRules.matchMode, gameRules.player2.aiDifficulty)
                            physics.setupRack(gameRules.gameMode)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 3. AI Thinking status indicator (Floating below HUD)
        if (isAiThinking) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        text = "AI Bot Aiming & Target Calculation...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Foul Banner Popup (Floating below HUD)
        AnimatedVisibility(
            visible = showFoulBanner,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 58.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFDC2626),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Warning, contentDescription = "Foul", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        text = foulBannerText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 5. Cue Ball In Hand Banner (Floating below HUD)
        if (gameRules.ballInHand && !gameRules.isGameOver && gameRules.getCurrentPlayer().type == PlayerType.HUMAN) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (gameRules.ballInHandBehindHeadstring) "CUE BALL IN HAND (Behind Headstring)" else "CUE BALL IN HAND (Drag table to place)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { gameRules.clearBallInHand() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Done", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Potted balls row (Floating below HUD)
        val pottedBalls = physics.balls.filter { it.state == BallState.POTTED && !it.isCueBall }
        if (pottedBalls.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 92.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Potted:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(pottedBalls) { ball ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(ball.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ball.number.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Bottom-Left Corner Controls (Fine Aim & Spin / English)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fine Aim Nudge Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newAngle = cueState.angle - 0.008f
                            cueState = cueState.copy(angle = newAngle)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.RotateLeft,
                            contentDescription = "Nudge Left",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "FINE AIM",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    IconButton(
                        onClick = {
                            val newAngle = cueState.angle + 0.008f
                            cueState = cueState.copy(angle = newAngle)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.RotateRight,
                            contentDescription = "Nudge Right",
                            tint = Color.White
                        )
                    }
                }
            }

            // Spin / English Button
            Button(
                onClick = { showSpinModal = !showSpinModal },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.55f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(38.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("SPIN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 8. Right-Side Sleek Translucent Vertical Power Meter & Integrated STRIKE Button
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, top = 50.dp, bottom = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.55f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "POWER",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "${(cueState.power * 100).toInt()}%",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                val isHumanTurn = gameRules.getCurrentPlayer().type == PlayerType.HUMAN && !physics.isAnyBallMoving() && !gameRules.isGameOver

                // Custom Vertical Drag Power Track
                Box(
                    modifier = Modifier
                        .height(130.dp)
                        .width(22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .pointerInput(isHumanTurn) {
                            if (isHumanTurn) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val trackHeight = size.height.toFloat()
                                        val touchY = change.position.y
                                        val rawP = 1.0f - (touchY / trackHeight)
                                        val newPower = rawP.coerceIn(0.05f, 1.0f)
                                        cueState = cueState.copy(power = newPower)
                                    },
                                    onDragEnd = {
                                        if (cueState.power > 0.05f && !cueState.isStriking) {
                                            if (gameRules.ballInHand) {
                                                gameRules.clearBallInHand()
                                            }
                                            cueState = cueState.copy(isStriking = true, strikeProgress = 0f)
                                        }
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((130 * cueState.power).dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444),
                                        Color(0xFFF59E0B),
                                        Color(0xFF10B981)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Integrated Prominent STRIKE Button
                val isStrikeEnabled = isHumanTurn && !cueState.isStriking
                Button(
                    onClick = {
                        if (isHumanTurn && !cueState.isStriking) {
                            val effectivePower = if (cueState.power <= 0.05f) 0.5f else cueState.power
                            if (gameRules.ballInHand) {
                                gameRules.clearBallInHand()
                            }
                            cueState = cueState.copy(power = effectivePower, isStriking = true, strikeProgress = 0f)
                        }
                    },
                    enabled = isStrikeEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlashOn,
                            contentDescription = "Strike",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "STRIKE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 9. Spin Modal Dialog
        AnimatedVisibility(
            visible = showSpinModal,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E293B),
                shadowElevation = 12.dp,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CUE BALL SPIN / ENGLISH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color.Gray, CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val spinX = ((offset.x - centerX) / centerX).coerceIn(-1f, 1f)
                                    val spinY = (-(offset.y - centerY) / centerY).coerceIn(-1f, 1f)
                                    cueState = cueState.copy(spinOffset = Vector3(spinX, spinY, 0f))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val dotOffsetX = (cueState.spinOffset.x * 50).dp
                        val dotOffsetY = (-cueState.spinOffset.y * 50).dp

                        Box(
                            modifier = Modifier
                                .padding(
                                    start = if (dotOffsetX > 0.dp) dotOffsetX else 0.dp,
                                    end = if (dotOffsetX < 0.dp) -dotOffsetX else 0.dp
                                )
                                .padding(
                                    top = if (dotOffsetY > 0.dp) dotOffsetY else 0.dp,
                                    bottom = if (dotOffsetY < 0.dp) -dotOffsetY else 0.dp
                                )
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showSpinModal = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }

        // 10. Match Victory / Defeat Overlay Dialog with Progression Rewards
        if (gameRules.isGameOver) {
            val winner = gameRules.winner
            val isUserWin = winner == gameRules.player1
            AlertDialog(
                onDismissRequest = { },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "Victory",
                        tint = if (isUserWin) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = {
                    Text(
                        text = if (isUserWin) "VICTORY!" else if (winner != null) "${winner.name.uppercase()} WINS" else "MATCH OVER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (isUserWin) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = gameRules.winReason.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        rewardResult?.let { rewards ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "MATCH EARNINGS & REWARDS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Coins Earned:")
                                        Text("+${rewards.coinsEarned} 🪙", fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("XP Earned:")
                                        Text("+${rewards.xpEarned} XP", fontWeight = FontWeight.Bold)
                                    }
                                    if (rewards.winStreakBonus > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Win Streak Bonus (${rewards.currentStreak} 🔥):")
                                            Text("+${rewards.winStreakBonus} Coins", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                        }
                                    }

                                    if (rewards.leveledUp) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "🎉 LEVEL UP! REACHED LEVEL ${rewards.newLevel} 🎉",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF6EE7B7)
                                                    )
                                                )
                                                Text(
                                                    text = "Level Bonus: +${rewards.levelUpBonusCoins} Coins, +${rewards.levelUpBonusGems} Gems",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            rewardResult = null
                            cueState = CueState(power = 0.5f)
                            gameRules.initMatch(gameRules.gameMode, gameRules.matchMode, gameRules.player2.aiDifficulty)
                            physics.setupRack(gameRules.gameMode)
                        }
                    ) {
                        Text("Play Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onExitGame
                    ) {
                        Text("Main Menu")
                    }
                }
            )
        }

        // 11. Game Setup / Settings Dialog
        if (showMatchSetupDialog) {
            var selectedGameMode by remember { mutableStateOf(gameRules.gameMode) }
            var selectedMatchMode by remember { mutableStateOf(gameRules.matchMode) }
            var selectedDifficulty by remember { mutableStateOf(gameRules.player2.aiDifficulty) }

            AlertDialog(
                onDismissRequest = { showMatchSetupDialog = false },
                title = { Text("Match Configuration", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Game Rules Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GameMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = selectedGameMode == mode,
                                        onClick = { selectedGameMode = mode },
                                        label = { Text(mode.name.replace("_", "-")) }
                                    )
                                }
                            }
                        }

                        Column {
                            Text("Opponent Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MatchMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = selectedMatchMode == mode,
                                        onClick = { selectedMatchMode = mode },
                                        label = { Text(mode.displayName) }
                                    )
                                }
                            }
                        }

                        if (selectedMatchMode == MatchMode.VS_AI) {
                            Column {
                                Text("AI Bot Difficulty:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AIDifficulty.entries.forEach { diff ->
                                        FilterChip(
                                            selected = selectedDifficulty == diff,
                                            onClick = { selectedDifficulty = diff },
                                            label = { Text(diff.displayName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            cueState = CueState(power = 0.5f)
                            gameRules.initMatch(selectedGameMode, selectedMatchMode, selectedDifficulty)
                            physics.setupRack(selectedGameMode)
                            showMatchSetupDialog = false
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                            Text("Start Match")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showMatchSetupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerHUDCard(
    player: com.xandron.novapool.game.Player,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.Transparent,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (player.type == PlayerType.AI_BOT) Color(0xFF6366F1) else Color(0xFF0EA5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (player.type == PlayerType.AI_BOT) Icons.Rounded.Psychology else Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = player.group.displayName,
                    color = if (player.group == BallGroup.SOLIDS) Color(0xFFFFD700) else if (player.group == BallGroup.STRIPES) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
