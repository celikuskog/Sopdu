package com.celik.sopdu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private data class DotMeta(val kind: String, val idKey: String, val title: String, val rssi: Int?, val pos: Offset, val baseRadius: Float)

@Composable
internal fun NearbyRadarScreen(peers: List<Peer>, bluetoothDevices: List<OtherDevice>, hideNonSopdu: Boolean, onToggleHideNonSopdu: (Boolean) -> Unit, isScanning: Boolean, scanLeft: Int, scanSeconds: Int, onScanNow: () -> Unit, onStopScan: () -> Unit, pinned: RadarHit?, onPinToggle: (RadarHit) -> Unit, onOpenSopduPeer: (RadarHit) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val selected = pinned
    val visibleCount = peers.size + bluetoothDevices.size
    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
        item { BrandedScreenTitle("Radar", "scan for users within about 100 m", logoTint = CYAN, logoGlow = isScanning) }
        item { FieldPanel(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(if (isScanning) "Scanning radar" else "Scan paused", color = if (isScanning) CYAN else TEXT, fontWeight = FontWeight.Black); Text(if (isScanning) "Time left: ${scanLeft}s" else "Manual only to protect battery", color = MUTED) }; Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box { IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "Options", tint = MUTED) }; DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.background(PANEL, RoundedCornerShape(12.dp))) { DropdownMenuItem(text = { Text("Hide non-Sopdu devices", color = TEXT) }, trailingIcon = { Switch(checked = hideNonSopdu, onCheckedChange = { onToggleHideNonSopdu(it) }) }, onClick = { onToggleHideNonSopdu(!hideNonSopdu) }) } }; if (!isScanning) Button(onClick = onScanNow, colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = CYAN), shape = RoundedCornerShape(8.dp)) { Text("Scan ${scanSeconds}s") } else OutlinedButton(onClick = onStopScan, colors = ButtonDefaults.outlinedButtonColors(contentColor = CYAN), shape = RoundedCornerShape(8.dp)) { Text("Stop") } } } } }
        item { Box(Modifier.fillMaxWidth().height(370.dp).background(PANEL, RoundedCornerShape(8.dp)).border(1.dp, BORDER, RoundedCornerShape(8.dp)).padding(10.dp)) { Radar(peers, bluetoothDevices, selected?.idKey, isScanning, onHit = { onPinToggle(it) }); RadarCounter("BT", bluetoothDevices.size, if (isScanning) CYAN2 else MUTED, Modifier.align(Alignment.TopStart)); RadarCounter("Sopdu", peers.size, if (isScanning) GREEN_ON else MUTED, Modifier.align(Alignment.TopEnd)) } }
        item { when { selected == null && isScanning -> FieldPanel(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tap a signal to inspect", color = TEXT, fontWeight = FontWeight.Bold); Text("${scanLeft}s", color = CYAN, fontWeight = FontWeight.Black) } }; selected == null && !isScanning && visibleCount == 0 -> FieldPanel(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("No active scan", color = TEXT, fontWeight = FontWeight.Bold); Text("Start a short radar scan when you need nearby users. Staying paused saves battery.", color = MUTED) } }; selected != null -> FieldPanel(Modifier.fillMaxWidth()) { Column { Text("Selected signal", color = CYAN, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); if (selected.kind == "BT") { val rssi = selected.rssi ?: -999; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text("Type: non-Sopdu", color = TEXT, fontWeight = FontWeight.SemiBold); Text("ID: ${selected.title}", color = MUTED) }; Column(horizontalAlignment = Alignment.End) { Text("Signal: ${if (rssi >= -60) "STRONG" else if (rssi >= -75) "MEDIUM" else "WEAK"}", color = TEXT, fontWeight = FontWeight.SemiBold); Text("RSSI: $rssi dBm", color = MUTED) } } } else { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Type: Sopdu", color = GREEN_ON, fontWeight = FontWeight.Black); Text("Name: ${selected.title}", color = TEXT, fontWeight = FontWeight.SemiBold); Text("Tap open to add this peer to chats.", color = MUTED, style = MaterialTheme.typography.bodySmall) }; Button(onClick = { onOpenSopduPeer(selected) }, colors = ButtonDefaults.buttonColors(containerColor = PANEL_RAISED, contentColor = GREEN_ON), shape = RoundedCornerShape(8.dp)) { Text("Open chat") } } } } }; else -> Unit } }
    }
}

