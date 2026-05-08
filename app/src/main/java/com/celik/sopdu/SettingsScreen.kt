package com.celik.sopdu

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsScreen(identity: LocalIdentity, onExportChats: () -> Unit, onClearAllMessages: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var notifSound by remember { mutableStateOf(true) }
    var notifVibrate by remember { mutableStateOf(true) }
    var showExportWarning by remember { mutableStateOf(false) }
    var showClearMessagesWarning by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle("Settings", "Identity and emergency defaults") }
        item { IdentitySettingsCard(identity, onCopyId = { clipboard.setText(AnnotatedString(identity.deviceId)); Toast.makeText(context, "Device ID copied", Toast.LENGTH_SHORT).show() }) }
        item { AlertsSettingsCard(notifSound, { notifSound = it }, notifVibrate, { notifVibrate = it }) }
        item { ExportSettingsCard { showExportWarning = true } }
        item { MessageHistorySettingsCard { showClearMessagesWarning = true } }
    }

    if (showExportWarning) {
        AlertDialog(
            onDismissRequest = { showExportWarning = false },
            containerColor = PANEL,
            titleContentColor = TEXT,
            textContentColor = TEXT,
            title = { Text("Export chats?", fontWeight = FontWeight.Black) },
            text = { Text("Exported chats may include sensitive messages, coordinates, peer IDs, and local names. Anyone with this JSON file can read it. Only export if you trust where you save it.", color = MUTED) },
            confirmButton = { TextButton(onClick = { showExportWarning = false; onExportChats() }) { Text("Export", color = CYAN) } },
            dismissButton = { TextButton(onClick = { showExportWarning = false }) { Text("Cancel", color = MUTED) } }
        )
    }

    if (showClearMessagesWarning) {
        AlertDialog(
            onDismissRequest = { showClearMessagesWarning = false },
            containerColor = PANEL,
            titleContentColor = TEXT,
            textContentColor = TEXT,
            title = { Text("Clear all messages?", fontWeight = FontWeight.Black) },
            text = { Text("This deletes message history stored on this phone only. Chat contacts, additional names, blocked users, and your Sopdu ID will stay.", color = MUTED) },
            confirmButton = { TextButton(onClick = { showClearMessagesWarning = false; onClearAllMessages() }) { Text("Clear", color = DANGER) } },
            dismissButton = { TextButton(onClick = { showClearMessagesWarning = false }) { Text("Cancel", color = MUTED) } }
        )
    }
}

@Composable
private fun IdentitySettingsCard(identity: LocalIdentity, onCopyId: () -> Unit) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("Local identity")
            Text("Your Sopdu ID", color = CYAN, fontWeight = FontWeight.Black)
            Text("Generated on this phone and cannot be changed. No account, profile photo, or social connection is attached.", color = MUTED)
            InfoLine("Device ID", identity.deviceId)
            OutlinedButton(onClick = onCopyId, shape = RoundedCornerShape(8.dp)) {
                Text("Copy ID", color = CYAN)
            }
        }
    }
}

@Composable
private fun AlertsSettingsCard(notifSound: Boolean, onSoundChange: (Boolean) -> Unit, notifVibrate: Boolean, onVibrateChange: (Boolean) -> Unit) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("Alerts")
            SettingsToggleRow("Notification sound", "Future alert sound preference", notifSound, onSoundChange)
            SettingsToggleRow("Vibrate", "Future alert vibration preference", notifVibrate, onVibrateChange)
        }
    }
}

@Composable
private fun ExportSettingsCard(onExport: () -> Unit) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Chat export")
            Text("Export chats", color = TEXT, fontWeight = FontWeight.Black)
            Text("Save a readable JSON file with local messages, peer IDs, additional names, and shared coordinates.", color = MUTED)
            OutlinedButton(onClick = onExport, shape = RoundedCornerShape(8.dp)) {
                Text("Export JSON", color = CYAN)
            }
        }
    }
}

@Composable
private fun MessageHistorySettingsCard(onClearMessages: () -> Unit) {
    FieldPanel(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("Message history")
            Text("Clear all messages", color = TEXT, fontWeight = FontWeight.Black)
            Text("Deletes local message history only. Chats, additional names, blocks, and your Sopdu ID stay on this phone.", color = MUTED)
            OutlinedButton(onClick = onClearMessages, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DANGER)) {
                Text("Clear messages", color = DANGER)
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = TEXT, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MUTED, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked, onCheckedChange = onCheckedChange)
    }
}
