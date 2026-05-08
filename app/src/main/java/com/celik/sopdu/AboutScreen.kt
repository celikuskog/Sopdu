package com.celik.sopdu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AboutScreen(onBack: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(bottom = 26.dp)) {
        item { BackTextButton(onClick = onBack, color = COPPER) }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(painter = painterResource(id = R.drawable.sopdu_launcher_mark_foreground), contentDescription = "Sopdu", modifier = Modifier.size(134.dp))
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(86.dp).height(2.dp).background(COPPER.copy(alpha = 0.72f), RoundedCornerShape(2.dp)))
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                AboutLead("Sopdu is built for moments when internet access, mobile networks, or familiar communication channels are unavailable, overloaded, or unreliable.")
                AboutParagraph("It can help nearby people discover each other, exchange short messages, save local notes about contacts, and share coordinates when they choose to. Sopdu is a support tool only. It is not an emergency service, medical tool, rescue system, or replacement for official safety instructions.")
                AboutAccent("Use official help first whenever it is reachable.")
                AboutParagraph("If you can safely call, text, radio, or otherwise reach help, try local emergency numbers, ambulance, fire response, rescue teams, or trusted local authorities before relying on any app.")
                AboutParagraph("Follow local safety instructions and pay attention to the environment around you. Damaged buildings, unstable ground, fire, smoke, crowds, weather, and blocked routes can change what is safe. Do not take unnecessary risks.")
                AboutParagraph("When ordinary communication is not working, use Sopdu to coordinate carefully with people nearby. Keep messages short, identify who you are talking to, and avoid sharing sensitive information unless the situation makes it necessary.")
                AboutAccent("Radar range is approximate.")
                AboutParagraph("In a clear open area, detection may reach about 100 m. Walls, floors, damaged structures, crowds, vehicles, interference, phone hardware, low battery, and battery saver settings can reduce that distance.")
                AboutParagraph("Move only when it is safe, and keep watching the area around you. Do not enter unsafe places or separate from trusted people without a clear reason.")
                AboutParagraph("Sopdu uses manual scans so your phone is not constantly searching in the background. Preserve battery where you can. Distress sound and flashlight can help draw attention, but they may drain battery and may not be appropriate in every situation.")
                AboutParagraph("Sopdu does not require an account, profile photo, or social media connection. Your Sopdu ID identifies this app installation on this phone. Additional names are saved locally, and coordinates are shared only when you choose to send them.")
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.width(42.dp).height(1.dp).background(BORDER, RoundedCornerShape(1.dp)))
                Text("Sopdu is being prepared for public testing. Features may change as connection reliability, distress behavior, and privacy controls are tested further.", color = MUTED, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(18.dp))
                Text("All rights reserved. Copyright 2026 Sopdu.", color = COPPER, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AboutLead(text: String) {
    Text(text, color = TEXT, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun AboutParagraph(text: String) {
    Text(text, color = MUTED, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun AboutAccent(text: String) {
    Text(text, color = COPPER, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
}
