package com.celik.sopdu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal val BG = Color(0xFF090B0D)
internal val PANEL = Color(0xFF11161B)
internal val PANEL2 = Color(0xFF161C22)
internal val PANEL_RAISED = Color(0xFF1A2229)
internal val BORDER = Color(0xFF26323B)
internal val BORDER2 = Color(0xFF344653)
internal val TEXT = Color(0xFFE8EEF2)
internal val MUTED = Color(0xFF94A1AA)
internal val CYAN = Color(0xFF8FD8FF)
internal val CYAN2 = Color(0xFF4FA9D8)
internal val GREEN_ON = Color(0xFF65D46E)
internal val ORANGE_ON = Color(0xFFE9A23B)
internal val AMBER = Color(0xFFF0C15A)
internal val DANGER = Color(0xFFE45D4F)
internal val COPPER = Color(0xFFD58A58)

@Composable
internal fun LaunchBrandScreen() {
    Box(Modifier.fillMaxSize().background(BG), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = "Sopdu", modifier = Modifier.size(240.dp))
            Text("Sopdu", color = TEXT, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("offline communication utility", color = MUTED, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun BrandHomeScreen(onBack: () -> Unit, onAbout: () -> Unit) {
    Box(Modifier.fillMaxSize().background(BG).padding(horizontal = 22.dp, vertical = 22.dp)) {
        BackTextButton(onClick = onBack, color = COPPER)
        Column(Modifier.align(Alignment.Center).offset(y = (-28).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = "Sopdu", modifier = Modifier.size(232.dp))
            Text("Sopdu", color = TEXT, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("offline communication utility", color = MUTED, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Box(Modifier.width(74.dp).height(2.dp).background(COPPER.copy(alpha = 0.62f), RoundedCornerShape(2.dp)))
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(PANEL, RoundedCornerShape(8.dp)).border(1.dp, BORDER, RoundedCornerShape(8.dp)).clickable { onAbout() }.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("About", color = TEXT, fontWeight = FontWeight.Black)
                    Text("mission, rights, and current build notes", color = MUTED, style = MaterialTheme.typography.bodySmall)
                }
                Text(">", color = MUTED, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun BrandedScreenTitle(title: String, subtitle: String? = null, onLogoClick: (() -> Unit)? = null, logoTint: Color? = null, logoGlow: Boolean = false) {
    val logoColor = logoTint ?: COPPER
    val glowTransition = rememberInfiniteTransition(label = "brand-logo-glow")
    val glowPulse by glowTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(720, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "brand-logo-glow-pulse")
    val activeLogoColor = if (logoGlow) lerp(CYAN2.copy(alpha = 0.72f), CYAN.copy(alpha = 1f), glowPulse) else logoColor
    val logoModifier = Modifier.size(48.dp).then(if (onLogoClick != null) Modifier.clickable { onLogoClick() } else Modifier)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(logoModifier, contentAlignment = Alignment.Center) {
            if (logoGlow) {
                Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = null, modifier = Modifier.size(66.dp).graphicsLayer { alpha = 0.22f + glowPulse * 0.46f; scaleX = 1.02f + glowPulse * 0.13f; scaleY = 1.02f + glowPulse * 0.13f }, colorFilter = ColorFilter.tint(CYAN))
                Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = null, modifier = Modifier.size(57.dp).graphicsLayer { alpha = 0.36f + glowPulse * 0.42f; scaleX = 1f + glowPulse * 0.07f; scaleY = 1f + glowPulse * 0.07f }, colorFilter = ColorFilter.tint(CYAN))
            }
            Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = "Sopdu", modifier = Modifier.size(46.dp).graphicsLayer { if (logoGlow) { alpha = 0.88f + glowPulse * 0.12f; scaleX = 0.98f + glowPulse * 0.06f; scaleY = 0.98f + glowPulse * 0.06f } }, colorFilter = ColorFilter.tint(activeLogoColor))
        }
        ScreenTitle(title, subtitle)
    }
}

@Composable
internal fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = TEXT, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        if (subtitle != null) Text(subtitle, color = MUTED, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun FieldPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.background(PANEL, RoundedCornerShape(8.dp)).border(1.dp, BORDER, RoundedCornerShape(8.dp)).padding(14.dp)) { content() }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(text.uppercase(), color = MUTED, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
}

@Composable
internal fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = modifier.background(color.copy(alpha = 0.13f), RoundedCornerShape(6.dp)).border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 5.dp))
}

@Composable
internal fun InfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = MUTED, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(value, color = TEXT, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun BackTextButton(text: String = "Back", color: Color = CYAN, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = text, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun BottomNavigationBarIcons(tab: Tab, onTab: (Tab) -> Unit) {
    NavigationBar(containerColor = Color(0xFF0D1115), contentColor = TEXT, tonalElevation = 0.dp) {
        NavigationBarItem(selected = tab == Tab.CHATS, onClick = { onTab(Tab.CHATS) }, icon = { Icon(Icons.Filled.Home, "Home", tint = if (tab == Tab.CHATS) CYAN else MUTED) }, label = { Text("Chats") }, colors = NavigationBarItemDefaults.colors(indicatorColor = PANEL_RAISED))
        NavigationBarItem(selected = tab == Tab.NEARBY, onClick = { onTab(Tab.NEARBY) }, icon = { Icon(Icons.Filled.TrackChanges, "Radar", tint = if (tab == Tab.NEARBY) CYAN else MUTED) }, label = { Text("Radar") }, colors = NavigationBarItemDefaults.colors(indicatorColor = PANEL_RAISED))
        NavigationBarItem(selected = tab == Tab.SETTINGS, onClick = { onTab(Tab.SETTINGS) }, icon = { Icon(Icons.Filled.Settings, "Settings", tint = if (tab == Tab.SETTINGS) CYAN else MUTED) }, label = { Text("Settings") }, colors = NavigationBarItemDefaults.colors(indicatorColor = PANEL_RAISED))
    }
}

internal fun batteryColor(percent: Int?): Color = when {
    percent == null -> MUTED
    percent <= 15 -> DANGER
    percent <= 35 -> ORANGE_ON
    else -> GREEN_ON
}

internal fun peerDisplayName(displayName: String, additionalName: String): String = if (additionalName.isBlank()) displayName else "$displayName | $additionalName"
