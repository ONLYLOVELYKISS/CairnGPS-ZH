package app.matthieu.cairngps.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.matthieu.cairngps.ui.theme.DarkBackground
import app.matthieu.cairngps.ui.theme.DarkGlassBorder
import app.matthieu.cairngps.ui.theme.DarkGlassHighlight
import app.matthieu.cairngps.ui.theme.DarkGlassSurface
import app.matthieu.cairngps.ui.theme.LightBackground
import app.matthieu.cairngps.ui.theme.LightGlassBorder
import app.matthieu.cairngps.ui.theme.LightGlassHighlight
import app.matthieu.cairngps.ui.theme.LightGlassSurface
import app.matthieu.cairngps.ui.theme.LocalIsLightTheme

/**
 * The app-wide canvas for the Liquid Glass visual system.
 *
 * It deliberately uses only Compose primitives: a soft gradient, two ambient light fields and
 * translucent surfaces. This keeps the effect stable on older devices where real-time backdrop
 * blur is not available, while preserving the depth and contrast of a glass UI.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val light = LocalIsLightTheme.current
    val background = if (light) LightBackground else DarkBackground
    val orbOne = if (light) Color(0x405E9B68) else Color(0x306FBE83)
    val orbTwo = if (light) Color(0x304D8DB0) else Color(0x243F7FA0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = if (light) {
                        listOf(background, Color(0xFFEAF3EB), Color(0xFFF7F9F5))
                    } else {
                        listOf(background, Color(0xFF111D18), Color(0xFF0B1110))
                    },
                ),
            )
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(orbOne, Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.12f),
                        radius = size.minDimension * 0.72f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(orbTwo, Color.Transparent),
                        center = Offset(size.width * 0.98f, size.height * 0.78f),
                        radius = size.minDimension * 0.62f,
                    ),
                )
            },
    ) {
        content()
    }
}

/** Applies the shared translucent surface, border and top-edge highlight to any container. */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color? = null,
): Modifier {
    val light = LocalIsLightTheme.current
    val surface = tint ?: if (light) LightGlassSurface else DarkGlassSurface
    val highlight = if (light) LightGlassHighlight else DarkGlassHighlight
    return this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(highlight, surface),
            ),
            shape,
        )
        .border(1.dp, if (light) LightGlassBorder else DarkGlassBorder, shape)
}
