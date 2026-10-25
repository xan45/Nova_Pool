package com.xandron.novapool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MonetizationOn
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xandron.novapool.data.ProgressionRepository
import com.xandron.novapool.model.FeltColorOption
import com.xandron.novapool.model.ProgressionState
import com.xandron.novapool.model.WoodFinishOption
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableCustomizationScreen(
    progressionState: ProgressionState,
    repository: ProgressionRepository,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Felt Cloth, 1: Wood Frame

    var selectedFeltItemForPurchase by remember { mutableStateOf<FeltColorOption?>(null) }
    var selectedFrameItemForPurchase by remember { mutableStateOf<WoodFinishOption?>(null) }
    var purchaseWithGems by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TABLE CUSTOMIZATION",
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
            // Live Interactive Table Preview Card
            TablePreviewCard(
                equippedFelt = progressionState.equippedFelt,
                equippedFrame = progressionState.equippedFrame
            )

            // Category Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Felt Cloth Colors", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Wood Frame Finishes", fontWeight = FontWeight.Bold) }
                )
            }

            // Catalog Grid based on tab
            if (selectedTab == 0) {
                FeltColorCatalogGrid(
                    progressionState = progressionState,
                    onEquipFelt = { felt ->
                        val res = repository.equipFelt(felt)
                        if (res.isSuccess) {
                            scope.launch { snackbarHostState.showSnackbar("Equipped ${felt.displayName}") }
                        }
                    },
                    onBuyFelt = { felt, useGems ->
                        selectedFeltItemForPurchase = felt
                        purchaseWithGems = useGems
                    }
                )
            } else {
                WoodFrameCatalogGrid(
                    progressionState = progressionState,
                    onEquipFrame = { frame ->
                        val res = repository.equipFrame(frame)
                        if (res.isSuccess) {
                            scope.launch { snackbarHostState.showSnackbar("Equipped ${frame.displayName}") }
                        }
                    },
                    onBuyFrame = { frame, useGems ->
                        selectedFrameItemForPurchase = frame
                        purchaseWithGems = useGems
                    }
                )
            }
        }
    }

    // Purchase Dialog for Felt
    selectedFeltItemForPurchase?.let { felt ->
        val price = if (purchaseWithGems) felt.priceGems else felt.priceCoins
        val currencyName = if (purchaseWithGems) "Gems" else "Coins"
        val userBalance = if (purchaseWithGems) progressionState.gems else progressionState.coins
        val canAfford = userBalance >= price

        AlertDialog(
            onDismissRequest = { selectedFeltItemForPurchase = null },
            title = { Text("Unlock ${felt.displayName} Felt") },
            text = { Text("Price: $price $currencyName (Your Balance: $userBalance $currencyName)") },
            confirmButton = {
                Button(
                    enabled = canAfford,
                    onClick = {
                        val res = repository.unlockFelt(felt, useGems = purchaseWithGems)
                        selectedFeltItemForPurchase = null
                        if (res.isSuccess) {
                            scope.launch { snackbarHostState.showSnackbar("Unlocked and equipped ${felt.displayName}!") }
                        }
                    }
                ) { Text("Confirm Purchase") }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedFeltItemForPurchase = null }) { Text("Cancel") }
            }
        )
    }

    // Purchase Dialog for Frame
    selectedFrameItemForPurchase?.let { frame ->
        val price = if (purchaseWithGems) frame.priceGems else frame.priceCoins
        val currencyName = if (purchaseWithGems) "Gems" else "Coins"
        val userBalance = if (purchaseWithGems) progressionState.gems else progressionState.coins
        val canAfford = userBalance >= price

        AlertDialog(
            onDismissRequest = { selectedFrameItemForPurchase = null },
            title = { Text("Unlock ${frame.displayName} Frame") },
            text = { Text("Price: $price $currencyName (Your Balance: $userBalance $currencyName)") },
            confirmButton = {
                Button(
                    enabled = canAfford,
                    onClick = {
                        val res = repository.unlockFrame(frame, useGems = purchaseWithGems)
                        selectedFrameItemForPurchase = null
                        if (res.isSuccess) {
                            scope.launch { snackbarHostState.showSnackbar("Unlocked and equipped ${frame.displayName}!") }
                        }
                    }
                ) { Text("Confirm Purchase") }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedFrameItemForPurchase = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TablePreviewCard(
    equippedFelt: FeltColorOption,
    equippedFrame: WoodFinishOption
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Render Wood Frame Outer
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(equippedFrame.outerColor, equippedFrame.innerColor, equippedFrame.outerColor)
                    ),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )

                // Render Felt Surface Inner
                val frameMargin = 28f
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(equippedFelt.centerColor, equippedFelt.edgeColor),
                        center = Offset(w / 2f, h / 2f),
                        radius = w * 0.6f
                    ),
                    topLeft = Offset(frameMargin, frameMargin),
                    size = Size(w - frameMargin * 2, h - frameMargin * 2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )

                // Draw 6 Pockets
                val pockets = listOf(
                    Offset(frameMargin, frameMargin),
                    Offset(w / 2f, frameMargin),
                    Offset(w - frameMargin, frameMargin),
                    Offset(frameMargin, h - frameMargin),
                    Offset(w / 2f, h - frameMargin),
                    Offset(w - frameMargin, h - frameMargin)
                )
                pockets.forEach { pos ->
                    drawCircle(Color.Black, radius = 18f, center = pos)
                }

                // Draw Headstring
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(w * 0.3f, frameMargin),
                    end = Offset(w * 0.3f, h - frameMargin),
                    strokeWidth = 2f
                )

                // Draw Cue ball & 8-Ball preview
                drawCircle(Color.White, radius = 10f, center = Offset(w * 0.3f, h / 2f))
                drawCircle(Color.Black, radius = 10f, center = Offset(w * 0.7f, h / 2f))
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "${equippedFelt.displayName} • ${equippedFrame.displayName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FeltColorCatalogGrid(
    progressionState: ProgressionState,
    onEquipFelt: (FeltColorOption) -> Unit,
    onBuyFelt: (FeltColorOption, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeltColorOption.entries.forEach { felt ->
            val isUnlocked = progressionState.unlockedFeltIds.contains(felt.id)
            val isEquipped = progressionState.equippedFeltId == felt.id

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Felt Color Swatch
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(felt.centerColor, felt.edgeColor)
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = felt.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            if (isEquipped) {
                                Text(
                                    text = "Currently Equipped",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    if (isUnlocked) {
                        if (!isEquipped) {
                            Button(
                                onClick = { onEquipFelt(felt) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Equip")
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (felt.priceCoins > 0) {
                                Button(
                                    onClick = { onBuyFelt(felt, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                ) {
                                    Icon(Icons.Rounded.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${felt.priceCoins}")
                                }
                            }
                            if (felt.priceGems > 0) {
                                Button(
                                    onClick = { onBuyFelt(felt, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${felt.priceGems}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WoodFrameCatalogGrid(
    progressionState: ProgressionState,
    onEquipFrame: (WoodFinishOption) -> Unit,
    onBuyFrame: (WoodFinishOption, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WoodFinishOption.entries.forEach { frame ->
            val isUnlocked = progressionState.unlockedFrameIds.contains(frame.id)
            val isEquipped = progressionState.equippedFrameId == frame.id

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = if (isEquipped) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Frame Color Swatch
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(frame.outerColor, frame.innerColor)
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = frame.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            if (isEquipped) {
                                Text(
                                    text = "Currently Equipped",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    if (isUnlocked) {
                        if (!isEquipped) {
                            Button(
                                onClick = { onEquipFrame(frame) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Equip")
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (frame.priceCoins > 0) {
                                Button(
                                    onClick = { onBuyFrame(frame, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                ) {
                                    Icon(Icons.Rounded.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${frame.priceCoins}")
                                }
                            }
                            if (frame.priceGems > 0) {
                                Button(
                                    onClick = { onBuyFrame(frame, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${frame.priceGems}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
