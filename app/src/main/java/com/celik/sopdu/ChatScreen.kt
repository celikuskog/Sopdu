package com.celik.sopdu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun ChatScreen(peer: Peer, broadcastName: String, messages: List<ChatMsg>, onBack: () -> Unit, onSend: (String) -> Unit, onSaveLocalName: (String) -> Unit, onDeleteChat: () -> Unit, onBlockPeer: () -> Unit, additionalName: String, batteryPercent: Int?, lastSeenLabel: String, distressStatus: String, connectionStatus: PeerConnectionStatus) {
    var input by remember { mutableStateOf("") }
    var showPeerInfo by remember { mutableStateOf(false) }
    val newestOtherTs = remember(messages) { messages.filterNot { it.fromMe }.maxOfOrNull { it.ts } }
    val hasMessages = messages.isNotEmpty()
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp)) {
        FieldPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BackTextButton(onClick = onBack)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f).clickable { showPeerInfo = true }.padding(vertical = 4.dp)) {
                    Text(peerDisplayName(peer.name, additionalName), color = TEXT, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("${connectionStatusLabel(connectionStatus)} - ${if (!hasMessages) "last seen $lastSeenLabel" else "tap name for peer card"}", color = connectionStatusColor(connectionStatus), style = MaterialTheme.typography.bodySmall)
                }
                StatusPill(batteryPercent?.let { "$it%" } ?: "--%", batteryColor(batteryPercent))
            }
        }
        if (showPeerInfo) PeerInfoDialog(peer, broadcastName, additionalName, batteryPercent, distressStatus, onSaveLocalName, onDeleteChat, onBlockPeer, onShareCoordinates = { coords -> onSend("coordinates: ${coords.first}, ${coords.second}") }, onDismiss = { showPeerInfo = false })
        if (connectionStatus != PeerConnectionStatus.CONNECTED) {
            Spacer(Modifier.height(10.dp))
            ConnectionNotice(connectionStatus)
        }
        Spacer(Modifier.height(16.dp))
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { FieldPanel(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { SectionLabel("Waiting for contact"); Text("No messages yet", color = TEXT, fontWeight = FontWeight.Black); Text("Last seen stays under the name until this peer sends a message.", color = MUTED) } } }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) {
                items(messages.asReversed()) { m ->
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.fromMe) Alignment.End else Alignment.Start) {
                        val bg = if (m.fromMe) Color(0xFF13202A) else PANEL2
                        Box(Modifier.padding(vertical = 6.dp).widthIn(max = 300.dp).background(bg, RoundedCornerShape(8.dp)).border(1.dp, if (m.fromMe) Color(0xFF213542) else BORDER, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                            val coords = parseCoordinateMessage(m.text)
                            if (coords != null) LocationMessageContent(coords.first, coords.second) else Text(m.text, color = TEXT)
                        }
                        if (!m.fromMe && newestOtherTs != null && m.ts == newestOtherTs) Text("last seen $lastSeenLabel - ${batteryPercent?.let { "$it%" } ?: "--%"}", color = MUTED, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp, bottom = 6.dp))
                        if (m.fromMe) Text(deliveryStatusLabel(m.deliveryStatus), color = deliveryStatusColor(m.deliveryStatus), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Message", color = MUTED) }, colors = TextFieldDefaults.colors(focusedContainerColor = PANEL, unfocusedContainerColor = PANEL, focusedTextColor = TEXT, unfocusedTextColor = TEXT, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = CYAN), shape = RoundedCornerShape(8.dp))
            Button(onClick = { val t = input.trim(); if (t.isNotEmpty()) { onSend(t); input = "" } }, colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = CYAN), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(19.dp)) }
        }
    }
}

@Composable
private fun ConnectionNotice(status: PeerConnectionStatus) {
    val color = connectionStatusColor(status)
    Row(Modifier.fillMaxWidth().background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(connectionStatusLabel(status), color = color, fontWeight = FontWeight.Black)
            Text("Messages can be saved locally before the peer reconnects.", color = MUTED, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LocationMessageContent(latitude: String, longitude: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel("Location shared")
        Text("lat $latitude", color = TEXT, fontWeight = FontWeight.SemiBold)
        Text("lon $longitude", color = TEXT, fontWeight = FontWeight.SemiBold)
    }
}

private fun connectionStatusLabel(status: PeerConnectionStatus): String = when (status) {
    PeerConnectionStatus.CONNECTING -> "connecting"
    PeerConnectionStatus.CONNECTED -> "connected"
    PeerConnectionStatus.FAILED -> "connection failed"
    PeerConnectionStatus.DISCONNECTED -> "not connected"
    PeerConnectionStatus.UNKNOWN -> "connection unknown"
}

private fun connectionStatusColor(status: PeerConnectionStatus): Color = when (status) {
    PeerConnectionStatus.CONNECTED -> GREEN_ON
    PeerConnectionStatus.CONNECTING -> CYAN
    PeerConnectionStatus.FAILED, PeerConnectionStatus.DISCONNECTED -> DANGER
    PeerConnectionStatus.UNKNOWN -> MUTED
}

private fun parseCoordinateMessage(text: String): Pair<String, String>? {
    if (!text.startsWith("coordinates:", ignoreCase = true)) return null
    val parts = text.substringAfter(":").split(",").map { it.trim() }
    if (parts.size != 2 || parts.any { it.isBlank() }) return null
    return parts[0] to parts[1]
}

private fun deliveryStatusLabel(status: MessageDeliveryStatus): String = when (status) {
    MessageDeliveryStatus.SENT -> "sent"
    MessageDeliveryStatus.SAVED_LOCAL -> "saved locally"
    MessageDeliveryStatus.FAILED -> "failed"
    MessageDeliveryStatus.RECEIVED -> ""
}

private fun deliveryStatusColor(status: MessageDeliveryStatus): Color = when (status) {
    MessageDeliveryStatus.SENT -> GREEN_ON
    MessageDeliveryStatus.SAVED_LOCAL -> MUTED
    MessageDeliveryStatus.FAILED -> DANGER
    MessageDeliveryStatus.RECEIVED -> MUTED
}