@Composable
private fun RadarCounter(label: String, count: Int, color: Color, modifier: Modifier) {
    Column(modifier.background(BG.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        Text(count.toString(), color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun Radar(peers: List<Peer>, otherDevices: List<OtherDevice>, selectedKey: String?, isScanning: Boolean, onHit: (RadarHit) -> Unit) {
    val density = LocalDensity.current
    val tapRadiusPx = with(density) { 40.dp.toPx() }
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    val dots = remember { mutableStateListOf<DotMeta>() }
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "radar")
    val sweepAngle by infinite.animateFloat(0f, 360f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(6500, easing = androidx.compose.animation.core.LinearEasing)), label = "sweep")
    fun stableAngle(seed: String) = (((seed.hashCode() % 360) + 360) % 360).toFloat()
    fun dist(a: Offset, b: Offset): Float { val dx = a.x - b.x; val dy = a.y - b.y; return sqrt(dx * dx + dy * dy) }
    Box(Modifier.fillMaxSize().pointerInput(dots.size, canvasSize) { detectTapGestures { tap -> dots.minByOrNull { dist(tap, it.pos) }?.let { if (dist(tap, it.pos) <= tapRadiusPx) onHit(RadarHit(it.kind, it.idKey, it.title, it.rssi)) } } }) {
        Canvas(Modifier.fillMaxSize().onSizeChanged { canvasSize = it }) {
            val center = Offset(size.width / 2f, size.height / 2f); val maxR = min(size.width, size.height) * 0.43f; val rings = (1..6).map { it / 6f * maxR }
            drawLine(BORDER.copy(alpha = .72f), Offset(center.x - maxR, center.y), Offset(center.x + maxR, center.y), 2f); drawLine(BORDER.copy(alpha = .72f), Offset(center.x, center.y - maxR), Offset(center.x, center.y + maxR), 2f)
            rings.forEachIndexed { idx, r -> drawCircle((if (idx % 2 == 0) BORDER2 else BORDER).copy(alpha = .86f), r, center, style = Stroke(2.4f)) }
            val sweepColor = if (isScanning) CYAN2 else MUTED
            val sweepAlpha = if (isScanning) .78f else .34f
            rotate(sweepAngle, center) { drawLine(sweepColor.copy(alpha = sweepAlpha), center, Offset(center.x, center.y - maxR), if (isScanning) 4f else 3f) }; drawCircle(TEXT.copy(alpha = if (isScanning) .65f else .38f), 4f, center)
            dots.clear(); val placed = mutableListOf<Offset>()
            fun place(seed: String, ring: Int): Offset { val a = Math.toRadians(stableAngle(seed).toDouble()); val r = rings[ring]; return Offset(center.x + (r * sin(a)).toFloat(), center.y - (r * cos(a)).toFloat()) }
            fun drawDot(id: String, seed: String, ring: Int, color: Color, radius: Float, title: String, kind: String, rssi: Int?) { val pos = place(seed, ring); placed.add(pos); val pulse = if (abs(((sweepAngle - stableAngle(seed) + 540f) % 360f) - 180f) < 10f) .8f else 0f; drawCircle(color.copy(alpha = .75f + pulse * .25f), radius + pulse * 12f, pos); if (selectedKey == id) { drawCircle(AMBER.copy(alpha = .18f), radius + 40f, pos, style = Stroke(20f)); drawCircle(AMBER, radius + 22f, pos, style = Stroke(10f)) }; dots.add(DotMeta(kind, id, title, rssi, pos, radius)) }
            fun drawSopduSignal(id: String, seed: String, ring: Int, title: String) { val pos = place(seed, ring); val pulse = if (abs(((sweepAngle - stableAngle(seed) + 540f) % 360f) - 180f) < 10f) .8f else 0f; val r = (17 - ring).coerceAtLeast(12).toFloat(); val c = GREEN_ON.copy(alpha = .86f + pulse * .14f); drawCircle(c.copy(alpha = .16f + pulse * .18f), r + 18f, pos); drawCircle(c, r * .25f, Offset(pos.x, pos.y - r * 1.25f)); drawLine(c, Offset(pos.x - r * .82f, pos.y + r), Offset(pos.x, pos.y - r * .55f), strokeWidth = 3.5f); drawLine(c, Offset(pos.x, pos.y - r * .55f), Offset(pos.x + r * .82f, pos.y + r), strokeWidth = 3.5f); drawLine(c, Offset(pos.x - r * .82f, pos.y + r), Offset(pos.x + r * .82f, pos.y + r), strokeWidth = 3.5f); drawLine(c.copy(alpha = .9f), Offset(pos.x - r * .18f, pos.y + r * .72f), Offset(pos.x + r * .54f, pos.y + r), strokeWidth = 3.2f); if (selectedKey == id) { drawCircle(AMBER.copy(alpha = .2f), r + 40f, pos, style = Stroke(20f)); drawCircle(AMBER, r + 22f, pos, style = Stroke(10f)) }; dots.add(DotMeta("SOPDU", id, title, null, pos, r)) }
            peers.forEach { val ring = abs(it.id.hashCode()) % 6; drawSopduSignal(it.id, it.id, ring, it.name) }
            otherDevices.forEach { val ring = when { it.rssi >= -55 -> 0; it.rssi >= -62 -> 1; it.rssi >= -70 -> 2; it.rssi >= -78 -> 3; it.rssi >= -86 -> 4; else -> 5 }; drawDot(it.key, it.key, ring, CYAN2, (14 - ring).coerceAtLeast(9).toFloat(), it.key, "BT", it.rssi) }
        }
    }
}
