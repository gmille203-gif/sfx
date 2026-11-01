package com.sfx.viperglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfx.viperglass.data.ViPERDevice
import com.sfx.viperglass.ui.components.*

@Composable
fun HomeScreen(
    masterEnabled: Boolean,
    onToggleMaster: () -> Unit,
    selectedDevice: Int,
    onSelectDevice: (Int)->Unit
) {
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("MASTER POWER", fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit(1.2f, androidx.compose.ui.unit.TextUnitType.Sp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Spacer(Modifier.height(12.dp))
                    PowerButton(masterEnabled, onToggleMaster)
                    Spacer(Modifier.height(10.dp))
                    Text(if (masterEnabled) "ViPER Engine Aktif • Ketuk untuk matikan" else "Master Mati • Ketuk untuk nyalakan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Spacer(Modifier.height(14.dp))
                    SpectrumBars(Modifier.fillMaxWidth())
                    Text("64-bar Live Spectrum • 60fps", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.35f))
                }
            }
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IOSSegmentedControl(ViPERDevice.values().map { it.label }, selectedDevice, onSelectDevice)
                Spacer(Modifier.height(8.dp))
                Text("Output: ${ViPERDevice.values()[selectedDevice].label} • Auto-detect via AudioDeviceCallback", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
        // Bento cards
        item { EffectMiniCard("Bass Booster","Deep low-end", "𝄞", true) }
        item { EffectMiniCard("Clarity","XHiFi vocal", "✦", true) }
        item { EffectMiniCard("Convolver","IRS Studio", "◐", false) }
        item { EffectMiniCard("Reverb","Room Hall", "◎", true) }
        item { EffectMiniCard("FET Compressor","Punch", "▅", false) }
        item { EffectMiniCard("DDC","Sony WH-1000XM5", "♡", true) }
        item { EffectMiniCard("Surround","Field/Diff", "◑", false) }
        item { EffectMiniCard("Limiter","-1.0 dB", "◒", true) }
    }
}

@Composable
private fun EffectMiniCard(title: String, subtitle: String, icon: String, enabled: Boolean) {
    var on by remember { mutableStateOf(enabled) }
    GlassCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(icon, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            IOSSwitch(on) { on = it }
        }
    }
}
