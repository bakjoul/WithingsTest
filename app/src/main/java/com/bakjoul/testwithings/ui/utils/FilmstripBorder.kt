package com.bakjoul.testwithings.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.filmstripBorder(
    bandHeight: Dp = 24.dp,
    holeSize: Dp = 12.dp,
    holeSpacing: Dp = 8.dp,
    sideBarWidth: Dp = 8.dp,
    color: Color = Color.Black,
    alpha: Float = 0.9f
): Modifier = this.drawWithCache {
    // Everything below only depends on `size` (not on the drawn content),
    // so it's computed once per size change instead of every frame,
    // unlike drawWithContent.
    val bandPx = bandHeight.toPx()
    val holePx = holeSize.toPx()
    val spacingPx = holeSpacing.toPx()
    val sideBarPx = sideBarWidth.toPx()
    val tintedColor = color.copy(alpha = alpha)
    val bounds = Rect(0f, 0f, size.width, size.height)
    val layerPaint = Paint() // reused for saveLayer, not recreated every frame

    // Holes distribution without truncated holes at the ends
    val usableWidth = size.width - 2 * sideBarPx
    if (usableWidth <= 0f) {
        // No space to draw holes, just draw the full frame
        val fullPath = Path().apply { addRect(bounds) }
        return@drawWithCache onDrawWithContent {
            drawContent()
            drawPath(fullPath, color = tintedColor)
        }
    }

    val step = holePx + spacingPx
    val holeCount = ((usableWidth + spacingPx) / step).toInt().coerceAtLeast(1)
    val realStep = usableWidth / holeCount
    val realHoleSize = realStep - spacingPx

    if (realHoleSize <= 0f) {
        // No space for a proper hole, just draw the full frame
        val fullPath = Path().apply { addRect(bounds) }
        return@drawWithCache onDrawWithContent {
            drawContent()
            drawPath(fullPath, color = tintedColor)
        }
    }

    // Hole positions precomputed once
    val cornerRadius = CornerRadius(2f, 2f)
    val topHoleY = (bandPx - realHoleSize) / 2f
    val bottomHoleY = size.height - bandPx + (bandPx - realHoleSize) / 2f
    val holeXs = FloatArray(holeCount) { i -> sideBarPx + spacingPx / 2f + i * realStep }

    // A single path for the entire frame, including top/bottom bands and sidebars
    val framePath = Path().apply {
        addRect(Rect(0f, 0f, size.width, bandPx)) // top band
        addRect(Rect(0f, size.height - bandPx, size.width, size.height)) // bottom band
        addRect(Rect(0f, bandPx, sideBarPx, size.height - bandPx)) // left bar
        addRect(Rect(size.width - sideBarPx, bandPx, size.width, size.height - bandPx)) // right bar
    }

    onDrawWithContent {
        // Clip content to the central area to prevent any bleed outside the borders
        clipRect(
            left = sideBarPx, top = bandPx,
            right = size.width - sideBarPx, bottom = size.height - bandPx
        ) {
            this@onDrawWithContent.drawContent()
        }

        // Single layer: paint the frame with alpha, then cut out the holes (transparent)
        drawIntoCanvas { canvas ->
            canvas.saveLayer(bounds, layerPaint)

            drawPath(framePath, color = tintedColor)

            for (x in holeXs) {
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(x, topHoleY),
                    size = Size(realHoleSize, realHoleSize),
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(x, bottomHoleY),
                    size = Size(realHoleSize, realHoleSize),
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )
            }

            canvas.restore()
        }
    }
}

@Preview
@Composable
fun FilmstripBorderPreview() {
    Box(
        modifier = Modifier
            .size(320.dp, 300.dp)
            .filmstripBorder()
    )
}
