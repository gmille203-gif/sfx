package com.sfx.viperglass.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfx.viperglass.ui.theme.ViperCyan
import com.sfx.viperglass.ui.theme.ViperRed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, spring(dampingRatio = 0.7f, stiffness = 400f))
    Card(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .then(if (hazeState != null) Modifier.hazeEffect(hazeState, style = HazeMaterials.ultraThinMaterial) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.07f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun IOSSwitch(checked: Boolean, onChecked: (Boolean) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Switch(
        checked = checked,
        onCheckedChange = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onChecked(it) },
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF30D158), uncheckedTrackColor = Color(0xFF636366))
    )
}

@Composable
fun PowerButton(enabled: Boolean, onToggle: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (enabled) 1f else 0.95f, spring(0.6f, 300f))
    val infinite = rememberInfiniteTransition()
    val glow by infinite.animateFloat(0.6f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    Box(
        Modifier
            .size(110.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(ViperRed, Color(0xFFFF6B30))))
            .clickable { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text("⏻", fontSize = 44.sp, color = Color.White.copy(alpha = if (enabled) 1f else 0.6f))
    }
}

@Composable
fun SpectrumBars(modifier: Modifier = Modifier, barCount: Int = 32) {
    val infinite = rememberInfiniteTransition()
    Row(modifier.height(56.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(barCount) { i ->
            val h by infinite.animateFloat(
                initialValue = 0.2f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(400 + i * 20, easing = LinearEasing), RepeatMode.Reverse)
            )
            Box(Modifier.weight(1f).fillMaxHeight(h).clip(RoundedCornerShape(4.dp)).background(Brush.verticalGradient(listOf(ViperCyan, ViperRed))))
        }
    }
}

@Composable
fun IOSSegmentedControl(options: List<String>, selected: Int, onSelect: (Int)->Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF2C2C2E)).padding(2.dp)
    ) {
        options.forEachIndexed { idx, label ->
            val isSel = idx == selected
            Box(
                Modifier.clip(RoundedCornerShape(9.dp))
                    .background(if (isSel) Color.White else Color.Transparent)
                    .clickable { onSelect(idx) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isSel) Color.Black else Color.White.copy(0.7f))
            }
        }
    }
}
