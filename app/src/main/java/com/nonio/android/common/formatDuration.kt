package com.nonio.android.common

fun formatDuration(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val elapsedTime = currentTime - timestamp

    val seconds = elapsedTime / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> "${years}yr"
        months > 0 -> "${months}mo"
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}hr"
        minutes > 0 -> "${minutes}min"
        else -> "${seconds}sec"
    }
}

fun main() {
    val timestamp = 1686600852000
    println("It has been ${formatDuration(timestamp)} since posting.")
}
