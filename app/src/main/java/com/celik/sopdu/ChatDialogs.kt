package com.celik.sopdu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun PeerInfoDialog(
    peer: Peer,
    broadcastName: String,
    additionalName: String,
    batteryPercent: Int?,
    distressStatus: String,
    onSaveLocalName: (String) -> Unit,
    onDeleteChat: () -> Unit,
    onBlockPeer: () -> Unit,
    onShareCoordinates: (Pair<Double, Double>) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var localName by remember(peer.id, additionalName) { mutableStateOf(additionalName) }
    var coordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showLocationWarning by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }
    var showBlockWarning by remember { mutableStateOf(false) }
    val timestamp = remember(peer.lastSeenAt) { formatStatusTimestamp(peer.lastSeenAt) }
    val infoText = remember(peer, broadcastName, additionalName, batteryPercent, distressStatus, timestamp, coordinates) {
        buildPeerInfoText(peer.id, broadcastName, additionalName, batteryPercent, coordinates?.first, coordinates?.second, timestamp, distressStatus)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onSaveLocalName(localName.trim()); onDismiss() }) { Text("Save", color = CYAN) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = MUTED) } },
        containerColor = PANEL,
        titleContentColor = TEXT,
        textContentColor = TEXT,
        title = { Text("Peer card", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PeerCardSection {
                    SectionLabel("Identity")
                    Text(broadcastName, color = TEXT, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(peer.id, color = MUTED, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(batteryPercent?.let { "$it%" } ?: "--%", batteryColor(batteryPercent))
                        StatusPill(distressStatus.lowercase(), if (distressStatus.equals("ACTIVE", true)) DANGER else GREEN_ON)
                    }
                }
                TextField(value = localName, onValueChange = { localName = it }, label = { Text("Add additional name") }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = PANEL2, unfocusedContainerColor = PANEL2, focusedTextColor = TEXT, unfocusedTextColor = TEXT, focusedLabelColor = CYAN, unfocusedLabelColor = MUTED, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = CYAN), shape = RoundedCornerShape(8.dp))
                PeerCardSection {
                    SectionLabel("Shared info")
                    InfoLine("Additional name", additionalName.ifBlank { "not added" })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) { InfoLine("Latitude", coordinates?.first?.toString() ?: "not shared") }
                        Column(Modifier.weight(1f)) { InfoLine("Longitude", coordinates?.second?.toString() ?: "not shared") }
                    }
                    InfoLine("Timestamp", timestamp)
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showLocationWarning = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = CYAN), shape = RoundedCornerShape(8.dp)) { Text("Share location") }
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(infoText)); Toast.makeText(context, "Peer info copied", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) { Text("Copy info", color = CYAN) }
                }
                PeerCardSection {
                    SectionLabel("Manage chat")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showDeleteWarning = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DANGER)) { Text("Delete chat", color = DANGER) }
                        OutlinedButton(onClick = { showBlockWarning = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DANGER)) { Text("Block", color = DANGER) }
                    }
                }
            }
        }
    )

    if (showLocationWarning) ShareLocationDialog(
        onConfirm = {
            showLocationWarning = false
            val last = getLastKnownCoordinates(context)
            if (last == null) {
                Toast.makeText(context, "Location unavailable. Check location permission/GPS.", Toast.LENGTH_LONG).show()
            } else {
                coordinates = last
                onShareCoordinates(last)
                Toast.makeText(context, "Coordinates shared in chat", Toast.LENGTH_SHORT).show()
            }
        },
        onDismiss = { showLocationWarning = false }
    )
    if (showDeleteWarning) ConfirmChatActionDialog(
        title = "Delete this chat?",
        message = "This deletes this local chat and its messages from this phone only. It cannot delete anything from the other device.",
        confirmText = "Delete",
        onConfirm = {
            showDeleteWarning = false
            onDeleteChat()
            Toast.makeText(context, "Chat deleted", Toast.LENGTH_SHORT).show()
            onDismiss()
        },
        onDismiss = { showDeleteWarning = false }
    )
    if (showBlockWarning) ConfirmChatActionDialog(
        title = "Block user?",
        message = "Blocked peers are ignored locally and cannot create normal chat requests on this phone.",
        confirmText = "Block",
        onConfirm = {
            showBlockWarning = false
            onBlockPeer()
            Toast.makeText(context, "Peer blocked", Toast.LENGTH_SHORT).show()
            onDismiss()
        },
        onDismiss = { showBlockWarning = false }
    )
}

@Composable
private fun PeerCardSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(PANEL2, RoundedCornerShape(8.dp)).border(1.dp, BORDER.copy(alpha = 0.75f), RoundedCornerShape(8.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun ShareLocationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PANEL,
        titleContentColor = TEXT,
        textContentColor = TEXT,
        title = { Text("Share location?", fontWeight = FontWeight.Black) },
        text = { Text("This will share your current coordinates in this chat. Only share location with people you trust.", color = MUTED) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Share location", color = CYAN) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MUTED) } }
    )
}

@Composable
private fun ConfirmChatActionDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PANEL,
        titleContentColor = TEXT,
        textContentColor = TEXT,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = { Text(message, color = MUTED) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = DANGER) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MUTED) } }
    )
}

@Composable
internal fun DistressConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = DANGER, contentColor = TEXT), shape = RoundedCornerShape(8.dp)) { Text("Start distress") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MUTED) } },
        containerColor = PANEL,
        titleContentColor = TEXT,
        textContentColor = TEXT,
        title = { Text("Start distress mode?", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This will play repeated alert sounds and flash the phone light when available.", color = TEXT)
                Text("Use it only when you need attention. It can drain battery and may be loud.", color = MUTED)
            }
        }
    )
}
