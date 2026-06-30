package com.justwen.androidnga.ui.compose.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

val PrimaryGreen = Color(0XFF128F80)
val PrimaryBlack = Color(0XFF212121)
val PrimaryBrown = Color(0XFF591804)
val PrimaryNight = PrimaryBlack

fun Color.isLight(): Boolean = luminance() > 0.5f

fun Color.luminance(): Float {
    val r = linearize(red)
    val g = linearize(green)
    val b = linearize(blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private fun linearize(c: Float): Float {
    return if (c <= 0.04045f) c / 12.92f
    else ((c + 0.055f) / 1.055f).pow(2.4f)
}

private fun Float.pow(exp: Float): Float {
    return this.toDouble().pow(exp.toDouble()).toFloat()
}

fun Color.contrastRatio(other: Color): Float {
    val l1 = luminance() + 0.05f
    val l2 = other.luminance() + 0.05f
    return if (l1 > l2) l1 / l2 else l2 / l1
}

fun Color.readableOn(bg: Color): Color {
    val white = Color.White
    val black = Color.Black
    return if (white.contrastRatio(bg) > black.contrastRatio(bg)) white else black
}
