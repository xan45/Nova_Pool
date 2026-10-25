package com.xandron.novapool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.game.TournamentManager
import com.xandron.novapool.model.ProgressionState
import com.xandron.novapool.model.TournamentMatch
import com.xandron.novapool.model.TournamentRound
import com.xandron.novapool.model.TournamentState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTournamentScreen(
    progressionState: ProgressionState,
    tournamentState: TournamentState?,
    tournamentManager: TournamentManager,
    repository: ProgressionRepository,
    onPlayTournamentMatch: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showVictoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tournamentState?.currentRound, tournamentState?.champion) {
        if (tournamentState?.currentRound == TournamentRound.COMPLETED && tournamentState.champion?.isHuman == true) {
            showVictoryDialog = true
            repository.addTournamentWin(tournamentState.prizeCoins, tournamentState.prizeGems)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LOCAL TOURNAMENT BRACKET",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tournamentManager.startNewTournament(progressionState.playerName)
                    }) {
                        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "New Tournament", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (tournamentState == null) {
                // Tournament Start Banner
                TournamentStartHeroCard(
                    onStartTournament = {
                        tournamentManager.startNewTournament(progressionState.playerName)
                    }
                )
            } else {
                // Header Status Card
                TournamentHeaderCard(
                    state = tournamentState,
                    onPlayNextMatch = onPlayTournamentMatch
                )

                // Bracket Layout
                Text(
                    text = "KNOCKOUT BRACKET",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.2.sp
                    )
                )

                // Quarter Finals
                RoundSection(
                    title = "Quarter-Finals (8 Players)",
                    matches = tournamentState.matches.filter { it.round == TournamentRound.QUARTER_FINALS }
                )

                // Semi Finals
                if (tournamentState.matches.any { it.round == TournamentRound.SEMI_FINALS }) {
                    RoundSection(
                        title = "Semi-Finals (4 Players)",
                        matches = tournamentState.matches.filter { it.round == TournamentRound.SEMI_FINALS }
                    )
                }

                // Finals
                if (tournamentState.matches.any { it.round == TournamentRound.FINALS }) {
                    RoundSection(
                        title = "Championship Finals",
                        matches = tournamentState.matches.filter { it.round == TournamentRound.FINALS }
                    )
                }
            }
        }
    }

    if (showVictoryDialog && tournamentState != null) {
        AlertDialog(
            onDismissRequest = { showVictoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TOURNAMENT CHAMPION!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Congratulations ${progressionState.playerName}! You defeated all opponents and won the 8-Ball Championship Cup!")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Prizes Awarded:", fontWeight = FontWeight.Bold)
                            Text("🏆 1x Championship Trophy")
                            Text("🪙 +${tournamentState.prizeCoins} Coins")
                            Text("💎 +${tournamentState.prizeGems} Gems")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVictoryDialog = false
                        tournamentManager.startNewTournament(progressionState.playerName)
                    }
                ) {
                    Text("Start New Tournament")
                }
            }
        )
    }
}

@Composable
private fun TournamentStartHeroCard(onStartTournament: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "8-PLAYER CHAMPIONSHIP CUP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Compete against 7 top AI pool contenders in a single elimination knockout tournament!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = onStartTournament,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enter Tournament Bracket (Prize: 1500 Coins + 25 Gems)")
            }
        }
    }
}

@Composable
private fun TournamentHeaderCard(
    state: TournamentState,
    onPlayNextMatch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Current Stage: ${state.currentRound.title}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Prize: ${state.prizeCoins} Coins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF08A)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!state.isUserEliminated && state.currentRound != TournamentRound.COMPLETED) {
                Button(
                    onClick = onPlayNextMatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Next Match (${state.currentRound.title})")
                }
            } else if (state.isUserEliminated) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "You were eliminated from the tournament. Tap refresh icon top-right to try again!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundSection(title: String, matches: List<TournamentMatch>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        )

        matches.forEach { match ->
            TournamentMatchCard(match = match)
        }
    }
}

@Composable
private fun TournamentMatchCard(match: TournamentMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (match.isUserMatch) Color(0xFF1E3A8A) else Color(0xFF1E293B)
        ),
        border = if (match.isUserMatch) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF3B82F6)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerMatchRow(
                player = match.player1,
                isWinner = match.winner?.id == match.player1.id,
                score = match.scorePlayer1
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {}
            PlayerMatchRow(
                player = match.player2,
                isWinner = match.winner?.id == match.player2.id,
                score = match.scorePlayer2
            )
        }
    }
}

@Composable
private fun PlayerMatchRow(
    player: com.xandron.novapool.model.TournamentPlayer,
    isWinner: Boolean,
    score: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = if (player.isHuman) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = player.name + (if (player.isHuman) " (YOU)" else ""),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                    color = if (isWinner) Color(0xFFFEF08A) else Color.White
                )
            )
        }

        if (isWinner) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.3f)
            ) {
                Text(
                    text = "WINNER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A),
                        fontSize = 9.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
