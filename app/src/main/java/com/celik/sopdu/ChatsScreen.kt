package com.celik.sopdu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.celik.sopdu.data.SopduDao

@Composable
internal fun ChatsScreen(chats: List<Peer>, pendingPeers: Map<String, String>, hiddenCount: Int, dao: SopduDao, batteryForPeer: (String) -> Int?, additionalNameForPeer: (String) -> String, bluetoothOn: Boolean, onOpenBluetoothSettings: () -> Unit, onLogoClick: () -> Unit, onAcceptPending: (String, String) -> Unit, onRejectPending: (String) -> Unit, onOpenHiddenChats: () -> Unit, onOpenChat: (Peer) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp)) {
        BrandedScreenTitle("Sopdu", "offline communication utility", onLogoClick)
        Spacer(Modifier.height(14.dp))
        if (!bluetoothOn) {
            BluetoothOffBanner(onOpenBluetoothSettings)
            Spacer(Modifier.height(12.dp))
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(pendingPeers.entries.toList()) { entry -> PendingChatRow(entry.key, entry.value, onAccept = { onAcceptPending(entry.key, entry.value) }, onReject = { onRejectPending(entry.key) }) }
            items(chats) { p -> ChatRowTile(p, dao, batteryForPeer(p.id), additionalNameForPeer(p.id), onClick = { onOpenChat(p) }) }
            item { HiddenChatsRow(hiddenCount, onOpenHiddenChats) }
        }
    }
}

@Composable
internal fun BluetoothOffBanner(onOpenBluetoothSettings: () -> Unit) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(CYAN.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.BluetoothDisabled, contentDescription = null, tint = CYAN, modifier = Modifier.size(21.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Bluetooth is off", color = TEXT, fontWeight = FontWeight.Black)
                Text("Turn it on before radar scans or nearby chats.", color = MUTED, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onOpenBluetoothSettings, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                Text("Settings", color = CYAN)
            }
        }
    }
}

@Composable
internal fun HiddenChatsRow(hiddenCount: Int, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.fillMaxWidth().padding(top = 15.dp, end = 2.dp, bottom = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(PANEL_RAISED, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.VisibilityOff, contentDescription = "Hidden chats", tint = MUTED, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hidden chats", color = TEXT, fontWeight = FontWeight.Black)
                Text("blocked and rejected chats", color = MUTED, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(hiddenCount.toString(), if (hiddenCount > 0) CYAN else MUTED)
        }
    }
}

@Composable
internal fun PendingChatRow(peerId: String, name: String, onAccept: () -> Unit, onReject: () -> Unit) {
    val letter = remember(name) { contactBadgeLetter(name, "") }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(top = 13.dp, end = 2.dp, bottom = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(CYAN.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                Text(letter, color = CYAN, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, color = TEXT, fontWeight = FontWeight.Black, maxLines = 1)
                Text("New chat request", color = MUTED, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = GREEN_ON), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) { Text("Accept") }
                OutlinedButton(onClick = onReject, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) { Text("Reject", color = DANGER) }
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 54.dp).height(1.dp).background(BORDER.copy(alpha = 0.55f)))
    }
}

@Composable
internal fun ChatRowTile(peer: Peer, dao: SopduDao, batteryPercent: Int?, additionalName: String, onClick: () -> Unit) {
    val msgs by dao.observeMessages(peer.id).collectAsState(initial = emptyList())
    val lastMsg = remember(msgs) { msgs.maxByOrNull { it.ts } }
    val preview = when {
        lastMsg == null -> "No messages yet"
        lastMsg.fromMe -> "You: ${lastMsg.text}"
        else -> lastMsg.text
    }
    val badgeColor = batteryColor(batteryPercent)
    val badgeLetter = remember(peer.name, additionalName) { contactBadgeLetter(peer.name, additionalName) }
    Column(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.fillMaxWidth().padding(top = 13.dp, end = 2.dp, bottom = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(badgeColor.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.matchParentSize().background(badgeColor.copy(alpha = 0.08f), CircleShape))
                Text(badgeLetter, color = badgeColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(peerDisplayName(peer.name, additionalName), color = TEXT, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(batteryPercent?.let { "$it%" } ?: "--%", color = badgeColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black)
                        Text(formatLastSeen(peer.lastSeenAt), color = MUTED, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
                Box(Modifier.fillMaxWidth().background(PANEL_RAISED.copy(alpha = 0.72f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(preview, color = if (lastMsg == null) MUTED else TEXT, maxLines = 1, style = MaterialTheme.typography.bodyMedium, fontWeight = if (lastMsg == null) FontWeight.Normal else FontWeight.SemiBold)
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 15.dp).height(1.dp).background(BORDER.copy(alpha = 0.55f)))
    }
}

private fun contactBadgeLetter(displayName: String, additionalName: String): String {
    val source = additionalName.trim().ifBlank { displayName.trim() }
    return source.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}
