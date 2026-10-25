package com.xandron.novapool.model

import androidx.compose.ui.graphics.Color

enum class FeltColorOption(
    val id: String,
    val displayName: String,
    val centerColorHex: Long,
    val edgeColorHex: Long,
    val priceCoins: Int,
    val priceGems: Int
) {
    EMERALD_GREEN(
        id = "felt_emerald",
        displayName = "Emerald Green",
        centerColorHex = 0xFF0F8A4B,
        edgeColorHex = 0xFF084D29,
        priceCoins = 0,
        priceGems = 0
    ),
    TOURNAMENT_BLUE(
        id = "felt_blue",
        displayName = "Tournament Blue",
        centerColorHex = 0xFF1D4ED8,
        edgeColorHex = 0xFF0F172A,
        priceCoins = 300,
        priceGems = 0
    ),
    DEEP_CRIMSON(
        id = "felt_crimson",
        displayName = "Deep Crimson",
        centerColorHex = 0xFFB91C1C,
        edgeColorHex = 0xFF450A0A,
        priceCoins = 600,
        priceGems = 5
    ),
    MIDNIGHT_BLACK(
        id = "felt_black",
        displayName = "Midnight Black",
        centerColorHex = 0xFF27272A,
        edgeColorHex = 0xFF09090B,
        priceCoins = 1000,
        priceGems = 15
    );

    val centerColor: Color get() = Color(centerColorHex)
    val edgeColor: Color get() = Color(edgeColorHex)

    companion object {
        fun fromId(id: String): FeltColorOption {
            return entries.firstOrNull { it.id == id } ?: EMERALD_GREEN
        }
    }
}

enum class WoodFinishOption(
    val id: String,
    val displayName: String,
    val outerColorHex: Long,
    val innerColorHex: Long,
    val priceCoins: Int,
    val priceGems: Int
) {
    WALNUT(
        id = "wood_walnut",
        displayName = "Classic Walnut",
        outerColorHex = 0xFF3E1F0F,
        innerColorHex = 0xFF261208,
        priceCoins = 0,
        priceGems = 0
    ),
    OAK(
        id = "wood_oak",
        displayName = "Golden Oak",
        outerColorHex = 0xFF854D0E,
        innerColorHex = 0xFF543207,
        priceCoins = 250,
        priceGems = 0
    ),
    MAHOGANY(
        id = "wood_mahogany",
        displayName = "Rich Mahogany",
        outerColorHex = 0xFF681A1A,
        innerColorHex = 0xFF3B0E0E,
        priceCoins = 500,
        priceGems = 5
    ),
    EBONY_CARBON(
        id = "wood_ebony",
        displayName = "Ebony Carbon",
        outerColorHex = 0xFF18181B,
        innerColorHex = 0xFF09090B,
        priceCoins = 900,
        priceGems = 12
    );

    val outerColor: Color get() = Color(outerColorHex)
    val innerColor: Color get() = Color(innerColorHex)

    companion object {
        fun fromId(id: String): WoodFinishOption {
            return entries.firstOrNull { it.id == id } ?: WALNUT
        }
    }
}

data class TableCustomizationState(
    val equippedFelt: FeltColorOption = FeltColorOption.EMERALD_GREEN,
    val equippedFrame: WoodFinishOption = WoodFinishOption.WALNUT,
    val unlockedFelts: Set<String> = setOf(FeltColorOption.EMERALD_GREEN.id),
    val unlockedFrames: Set<String> = setOf(WoodFinishOption.WALNUT.id)
)
