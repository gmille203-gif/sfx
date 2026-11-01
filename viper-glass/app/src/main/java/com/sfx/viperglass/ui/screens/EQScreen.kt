package com.sfx.viperglass.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfx.viperglass.ui.components.GlassCard
import kotlin.math.pow

@Composable
fun EQScreen() {
    var gains by remember { mutableStateOf(List(10){ 0f }) }
    var ab by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Equalizer Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(ab, { ab = !ab }, label = { Text(if (ab) "B" else "A") })
                    FilterChip(false, {}, label = { Text("🪄 AutoEQ") })
                }
            }
            Spacer(Modifier.height(12.dp))
            // Bezier curve
            Canvas(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))) {
                val path = Path()
                val w = size.width; val h = size.height
                val points = gains.mapIndexed { i, g -> Offset(i * w / 9f, h/2 - g * h/2/12f) }
                if (points.isNotEmpty()) {
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i-1]; val cur = points[i]
                        val midX = (prev.x + cur.x)/2
                        path.quadraticBezierTo(prev.x, prev.y, midX, (prev.y+cur.y)/2)
                    }
                    drawPath(path, Color(0xFFFF3B30), style = Stroke(3f))
                    points.forEach { drawCircle(Color.White, 6f, it) }
                }
                // grid
                repeat(4) { i -> drawLine(Color.White.copy(0.08f), Offset(0f, i*h/4), Offset(w, i*h/4), 1f) }
            }
            Text("20Hz ——————————————————————— 20kHz", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
        // Sliders
        GlassCard {
            Text("10-Band Graphic EQ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                gains.forEachIndexed { idx, g ->
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Slider(value = g, onValueChange = { gains = gains.toMutableList().apply { set(idx, it) } }, valueRange = -12f..12f, modifier = Modifier.width(28.dp).height(120.dp))
                        Text("${idx+1}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("-12 dB", fontSize = 10.sp); Text("+12 dB", fontSize = 10.sp) }
        }
        GlassCard {
            Text("Preset Cepat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Flat","Bass Heavy","Vocal","Gaming","Acoustic").forEach { SuggestionChip(onClick = {}, label = { Text(it, fontSize = 12.sp) }) }
            }
        }
    }
}
