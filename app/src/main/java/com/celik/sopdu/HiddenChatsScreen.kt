package com.celik.sopdu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun HiddenChatsScreen(
    rejectedPeers: Map<String, String>,
    blockedPeers: Map<String, String>,
    onBack: () -> Unit,
    onAcceptRejected: (String, String) -> Unit,
    onRemoveRejected: (String) -> Unit,
    onUnblockPeer: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        item {
            BackTextButton(onClick = onBack)
            Spacer(Modifier.height(8.dp))
            ScreenTitle("Hidden chats", "Blocked users and rejected chat requests")
        }
        item {
            FieldPanel(Modifier.fillMaxWidth()) {
                Text(
                    "You can review chats hidden from the main list here. Accepting a rejected request moves it into Chats. Unblocking a user allows that chat to appear again.",
                    color = MUTED,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item { SectionHeader("Rejected requests") }
        if (rejectedPeers.isEmpty()) {
            item { EmptyHiddenState("No rejected requests") }
        } else {
            items(rejectedPeers.entries.toList()) { entry ->
                HiddenPeerRow(
                    name = entry.value,
                    peerId = entry.key,
                    primaryText = "Accept",
                    secondaryText = "Remove",
                    onPrimary = { onAcceptRejected(entry.key, entry.value) },
                    onSecondary = { onRemoveRejected(entry.key) }
                )
            }
        }
        item { SectionHeader("Blocked users") }
        if (blockedPeers.isEmpty()) {
            item { EmptyHiddenState("No blocked users") }
        } else {
            items(blockedPeers.entries.toList()) { entry ->
                HiddenPeerRow(
                    name = entry.value,
                    peerId = entry.key,
                    primaryText = "Unblock",
                    secondaryText = null,
                    onPrimary = { onUnblockPeer(entry.key) },
                    onSecondary = {}
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    SectionLabel(text)
}

@Composable
private fun EmptyHiddenState(text: String) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Text(text, color = MUTED, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HiddenPeerRow(
    name: String,
    peerId: String,
    primaryText: String,
    secondaryText: String?,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    val badge = remember(name, peerId) {
        name.trim().ifBlank { peerId }.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    FieldPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(PANEL_RAISED, CircleShape), contentAlignment = Alignment.Center) {
                Text(badge, color = CYAN, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, color = TEXT, fontWeight = FontWeight.Black, maxLines = 1)
                Text(peerId, color = MUTED, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onPrimary,
                    colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = CYAN),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(primaryText)
                }
                if (secondaryText != null) {
                    OutlinedButton(
                        onClick = onSecondary,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(secondaryText, color = MUTED)
                    }
                }
            }
        }
    }
}
