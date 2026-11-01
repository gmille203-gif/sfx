package com.sfx.viperglass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfx.viperglass.ui.components.GlassCard
import com.sfx.viperglass.ui.components.SpectrumBars

@Composable
fun PlayerScreen() {
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.32f) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(180.dp).clip(RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                        // artwork placeholder
                        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {}
                        Text("♪", fontSize = 64.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Midnight City", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("M83 • Hurry Up, We're Dreaming", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Spacer(Modifier.height(10.dp))
                    SpectrumBars(Modifier.fillMaxWidth().height(32.dp), barCount = 24)
                    Spacer(Modifier.height(10.dp))
                    Slider(progress, { progress = it }, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1:42", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Text("4:04", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null) }
                        IconButton(onClick = { playing = !playing }, modifier = Modifier.size(64.dp).clip(CircleShape)) {
                            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                                Box(contentAlignment = Alignment.Center) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary) }
                            }
                        }
                        IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null) }
                    }
                }
            }
        }
        item { GlassCard { Text("Up Next", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp)); repeat(4) { QueueItem("Track ${it+1}", "Artist ${it+1}") } } }
        item { GlassCard { Text("Audition ViPER", fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("Musik ini lewat ViPER engine real-time. Toggle Master di Home untuk A/B.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f)) } }
    }
}

@Composable private fun QueueItem(title: String, artist: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.surfaceVariant) {}
        Column(Modifier.weight(1f)) { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(artist, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f)) }
        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
    }
}
