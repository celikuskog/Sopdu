package com.celik.sopdu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
internal fun TopRightToolsPopout(open: Boolean, onToggle: () -> Unit, btOn: Boolean, hotspotOn: Boolean, onBluetoothTap: () -> Unit, onHotspotTap: () -> Unit, onCallTap: () -> Unit, distressActive: Boolean, onDistressTap: () -> Unit) {
    val arrowRotation = animateFloatAsState(
        targetValue = if (open) 180f + 360f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "tools-arrow-rotation"
    )
    Box(Modifier.fillMaxSize()) {
        if (open) Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { onToggle() }) })
        Box(Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 14.dp).size(44.dp).background(PANEL_RAISED, RoundedCornerShape(8.dp)).border(1.dp, BORDER2, RoundedCornerShape(8.dp)).clickable { onToggle() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = ORANGE_ON.copy(alpha = 0.28f), modifier = Modifier.size(31.dp).graphicsLayer { rotationZ = arrowRotation.value; scaleX = 1.32f; scaleY = 1.32f })
            Icon(Icons.Filled.ExpandMore, contentDescription = "Tools", tint = ORANGE_ON, modifier = Modifier.size(25.dp).graphicsLayer { rotationZ = arrowRotation.value })
        }
        if (open) {
            Column(Modifier.align(Alignment.TopEnd).padding(top = 64.dp, end = 14.dp).background(PANEL, RoundedCornerShape(8.dp)).border(1.dp, BORDER2, RoundedCornerShape(8.dp)).padding(10.dp).pointerInput(Unit) { detectTapGestures(onTap = { }) }, verticalArrangement = Arrangement.spacedBy(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                QuickToolButton(Icons.Filled.Bluetooth, "Bluetooth settings", btOn, CYAN2, onBluetoothTap)
                QuickToolButton(Icons.Filled.WifiTethering, "Tethering settings", hotspotOn, ORANGE_ON, onHotspotTap)
                QuickToolButton(Icons.Filled.Phone, "Direct dialer", true, GREEN_ON, onCallTap)
                QuickToolButton(Icons.Filled.Warning, if (distressActive) "Stop distress mode" else "Start distress mode", distressActive, DANGER, onDistressTap)
            }
        }
    }
}

@Composable
internal fun QuickToolButton(icon: ImageVector, label: String, on: Boolean, onColor: Color, onClick: () -> Unit) {
    val tint = if (on) onColor else MUTED
    val bg = if (on) tint.copy(alpha = 0.16f) else Color(0xFF0D1115)
    Box(Modifier.size(54.dp).background(bg, RoundedCornerShape(8.dp)).border(1.dp, if (on) tint.copy(alpha = 0.35f) else BORDER, RoundedCornerShape(8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}
