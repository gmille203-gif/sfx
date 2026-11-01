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

@Composable
fun PresetsScreen() {
    var filter by remember { mutableStateOf("All") }
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Text("Presets", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All","Bass","Vocal","Gaming","Studio").forEach {
                        FilterChip(filter==it, { filter=it }, label = { Text(it, fontSize = 12.sp) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField("", {}, placeholder={Text("Search presets…", fontSize=13.sp)}, modifier=Modifier.fillMaxWidth())
            }
        }
        items(8) { idx ->
            GlassCard {
                Text("🎨", fontSize=28.sp)
                Spacer(Modifier.height(8.dp))
                Text("Preset ${idx+1}", fontWeight=FontWeight.SemiBold, fontSize=13.sp)
                Text(if(idx%2==0) "Bass Heavy • 2.1k likes" else "Vocal Clear • 890 likes", fontSize=11.sp, color=MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    SuggestionChip({}, label={Text("Share", fontSize=11.sp)})
                    SuggestionChip({}, label={Text("Apply", fontSize=11.sp)})
                }
            }
        }
        item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(2)}){
            Button(onClick={}, modifier=Modifier.fillMaxWidth()){ Text("＋ Save Current as Preset") }
        }
    }
}
