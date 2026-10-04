package com.example.wellme.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Neumorphic Theme Colors
val NeumorphicLightBg = Color(0xFFE0E5EC)
val NeumorphicLightTopShadow = Color(0xFFFFFFFF)
val NeumorphicLightBottomShadow = Color(0xFFA3B1C6)

val NeumorphicDarkBg = Color(0xFF1E222A)
val NeumorphicDarkTopShadow = Color(0xFF282D3A)
val NeumorphicDarkBottomShadow = Color(0xFF14171D)

fun Modifier.neumorphicShadow(
    offsetDp: Dp = 6.dp,
    blurDp: Dp = 10.dp,
    topShadowColor: Color = NeumorphicLightTopShadow,
    bottomShadowColor: Color = NeumorphicLightBottomShadow,
    shapeDp: Dp = 20.dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val offsetPx = offsetDp.toPx()
        val blurPx = blurDp.toPx()
        val shapePx = shapeDp.toPx()

        // Bottom-Right Dark Shadow
        val darkPaint = Paint().apply {
            color = bottomShadowColor
            style = PaintingStyle.Fill
            asFrameworkPaint().run {
                isAntiAlias = true
                if (blurPx > 0) maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        }
        canvas.drawRoundRect(
            left = offsetPx,
            top = offsetPx,
            right = size.width + offsetPx,
            bottom = size.height + offsetPx,
            radiusX = shapePx,
            radiusY = shapePx,
            paint = darkPaint
        )

        // Top-Left Light Shadow
        val lightPaint = Paint().apply {
            color = topShadowColor
            style = PaintingStyle.Fill
            asFrameworkPaint().run {
                isAntiAlias = true
                if (blurPx > 0) maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        }
        canvas.drawRoundRect(
            left = -offsetPx,
            top = -offsetPx,
            right = size.width - offsetPx,
            bottom = size.height - offsetPx,
            radiusX = shapePx,
            radiusY = shapePx,
            paint = lightPaint
        )
    }
}

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeumorphicLightBg,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .neumorphicShadow(
                offsetDp = elevation,
                blurDp = elevation * 1.5f,
                shapeDp = cornerRadius
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = EmeraldGreen,
    cornerRadius: Dp = 20.dp,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .neumorphicShadow(
                offsetDp = 4.dp,
                blurDp = 8.dp,
                shapeDp = cornerRadius
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
