package com.colorwithfriends.app.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Region(
    val id: String,
    val label: String,
    val points: List<Offset>, // normalized 0f..1f
    val baseFill: Color
)

data class PalettePreset(
    val name: String,
    val colors: List<Color>
)

enum class AccountTier { FREE, SUBSCRIBER }

sealed class AccessRequirement {
    data object Free : AccessRequirement()
    data object Subscription : AccessRequirement()
    data class AddOn(val price: String) : AccessRequirement()
}

data class IllustrationPack(
    val id: String,
    val title: String,
    val description: String,
    val illustrationCount: Int,
    val tags: List<String>,
    val requirement: AccessRequirement,
    val heroSwatches: List<Color>
)

val sampleRegions = listOf(
    Region(
        id = "sky",
        label = "Sky",
        points = listOf(
            Offset(0f, 0f), Offset(1f, 0f), Offset(1f, 0.55f), Offset(0f, 0.55f)
        ),
        baseFill = Color(0xffdbeafe)
    ),
    Region(
        id = "sun",
        label = "Sun",
        points = listOf(
            Offset(0.72f, 0.15f), Offset(0.82f, 0.22f), Offset(0.86f, 0.32f), Offset(0.82f, 0.42f),
            Offset(0.72f, 0.49f), Offset(0.62f, 0.42f), Offset(0.58f, 0.32f), Offset(0.62f, 0.22f)
        ),
        baseFill = Color(0xfffff4c2)
    ),
    Region(
        id = "left_peak",
        label = "Left peak",
        points = listOf(
            Offset(0.08f, 0.55f), Offset(0.35f, 0.18f), Offset(0.55f, 0.55f)
        ),
        baseFill = Color(0xffd1d5db)
    ),
    Region(
        id = "right_peak",
        label = "Right peak",
        points = listOf(
            Offset(0.35f, 0.55f), Offset(0.62f, 0.26f), Offset(0.88f, 0.55f)
        ),
        baseFill = Color(0xffcbd5e1)
    ),
    Region(
        id = "ground",
        label = "Ground",
        points = listOf(
            Offset(0f, 0.55f), Offset(1f, 0.55f), Offset(1f, 1f), Offset(0f, 1f)
        ),
        baseFill = Color(0xfff4f1de)
    )
)

val defaultQuickPalette = listOf(
    Color(0xffff6b6b),
    Color(0xfff7b32b),
    Color(0xff6bcf63),
    Color(0xff3b82f6),
    Color(0xff8b5cf6),
    Color(0xffffffff),
    Color(0xff111827)
)

val palettePresets = listOf(
    PalettePreset(
        name = "Sunset",
        colors = listOf(
            Color(0xfff72585),
            Color(0xffb5179e),
            Color(0xff7209b7),
            Color(0xff4361ee),
            Color(0xff4cc9f0)
        )
    ),
    PalettePreset(
        name = "Forest",
        colors = listOf(
            Color(0xff283618),
            Color(0xff606c38),
            Color(0xff8da674),
            Color(0xffd9d9a8),
            Color(0xffbc6c25)
        )
    ),
    PalettePreset(
        name = "Pastel",
        colors = listOf(
            Color(0xfffef6e4),
            Color(0xfff3d2c1),
            Color(0xff8bd3dd),
            Color(0xfff582ae),
            Color(0xffc4d7f2)
        )
    )
)

val samplePacks = listOf(
    IllustrationPack(
        id = "starter",
        title = "Starter scenes",
        description = "Friendly landscapes and simple characters great for onboarding and younger colorists.",
        illustrationCount = 18,
        tags = listOf("All-ages", "Beginner", "Relaxing"),
        requirement = AccessRequirement.Free,
        heroSwatches = listOf(Color(0xff6bcf63), Color(0xff3b82f6), Color(0xfff7b32b))
    ),
    IllustrationPack(
        id = "cozy_travel",
        title = "Cozy travel",
        description = "Window seats, city strolls, and quiet cafés with lots of textures to shade.",
        illustrationCount = 24,
        tags = listOf("Textured", "Intermediate", "Warm"),
        requirement = AccessRequirement.AddOn(price = "$2.99"),
        heroSwatches = listOf(Color(0xfff4a261), Color(0xff264653), Color(0xffe9c46a))
    ),
    IllustrationPack(
        id = "nature_master",
        title = "Nature master set",
        description = "Lush florals, terrariums, and wildlife scenes with lots of small regions to detail.",
        illustrationCount = 32,
        tags = listOf("Advanced", "Floral", "Detail-heavy"),
        requirement = AccessRequirement.Subscription,
        heroSwatches = listOf(Color(0xff2b8a3e), Color(0xff4cc9f0), Color(0xfff72585))
    ),
    IllustrationPack(
        id = "futurescape",
        title = "Futurescape",
        description = "Sleek sci-fi cities, neon signage, and holo-bot companions for bold palettes.",
        illustrationCount = 20,
        tags = listOf("Sci-fi", "Bold", "Neon"),
        requirement = AccessRequirement.Subscription,
        heroSwatches = listOf(Color(0xff4361ee), Color(0xff4cc9f0), Color(0xff9d4edd))
    )
)
