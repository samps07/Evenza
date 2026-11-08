package com.example.ai

enum class OcrMode(
    val title: String,
    val subtitle: String,
    val estimatedTime: String
) {
    FAST(
        title = "Fast Standard",
        subtitle = "Instant single-pass scan. Best for high-contrast crisp labels.",
        estimatedTime = "~0.5s"
    ),
    ENHANCED(
        title = "Clarity Boost",
        subtitle = "Sharpened contrast filter for faint text and glossy labels.",
        estimatedTime = "~1.5s"
    ),
    DEEP_SCAN(
        title = "Deep Multi-Pass",
        subtitle = "Dual-pass scan merging original & high-contrast. Excels at dot-matrix stamps.",
        estimatedTime = "~2-3s"
    ),
    MAX_PRECISION(
        title = "Max Multi-Angle",
        subtitle = "Multi-pass with 90°/270° rotation search for curved bottles & vertical stamps.",
        estimatedTime = "~3-5s"
    )
}

data class OcrConfig(
    val mode: OcrMode = OcrMode.DEEP_SCAN,
    val maxResolution: Int = 1800,
    val contrastBoost: Boolean = true,
    val sharpening: Boolean = true,
    val checkRotations: Boolean = false
)
