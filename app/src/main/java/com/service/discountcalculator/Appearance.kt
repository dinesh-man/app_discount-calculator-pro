package com.service.discountcalculator

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class Appearance (
    val topAppBarContainerColor: Color,
    val inputFieldsLabelColor : Color,
    val inputFieldsFontColor : Color,
    val resultBarsContainerColor: List<Color>,
    val backgroundColor: Color,

    val inputFieldLabelsFontSize : TextUnit = 15.sp,
    val inputFieldsFontSize : TextUnit = 17.sp
)

val RedColorScheme = Appearance (
    topAppBarContainerColor = Color(0xFFB40202),
    inputFieldsLabelColor = Color(0xFF991E1E),
    inputFieldsFontColor = Color.Red,
    resultBarsContainerColor = listOf(
        Color(0xFFD83333),
        Color(0xFFCF1E1E),
        Color(0xFFA80000),
        ),
    backgroundColor = Color.Transparent
)

val BlueColorScheme = Appearance (
    topAppBarContainerColor = Color(0xFF1E90FF),
    inputFieldsLabelColor = Color(0xFF386BD1),
    inputFieldsFontColor = Color(0xFF2485D3),
    resultBarsContainerColor = listOf(
        Color(0xFF3CA1D8),
        Color(0xFF3E7FF3),
        Color(0xFF4961E7)),
    backgroundColor = Color.Transparent
)

val GreenColorScheme = Appearance (
    topAppBarContainerColor = Color(0xFF13A519),
    inputFieldsLabelColor = Color(0xFF028808),
    inputFieldsFontColor = Color(0xFF017066),
    resultBarsContainerColor = listOf(
        Color(0xFF459C49),
        Color(0xFF21831B),
        Color(0xFF0D7401),
    ),
    backgroundColor = Color.Transparent
)

val PinkColorScheme = Appearance (
    topAppBarContainerColor = Color(0xFFD5439A),
    inputFieldsLabelColor = Color(0xFFDB4175),
    inputFieldsFontColor = Color(0xFFE91E63),
    resultBarsContainerColor = listOf(
        Color(0xFFBD4B71),
        Color(0xFFC51A54),
        Color(0xFFAF2B57)
    ),
    backgroundColor = Color.Transparent
)

val DarkColorScheme = Appearance (
    topAppBarContainerColor = Color(0xFF212222),
    inputFieldsLabelColor = Color(0xFFD1D889),
    inputFieldsFontColor = Color(0xFFCC9903),
    resultBarsContainerColor = listOf(
        Color(0xFF3D302F),
        Color(0xFF2B1413),
        Color(0xFF221717)
    ),
    backgroundColor = Color(0xFF1B1918)
)

fun getColorScheme(selectedColorScheme : String): Appearance {
    return when (selectedColorScheme) {
        "Red" -> RedColorScheme
        "Green" -> GreenColorScheme
        "Dark" -> DarkColorScheme
        "Pink" -> PinkColorScheme
        else -> BlueColorScheme
    }
}
