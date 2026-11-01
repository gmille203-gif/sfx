package com.sfx.viperglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfx.viperglass.ui.components.GlassCard
import com.sfx.viperglass.ui.components.IOSSwitch

@Composable
fun LabScreen() {
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) { Text("Lab — Semua Efek", fontWeight = FontWeight.Bold, fontSize = 22.sp) }
        item { LabCard("Convolver","IRS Studio • WAV","◐", false) }
        item { LabCard("DDC","Device Correction","♡", true) }
        item { LabCard("Reverb","Hall / Room","◎", true) }
        item { LabCard("Bass","80Hz • Mono","𝄞", true) }
        item { LabCard("FET Compressor","Threshold -20","▅", false) }
        item { LabCard("Field Surround","Spatial","◑", false) }
        item { LabCard("Headphone Surround","3D","◒", true) }
        item { LabCard("Cure","Anti-fatigue","✚", false) }
        item { LabCard("Tube Simulator","Warmth","◍", false) }
        item { LabCard("Limiter","-1.0 dB","⬓", true) }
        item { LabCard("AnalogX","Exciter","✦", false) }
        item { LabCard("Spectrum Ext","Air","⬢", false) }
    }
}

@Composable private fun LabCard(title: String, subtitle: String, icon: String, defaultOn: Boolean) {
    var on by remember { mutableStateOf(defaultOn) }
    var show by remember { mutableStateOf(false) }
    GlassCard(onClick = { show = true }) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(icon, fontSize = 20.sp)
                IOSSwitch(on) { on = it }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
        if (show) {
            // Bottom sheet detail would go here — simplified
        }
    }
}
