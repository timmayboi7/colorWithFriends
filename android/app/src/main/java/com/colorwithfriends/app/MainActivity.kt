package com.colorwithfriends.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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

private data class PalettePreset(
    val name: String,
    val colors: List<Color>
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

private val defaultQuickPalette = listOf(
    Color(0xffff6b6b),
    Color(0xfff7b32b),
    Color(0xff6bcf63),
    Color(0xff3b82f6),
    Color(0xff8b5cf6),
    Color(0xffffffff),
    Color(0xff111827)
)

private val palettePresets = listOf(
    PalettePreset(
        name = "Sunset",
        colors = listOf(
            Color(0xfff72585),
            Color(0xffb5179e),
            Color(0xff7209b7),
            Color(0xff4361ee),
            Color(0xff4cc9f0)
        )
    ),
    PalettePreset(
        name = "Forest",
        colors = listOf(
            Color(0xff283618),
            Color(0xff606c38),
            Color(0xff8da674),
            Color(0xffd9d9a8),
            Color(0xffbc6c25)
        )
    ),
    PalettePreset(
        name = "Pastel",
        colors = listOf(
            Color(0xfffef6e4),
            Color(0xfff3d2c1),
            Color(0xff8bd3dd),
            Color(0xfff582ae),
            Color(0xffc4d7f2)
        )
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
    val defaultHsv = remember { defaultQuickPalette.first().toHsvComponents() }
    var hue by remember { mutableStateOf(defaultHsv[0]) }
    var saturation by remember { mutableStateOf(defaultHsv[1]) }
    var value by remember { mutableStateOf(defaultHsv[2]) }
    var regionColors by remember { mutableStateOf(sampleRegions.associate { it.id to it.baseFill }) }
    val undoStack = remember { SnapshotStateList<Map<String, Color>>() }
    val colorHistory = remember { SnapshotStateList<Color>() }
    var lastTouched by remember { mutableStateOf<String?>(null) }
    var targetRegionId by remember { mutableStateOf<String?>(null) }
    var quickPalette by remember { mutableStateOf(defaultQuickPalette) }

    val currentColor = Color.hsv(hue, saturation, value)

    fun setCurrentColor(color: Color) {
        val hsv = color.toHsvComponents()
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        rememberColor(colorHistory, color)
    }

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
                        colorHistory.clear()
                        lastTouched = null
                        targetRegionId = null
                        quickPalette = defaultQuickPalette
                        setCurrentColor(defaultQuickPalette.first())
                    },
                    onUndo = {
                        if (undoStack.isNotEmpty()) {
                            regionColors = undoStack.removeLast()
                            targetRegionId = computeNextUnfilled(sampleRegions, regionColors, targetRegionId)
                        }
                    },
                    canUndo = undoStack.isNotEmpty(),
                    onPaletteSelected = { selectedColor ->
                        setCurrentColor(selectedColor)
                    },
                    currentColor = currentColor,
                    quickSwatches = quickPalette,
                    presets = palettePresets,
                    onPresetSelected = { preset ->
                        if (preset.colors.isNotEmpty()) {
                            quickPalette = preset.colors
                            setCurrentColor(preset.colors.first())
                        }
                    }
                )
                ColoringCanvas(
                    regions = sampleRegions,
                    regionColors = regionColors,
                    targetRegionId = targetRegionId,
                    onRegionFilled = { regionId ->
                        undoStack.add(regionColors)
                        regionColors = regionColors.toMutableMap().apply { put(regionId, currentColor) }
                        lastTouched = sampleRegions.firstOrNull { it.id == regionId }?.label
                        targetRegionId = computeNextUnfilled(sampleRegions, regionColors, targetRegionId)
                        rememberColor(colorHistory, currentColor)
                    },
                    onRegionColorSampled = { sampledColor ->
                        setCurrentColor(sampledColor)
                    }
                )
                CanvasLegend(regionColors)
                ProgressSummary(
                    regions = sampleRegions,
                    regionColors = regionColors,
                    targetRegionId = targetRegionId,
                    onNextUnfilled = {
                        targetRegionId = computeNextUnfilled(sampleRegions, regionColors, targetRegionId)
                    }
                )
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
                ColorHistoryTray(history = colorHistory, onSwatchSelected = { swatch ->
                    setCurrentColor(swatch)
                })
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
    targetRegionId: String?,
    onRegionFilled: (String) -> Unit,
    onRegionColorSampled: (Color) -> Unit
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
                        detectTapGestures(
                            onTap = { offset ->
                                val size = this.size
                                val normalized = Offset(
                                    x = offset.x / max(1f, size.width),
                                    y = offset.y / max(1f, size.height)
                                )
                                val hit = regions.firstOrNull { pointInPolygon(normalized, it.points) }
                                if (hit != null) {
                                    onRegionFilled(hit.id)
                                }
                            },
                            onLongPress = { offset ->
                                val size = this.size
                                val normalized = Offset(
                                    x = offset.x / max(1f, size.width),
                                    y = offset.y / max(1f, size.height)
                                )
                                val hit = regions.firstOrNull { pointInPolygon(normalized, it.points) }
                                if (hit != null) {
                                    val sampled = regionColors[hit.id] ?: hit.baseFill
                                    onRegionColorSampled(sampled)
                                }
                            }
                        )
                    }
            ) {
                drawRegions(
                    regions = regions,
                    regionColors = regionColors,
                    targetRegionId = targetRegionId
                )
            }
        }
    }
}

