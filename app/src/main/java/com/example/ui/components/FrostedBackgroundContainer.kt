package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FrostedBackgroundContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9FF))
    ) {
        // Top-Left Soft Blue Ambient Glow Orbs
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-60).dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x6093C5FD), // Light blue opacity 0.35
                            Color(0x10BFDBFE),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Middle-Right Soft Pink Ambient Glow Orbs
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = 40.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x50FBCFE8), // Light pink opacity 0.3
                            Color(0x10FCE7F3),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
        )

        // Main Content Layer
        content()
    }
}
