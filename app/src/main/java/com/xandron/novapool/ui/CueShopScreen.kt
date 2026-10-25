package com.xandron.novapool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.model.CueItem
import com.xandron.novapool.model.CueRarity
import com.xandron.novapool.model.ProgressionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CueShopScreen(
    progressionState: ProgressionState,
    repository: ProgressionRepository,
    onBackClick: () -> Unit
) {
    var selectedRarityFilter by remember { mutableStateOf<CueRarity?>(null) }
    var selectedCueForPurchase by remember { mutableStateOf<CueItem?>(null) }
    var purchaseWithGems by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredCues = CueItem.CATALOG.filter { cue ->
        selectedRarityFilter == null || cue.rarity == selectedRarityFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CUE SHOP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
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
                    // Wallet Chips
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${progressionState.coins}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFEF08A)
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF06B6D4).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Gems",
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${progressionState.gems}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCFFAFE)
                                    )
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Rarity Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRarityFilter == null,
                        onClick = { selectedRarityFilter = null },
                        label = { Text("All Cues (${CueItem.CATALOG.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                }

                items(CueRarity.entries) { rarity ->
                    FilterChip(
                        selected = selectedRarityFilter == rarity,
                        onClick = { selectedRarityFilter = if (selectedRarityFilter == rarity) null else rarity },
                        label = { Text(rarity.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(rarity.badgeColor),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            // Cue Catalog List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCues) { cue ->
                    val isUnlocked = progressionState.unlockedCueIds.contains(cue.id)
                    val isEquipped = progressionState.equippedCueId == cue.id

                    CueCatalogCard(
                        cue = cue,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        onEquipClick = {
                            val res = repository.equipCue(cue.id)
                            if (res.isSuccess) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Equipped ${cue.name}!")
                                }
                            }
                        },
                        onBuyClick = { useGems ->
                            selectedCueForPurchase = cue
                            purchaseWithGems = useGems
                        }
                    )
                }
            }
        }
    }

    // Purchase Confirmation Dialog
    selectedCueForPurchase?.let { cue ->
        val price = if (purchaseWithGems) cue.priceGems else cue.priceCoins
        val currencyName = if (purchaseWithGems) "Gems" else "Coins"
        val userBalance = if (purchaseWithGems) progressionState.gems else progressionState.coins
        val canAfford = userBalance >= price

        AlertDialog(
            onDismissRequest = { selectedCueForPurchase = null },
            title = {
                Text(
                    text = "UNLOCK ${cue.name.uppercase()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(cue.description)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Price: $price $currencyName")
                            Text("Your Balance: $userBalance $currencyName")
                        }
                    }
                    if (!canAfford) {
                        Text(
                            text = "Insufficient $currencyName balance!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = canAfford,
                    onClick = {
                        val res = repository.unlockCue(cue.id, useGems = purchaseWithGems)
                        selectedCueForPurchase = null
                        if (res.isSuccess) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Unlocked and equipped ${cue.name}!")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Purchase failed")
                            }
                        }
                    }
                ) {
                    Text("Confirm Purchase")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedCueForPurchase = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CueCatalogCard(
    cue: CueItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquipClick: () -> Unit,
    onBuyClick: (useGems: Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Cue Name + Rarity Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cue.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        if (isEquipped) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Equipped",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "EQUIPPED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF6EE7B7),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = cue.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.65f)
                        ),
                        maxLines = 2
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(cue.rarity.badgeColor).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(cue.rarity.badgeColor))
                ) {
                    Text(
                        text = cue.rarity.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(cue.rarity.badgeColor)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2D Visual Cue Stick Representation Bar
            CueVisualBar(cue = cue)

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Section (Power, Spin, Aim, Stability)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CueStatRow(label = "Power", value = cue.powerStat, color = Color(0xFFEF4444))
                CueStatRow(label = "Spin", value = cue.spinStat, color = Color(0xFF3B82F6))
                CueStatRow(label = "Aim Guide", value = cue.aimStat, color = Color(0xFF10B981))
                CueStatRow(label = "Stability", value = cue.stabilityStat, color = Color(0xFFF59E0B))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUnlocked) {
                    if (isEquipped) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                disabledContentColor = Color(0xFF6EE7B7)
                            )
                        ) {
                            Text("Equipped")
                        }
                    } else {
                        Button(
                            onClick = onEquipClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Equip Cue")
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (cue.priceCoins > 0) {
                            Button(
                                onClick = { onBuyClick(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MonetizationOn,
                                    contentDescription = "Coins",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${cue.priceCoins}")
                            }
                        }

                        if (cue.priceGems > 0) {
                            Button(
                                onClick = { onBuyClick(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Gems",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${cue.priceGems}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CueVisualBar(cue: CueItem) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
    ) {
        val h = size.height

        // Background stick shaft gradient
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    cue.primaryColor,
                    cue.secondaryColor,
                    cue.accentColor
                )
            ),
            size = size
        )

        // Cue Tip accent at tip end
        drawLine(
            color = cue.tipColor,
            start = Offset(0f, 0f),
            end = Offset(0f, h),
            strokeWidth = 12f
        )
    }
}

@Composable
private fun CueStatRow(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(80.dp)
        )

        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$value/100",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            ),
            modifier = Modifier.width(50.dp)
        )
    }
}
