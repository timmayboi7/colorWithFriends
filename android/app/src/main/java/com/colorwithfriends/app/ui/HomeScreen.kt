package com.colorwithfriends.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Check
import androidx.compose.material3.icons.filled.Lock
import androidx.compose.material3.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colorwithfriends.app.model.AccessRequirement
import com.colorwithfriends.app.model.AccountTier
import com.colorwithfriends.app.model.IllustrationPack
import com.colorwithfriends.app.model.samplePacks

private enum class HomeTab(val label: String) { PREVIEW("Preview"), LIBRARY("Library") }

@Composable
fun ColorWithFriendsApp() {
    var selectedTab by remember { mutableStateOf(HomeTab.PREVIEW) }
    var accountTier by remember { mutableStateOf(AccountTier.FREE) }
    var ownedPackIds by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Color With Friends",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Android preview sandbox with coloring canvas and a mock illustration library to show access rules.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        SegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            HomeTab.values().forEach { tab ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = tab.ordinal, count = HomeTab.values().size),
                    onClick = { selectedTab = tab },
                    selected = selectedTab == tab,
                    label = { Text(tab.label) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f, fill = true)) {
            when (selectedTab) {
                HomeTab.PREVIEW -> ColoringPreviewScreen(
                    modifier = Modifier.fillMaxSize(),
                    showHeading = false
                )

                HomeTab.LIBRARY -> LibraryBrowserScreen(
                    modifier = Modifier.fillMaxSize(),
                    accountTier = accountTier,
                    ownedPackIds = ownedPackIds,
                    onAccountTierChange = { accountTier = it },
                    onToggleOwned = { packId ->
                        ownedPackIds = ownedPackIds.toMutableSet().also { set ->
                            if (set.contains(packId)) set.remove(packId) else set.add(packId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryBrowserScreen(
    modifier: Modifier = Modifier,
    accountTier: AccountTier,
    ownedPackIds: Set<String>,
    onAccountTierChange: (AccountTier) -> Unit,
    onToggleOwned: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        AccountTierSelector(accountTier = accountTier, onAccountTierChange = onAccountTierChange)
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = "Library",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Free users can color the starter pack and download exports. Subscribers unlock the full catalog and cloud saves. Add-on packs can be marked as owned to simulate purchases.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(samplePacks) { pack ->
                val accessible = when (pack.requirement) {
                    AccessRequirement.Free -> true
                    is AccessRequirement.AddOn -> ownedPackIds.contains(pack.id)
                    AccessRequirement.Subscription -> accountTier == AccountTier.SUBSCRIBER
                }
                PackCard(
                    pack = pack,
                    accessible = accessible,
                    isOwned = ownedPackIds.contains(pack.id),
                    onToggleOwned = onToggleOwned
                )
            }
        }
    }
}

@Composable
private fun AccountTierSelector(accountTier: AccountTier, onAccountTierChange: (AccountTier) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Account access", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Toggle between Free and Subscriber to preview entitlements, social perks, and cloud sync availability.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAccountTierChange(AccountTier.FREE) },
                    enabled = accountTier != AccountTier.FREE
                ) { Text("Free") }
                Button(
                    onClick = { onAccountTierChange(AccountTier.SUBSCRIBER) },
                    enabled = accountTier != AccountTier.SUBSCRIBER
                ) { Text("Subscriber") }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Subscriber perks: full catalog, cloud saves, social feed, and version history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PackCard(
    pack: IllustrationPack,
    accessible: Boolean,
    isOwned: Boolean,
    onToggleOwned: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (accessible) Icons.Filled.Check else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (accessible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(pack.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${pack.illustrationCount} vector illustrations",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(pack.description, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pack.tags.forEach { tag ->
                    Card(
                        shape = RoundedCornerShape(50),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pack.heroSwatches.forEach { swatch ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(swatch, shape = CircleShape)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ctaLabel = when (pack.requirement) {
                    AccessRequirement.Free -> "Color now"
                    is AccessRequirement.AddOn -> if (isOwned) "Color now" else "Purchase ${pack.requirement.price}"
                    AccessRequirement.Subscription -> if (accessible) "Color now" else "Unlock with subscription"
                }
                Button(onClick = { /* placeholder hook for navigation to detail */ }, enabled = accessible) {
                    Text(ctaLabel)
                }
                if (pack.requirement is AccessRequirement.AddOn) {
                    OutlinedButton(onClick = { onToggleOwned(pack.id) }) {
                        Text(if (isOwned) "Mark unowned" else "Mark owned")
                    }
                }
            }
            val subtitle = when (pack.requirement) {
                AccessRequirement.Free -> "Included for everyone"
                is AccessRequirement.AddOn -> if (isOwned) "Owned add-on" else "Requires one-time purchase"
                AccessRequirement.Subscription -> "Requires active subscription"
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

