package com.colorwithfriends.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.colorwithfriends.app.ui.theme.ColorWithFriendsTheme
import kotlin.math.max
import kotlin.math.min

private data class Region(
    val id: String,
    val label: String,
    val points: List<Offset>, // normalized 0f..1f
    val baseFill: Color
)

private val sampleRegions = listOf(
    Region(
        id = "sky",
        label = "Sky",
        points = listOf(
            Offset(0f, 0f), Offset(1f, 0f), Offset(1f, 0.55f), Offset(0f, 0.55f)
        ),
        baseFill = Color(0xffdbeafe)
    ),
    Region(
        id = "sun",
        label = "Sun",
        points = listOf(
            Offset(0.72f, 0.15f), Offset(0.82f, 0.22f), Offset(0.86f, 0.32f), Offset(0.82f, 0.42f),
            Offset(0.72f, 0.49f), Offset(0.62f, 0.42f), Offset(0.58f, 0.32f), Offset(0.62f, 0.22f)
        ),
        baseFill = Color(0xfffff4c2)
    ),
    Region(
        id = "left_peak",
        label = "Left peak",
        points = listOf(
            Offset(0.08f, 0.55f), Offset(0.35f, 0.18f), Offset(0.55f, 0.55f)
        ),
        baseFill = Color(0xffd1d5db)
    ),
    Region(
        id = "right_peak",
        label = "Right peak",
        points = listOf(
            Offset(0.35f, 0.55f), Offset(0.62f, 0.26f), Offset(0.88f, 0.55f)
        ),
        baseFill = Color(0xffcbd5e1)
    ),
    Region(
        id = "ground",
        label = "Ground",
        points = listOf(
            Offset(0f, 0.55f), Offset(1f, 0.55f), Offset(1f, 1f), Offset(0f, 1f)
        ),
        baseFill = Color(0xfff4f1de)
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ColorWithFriendsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ColoringPreviewScreen()
                }
            }
        }
    }
}

@Composable
private fun ColoringPreviewScreen() {
    var hue by remember { mutableStateOf(210f) }
    var saturation by remember { mutableStateOf(0.75f) }
    var value by remember { mutableStateOf(0.85f) }
    var regionColors by remember { mutableStateOf(sampleRegions.associate { it.id to it.baseFill }) }
    val undoStack = remember { SnapshotStateList<Map<String, Color>>() }
    var lastTouched by remember { mutableStateOf<String?>(null) }

    val currentColor = Color.hsv(hue, saturation, value)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Color With Friends",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap the canvas to fill a region with your current color. Adjust hue, saturation, and lightness to explore the full spectrum.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Coloring preview", style = MaterialTheme.typography.titleMedium)
                PaletteAndActions(
                    onReset = {
                        undoStack.clear()
                        regionColors = sampleRegions.associate { it.id to it.baseFill }
                        lastTouched = null
                    },
                    onUndo = {
                        if (undoStack.isNotEmpty()) {
                            regionColors = undoStack.removeLast()
                        }
                    },
                    canUndo = undoStack.isNotEmpty(),
                    onPaletteSelected = { selectedColor ->
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)
                        hue = hsv[0]
                        saturation = hsv[1]
                        value = hsv[2]
                    },
                    currentColor = currentColor
                )
                ColoringCanvas(
                    regions = sampleRegions,
                    regionColors = regionColors,
                    onRegionFilled = { regionId ->
                        undoStack.add(regionColors)
                        regionColors = regionColors.toMutableMap().apply { put(regionId, currentColor) }
                        lastTouched = sampleRegions.firstOrNull { it.id == regionId }?.label
                    }
                )
                CanvasLegend(regionColors)
                Divider()
                ColorControls(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onHueChange = { hue = it },
                    onSaturationChange = { saturation = it },
                    onValueChange = { value = it },
                    currentColor = currentColor
                )
                lastTouched?.let {
                    Text(
                        text = "Most recent fill: $it",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ColoringCanvas(
    regions: List<Region>,
    regionColors: Map<String, Color>,
    onRegionFilled: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(regions, regionColors) {
                        detectTapGestures { offset ->
                            val size = this.size
                            val normalized = Offset(
                                x = offset.x / max(1f, size.width),
                                y = offset.y / max(1f, size.height)
                            )
                            val hit = regions.firstOrNull { pointInPolygon(normalized, it.points) }
                            if (hit != null) {
                                onRegionFilled(hit.id)
                            }
                        }
                    }
            ) {
                drawRegions(regions = regions, regionColors = regionColors)
            }
        }
    }
}