@Composable
private fun ColorHistoryTray(history: List<Color>, onSwatchSelected: (Color) -> Unit) {
    if (history.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Recent colors", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            history.takeLast(10).asReversed().forEach { swatch ->
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .weight(1f)
                        .background(swatch, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .pointerInput(swatch) { detectTapGestures { onSwatchSelected(swatch) } }
                )
            }
        }
        Text(
            text = "Tip: long-press the canvas to eyedropper a region and reuse that color.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun rememberColor(history: SnapshotStateList<Color>, color: Color, limit: Int = 10) {
    history.remove(color)
    history.add(color)
    while (history.size > limit) {
        history.removeFirst()
    }
}

private fun Color.toHsvComponents(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

@Composable
private fun PaletteAndActions(
    onReset: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
    onPaletteSelected: (Color) -> Unit,
    currentColor: Color,
    quickSwatches: List<Color>,
    presets: List<PalettePreset>,
    onPresetSelected: (PalettePreset) -> Unit
) {
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Preset palettes", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .pointerInput(preset) {
                                detectTapGestures { onPresetSelected(preset) }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(preset.name, style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                preset.colors.take(5).forEach { swatch ->
                                    Box(
                                        modifier = Modifier
                                            .height(18.dp)
                                            .weight(1f)
                                            .background(swatch, RoundedCornerShape(6.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    )
                                }
                            }
                            Text(
                                text = "Tap to load ${preset.colors.size} colors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

@Composable
private fun ProgressSummary(
    regions: List<Region>,
    regionColors: Map<String, Color>,
    targetRegionId: String?,
    onNextUnfilled: () -> Unit
) {
    val unfilled = remember(regions, regionColors) {
        regions.filter { region ->
            (regionColors[region.id] ?: region.baseFill) == region.baseFill
        }
    }
    val completion = ((regions.size - unfilled.size).toFloat() / max(1, regions.size)) * 100
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Progress", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${regions.size - unfilled.size} of ${regions.size} regions colored (${completion.toInt()}%)",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNextUnfilled, enabled = unfilled.isNotEmpty()) {
                Text(if (targetRegionId == null) "Next unfilled" else "Jump to unfilled")
            }
            if (targetRegionId != null) {
                val label = regions.firstOrNull { it.id == targetRegionId }?.label ?: targetRegionId
                Text(
                    text = "Focus: $label",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        if (unfilled.isNotEmpty()) {
            Text(
                text = "Unfilled: ${unfilled.joinToString { it.label }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun DrawScope.drawRegions(
    regions: List<Region>,
    regionColors: Map<String, Color>,
    targetRegionId: String?
) {
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
        val strokeColor = if (region.id == targetRegionId) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
        val strokeWidth = if (region.id == targetRegionId) 6f else 2f
        drawPath(
            path = path,
            color = strokeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
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

private fun computeNextUnfilled(
    regions: List<Region>,
    regionColors: Map<String, Color>,
    currentTarget: String?
): String? {
    val unfilled = regions.filter { region ->
        (regionColors[region.id] ?: region.baseFill) == region.baseFill
    }
    if (unfilled.isEmpty()) return null
    val currentIndex = unfilled.indexOfFirst { it.id == currentTarget }
    val nextIndex = if (currentIndex == -1 || currentIndex == unfilled.lastIndex) 0 else currentIndex + 1
    return unfilled[nextIndex].id
}

@Preview(showBackground = true)
@Composable
private fun PaletteScreenPreview() {
    ColorWithFriendsTheme {
        ColoringPreviewScreen()
    }
}
