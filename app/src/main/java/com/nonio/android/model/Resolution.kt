package com.nonio.android.model

// 480p, 720p, 1080p, 1440p, 2160p, and 4320p
enum class Resolution(
    val width: Int,
    val height: Int,
) {
    RES_480P(854, 480),
    RES_720P(1280, 720),
    RES_1080P(1920, 1080),
    RES_1440P(2560, 1440),
    RES_2160P(3840, 2160),
    RES_4320P(7680, 4320),
    ;

    companion object {
        fun getLowerResolutions(
            width: Int,
            height: Int,
        ): List<Resolution> {
            val max = maxOf(width, height)
            return entries.filter { resolution ->
                resolution.width <= max
            }
        }
    }
}

fun Resolution.getDisplayName(): String {
    if (this == Resolution.RES_2160P) {
        return "4k"
    } else if (this == Resolution.RES_4320P) {
        return "8k"
    }

    return this.name.replace("RES_", "").toLowerCase()
}
