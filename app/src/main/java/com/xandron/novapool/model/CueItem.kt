package com.xandron.novapool.model

import androidx.compose.ui.graphics.Color

enum class CueRarity(val displayName: String, val badgeColor: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFF9800),
    MYTHIC("Mythic", 0xFFE91E63)
}

/**
 * Data model for cue stick items available in Nova Pool.
 */
data class CueItem(
    val id: String,
    val name: String,
    val description: String,
    val rarity: CueRarity,
    val powerStat: Int,     // 0..100 -> affects shot velocity boost
    val spinStat: Int,      // 0..100 -> affects spin transfer & english
    val aimStat: Int,       // 0..100 -> affects trajectory guideline length
    val stabilityStat: Int, // 0..100 -> affects aim precision & cue stability
    val priceCoins: Int,
    val priceGems: Int,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val tipColorHex: Long = 0xFFFFFFFF
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
    val tipColor: Color get() = Color(tipColorHex)

    // Multipliers derived from stats
    val powerMultiplier: Float get() = 0.85f + (powerStat / 100f) * 0.45f // 0.85 to 1.30
    val spinMultiplier: Float get() = 0.70f + (spinStat / 100f) * 0.60f  // 0.70 to 1.30
    val aimLengthPx: Float get() = 900f + (aimStat / 100f) * 1600f       // 900px to 2500px

    companion object {
        val DEFAULT_CUE = CueItem(
            id = "standard_wood",
            name = "Standard Wooden",
            description = "Classic polished maple cue stick. Reliable and balanced for traditional players.",
            rarity = CueRarity.COMMON,
            powerStat = 50,
            spinStat = 50,
            aimStat = 50,
            stabilityStat = 50,
            priceCoins = 0,
            priceGems = 0,
            primaryColorHex = 0xFFE6C280,
            secondaryColorHex = 0xFF663300,
            accentColorHex = 0xFF331A00,
            tipColorHex = 0xFFFFFFFF
        )

        val CATALOG = listOf(
            DEFAULT_CUE,
            CueItem(
                id = "carbon_fiber",
                name = "Carbon Fiber",
                description = "Ultra-lightweight carbon fiber construct providing superior stability and sharp cue velocity.",
                rarity = CueRarity.RARE,
                powerStat = 75,
                spinStat = 65,
                aimStat = 70,
                stabilityStat = 80,
                priceCoins = 500,
                priceGems = 0,
                primaryColorHex = 0xFF374151,
                secondaryColorHex = 0xFF111827,
                accentColorHex = 0xFF60A5FA,
                tipColorHex = 0xFFE5E7EB
            ),
            CueItem(
                id = "crimson_dragon",
                name = "Crimson Dragon",
                description = "Forged with fiery dragon scales. Unleashes fierce spin and explosive force on every break.",
                rarity = CueRarity.EPIC,
                powerStat = 88,
                spinStat = 85,
                aimStat = 80,
                stabilityStat = 75,
                priceCoins = 1500,
                priceGems = 20,
                primaryColorHex = 0xFFDC2626,
                secondaryColorHex = 0xFF7F1D1D,
                accentColorHex = 0xFFF59E0B,
                tipColorHex = 0xFFFEE2E2
            ),
            CueItem(
                id = "cyberpunk_neon",
                name = "Cyberpunk Neon",
                description = "High-tech laser guided cue infused with holographic neon energy matrix for extreme precision.",
                rarity = CueRarity.LEGENDARY,
                powerStat = 92,
                spinStat = 90,
                aimStat = 95,
                stabilityStat = 85,
                priceCoins = 3000,
                priceGems = 50,
                primaryColorHex = 0xFF06B6D4,
                secondaryColorHex = 0xFF0891B2,
                accentColorHex = 0xFFEC4899,
                tipColorHex = 0xFFCFFAFE
            ),
            CueItem(
                id = "golden_royal",
                name = "Golden Royal",
                description = "Masterpiece crafted in 24K solid gold and diamond inlay. Unrivaled power, spin, and elegance.",
                rarity = CueRarity.MYTHIC,
                powerStat = 98,
                spinStat = 95,
                aimStat = 98,
                stabilityStat = 95,
                priceCoins = 5000,
                priceGems = 100,
                primaryColorHex = 0xFFF59E0B,
                secondaryColorHex = 0xFF78350F,
                accentColorHex = 0xFFFEF08A,
                tipColorHex = 0xFFFFFFFF
            ),
            CueItem(
                id = "phantom_stealth",
                name = "Phantom Stealth",
                description = "Sleek matte-black stealth cue designed for smooth control and pinpoint trajectory execution.",
                rarity = CueRarity.RARE,
                powerStat = 80,
                spinStat = 78,
                aimStat = 85,
                stabilityStat = 88,
                priceCoins = 1200,
                priceGems = 15,
                primaryColorHex = 0xFF1F2937,
                secondaryColorHex = 0xFF111827,
                accentColorHex = 0xFF9CA3AF,
                tipColorHex = 0xFFF3F4F6
            ),
            CueItem(
                id = "sapphire_frost",
                name = "Sapphire Frost",
                description = "Cryo-infused crystal shaft delivering icy control and maximum spin manipulation.",
                rarity = CueRarity.EPIC,
                powerStat = 86,
                spinStat = 92,
                aimStat = 88,
                stabilityStat = 80,
                priceCoins = 2200,
                priceGems = 35,
                primaryColorHex = 0xFF2563EB,
                secondaryColorHex = 0xFF1E3A8A,
                accentColorHex = 0xFF38BDF8,
                tipColorHex = 0xFFE0F2FE
            )
        )

        fun getById(id: String): CueItem {
            return CATALOG.firstOrNull { it.id == id } ?: DEFAULT_CUE
        }
    }
}
