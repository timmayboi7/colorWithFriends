package com.colorwithfriends.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.colorwithfriends.app.ui.theme.ColorWithFriendsTheme

private data class PaletteColor(val name: String, val color: Color, val hex: String)

private val palette = listOf(
    PaletteColor("Sunset", Color(0xffff6b6b), "#FF6B6B"),
    PaletteColor("Coral", Color(0xffff9f7a), "#FF9F7A"),
    PaletteColor("Gold", Color(0xfffbbf24), "#FBBF24"),
    PaletteColor("Mint", Color(0xff34d399), "#34D399"),
    PaletteColor("Sea", Color(0xff22d3ee), "#22D3EE"),
    PaletteColor("Sky", Color(0xff60a5fa), "#60A5FA"),
    PaletteColor("Indigo", Color(0xff4f46e5), "#4F46E5"),
    PaletteColor("Lilac", Color(0xffa78bfa), "#A78BFA"),
    PaletteColor("Pink", Color(0xffec4899), "#EC4899"),
    PaletteColor("Sand", Color(0xfff5e0b7), "#F5E0B7"),
    PaletteColor("Olive", Color(0xff84cc16), "#84CC16"),
    PaletteColor("Charcoal", Color(0xff1f2937), "#1F2937"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ColorWithFriendsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PaletteScreen()
                }
            }
        }
    }
}

@Composable
private fun PaletteScreen() {
    var selected by remember { mutableStateOf(palette.first()) }
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Color With Friends (Android Preview)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap a swatch to preview a color. Share a link with the current selection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Selected", style = MaterialTheme.typography.titleMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        color = selected.color,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {}
                    Text(text = "${selected.name} ${selected.hex}", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = {
                        val uri = Uri.parse("https://example.com/color?hex=${selected.hex.removePrefix("#")}")
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, uri.toString())
                        }
                        shareLauncher.launch(Intent.createChooser(intent, "Share color"))
                    }) {
                        Text("Share selection")
                    }
                }
            }

            Divider()
            Text(text = "Palette", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(palette) { swatch ->
                    SwatchCard(
                        paletteColor = swatch,
                        isSelected = swatch == selected,
                        onSelect = { selected = swatch }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwatchCard(
    paletteColor: PaletteColor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(color = paletteColor.color, shape = RoundedCornerShape(8.dp))
            )
            Text(text = paletteColor.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = paletteColor.hex, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaletteScreenPreview() {
    ColorWithFriendsTheme {
        PaletteScreen()
    }
}