@Composable
private fun PaletteAndActions(
    onReset: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
    onPaletteSelected: (Color) -> Unit,
    currentColor: Color
) {
    val quickSwatches = listOf(
        Color(0xffff6b6b),
        Color(0xfff7b32b),
        Color(0xff6bcf63),
        Color(0xff3b82f6),
        Color(0xff8b5cf6),
        Color(0xffffffff),
        Color(0xff111827)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onUndo,
                enabled = canUndo,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Undo")
            }
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("Reset")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Quick palette", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickSwatches.forEach { swatch ->
                    val isActive = swatch == currentColor
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f)
                            .background(swatch, RoundedCornerShape(8.dp))
                            .border(
                                width = if (isActive) 3.dp else 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .pointerInput(swatch) {
                                detectTapGestures { onPaletteSelected(swatch) }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun CanvasLegend(regionColors: Map<String, Color>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Regions", style = MaterialTheme.typography.titleSmall)
        sampleRegions.forEach { region ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .aspectRatio(1f)
                        .background(
                            color = regionColors[region.id] ?: region.baseFill,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                )
                Column {
                    Text(region.label, style = MaterialTheme.typography.bodyMedium)
                    Text(region.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ColorControls(
    hue: Float,
    saturation: Float,
    value: Float,
    onHueChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onValueChange: (Float) -> Unit,
    currentColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Color controls", style = MaterialTheme.typography.titleSmall)
        SliderWithLabel(
            label = "Hue",
            value = hue,
            valueRange = 0f..360f,
            onValueChange = onHueChange,
            valueFormatter = { "${it.toInt()}°" }
        )
        SliderWithLabel(
            label = "Saturation",
            value = saturation,
            valueRange = 0f..1f,
            onValueChange = { onSaturationChange(min(1f, max(0f, it))) },
            valueFormatter = { "${(it * 100).toInt()}%" }
        )
        SliderWithLabel(
            label = "Lightness",
            value = value,
            valueRange = 0f..1f,
            onValueChange = { onValueChange(min(1f, max(0f, it))) },
            valueFormatter = { "${(it * 100).toInt()}%" }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(currentColor, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.weight(2f)) {
                Text("Current color", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "HSV(${hue.toInt()}, ${(saturation * 100).toInt()}%, ${(value * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = valueFormatter(value), style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

private fun DrawScope.drawRegions(regions: List<Region>, regionColors: Map<String, Color>) {
    regions.forEach { region ->
        val scaledPoints = region.points.map { Offset(it.x * size.width, it.y * size.height) }
        val path = Path().apply {
            scaledPoints.firstOrNull()?.let { moveTo(it.x, it.y) }
            for (point in scaledPoints.drop(1)) {
                lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(path = path, color = regionColors[region.id] ?: region.baseFill)
        drawPath(path = path, color = MaterialTheme.colorScheme.outlineVariant, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
    }
}

private fun pointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.isEmpty()) return false
    var crossings = 0
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        val cond = ((a.y > point.y) != (b.y > point.y)) &&
            (point.x < (b.x - a.x) * (point.y - a.y) / (b.y - a.y + 1e-6f) + a.x)
        if (cond) crossings++
    }
    return crossings % 2 == 1
}

@Preview(showBackground = true)
@Composable
private fun PaletteScreenPreview() {
    ColorWithFriendsTheme {
        ColoringPreviewScreen()
    }
}
