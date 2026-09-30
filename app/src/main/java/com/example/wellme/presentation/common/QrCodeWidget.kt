package com.example.wellme.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun QrCodeWidget(seed: Int = 42, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val size = size.width
        val moduleCount = 21
        val moduleSize = size / moduleCount

        // 1. Finder pattern top-left
        drawFinderPattern(0f, 0f, moduleSize)

        // 2. Finder pattern top-right
        drawFinderPattern((moduleCount - 7) * moduleSize, 0f, moduleSize)

        // 3. Finder pattern bottom-left
        drawFinderPattern(0f, (moduleCount - 7) * moduleSize, moduleSize)

        // 4. Fill in random mock modules
        val random = Random(seed.toLong())
        for (row in 0 until moduleCount) {
            for (col in 0 until moduleCount) {
                // Skip finder patterns
                if (row < 8 && col < 8) continue
                if (row < 8 && col >= moduleCount - 8) continue
                if (row >= moduleCount - 8 && col < 8) continue

                // Draw random black modules (50% density)
                if (random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * moduleSize, row * moduleSize),
                        size = Size(moduleSize, moduleSize)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    moduleSize: Float
) {
    drawRect(
        color = Color.Black,
        topLeft = Offset(x, y),
        size = Size(7 * moduleSize, 7 * moduleSize)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(x + moduleSize, y + moduleSize),
        size = Size(5 * moduleSize, 5 * moduleSize)
    )
    drawRect(
        color = Color.Black,
        topLeft = Offset(x + 2 * moduleSize, y + 2 * moduleSize),
        size = Size(3 * moduleSize, 3 * moduleSize)
    )
}
