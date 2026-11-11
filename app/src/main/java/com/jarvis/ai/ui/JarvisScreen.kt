package com.jarvis.ai.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.ai.viewmodel.JarvisViewModel

private val NEON = Color(0xFF00E5FF)
private val DEEP_BLUE = Color(0xFF0066FF)

@Composable
fun JarvisScreen(vm: JarvisViewModel) {
    val messages by vm.messages.collectAsState()
    val status by vm.status.collectAsState()
    val awake by vm.awake.collectAsState()
    val bangla by vm.bangla.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("JARVIS AI", color = NEON, fontSize = 22.sp)
            TextButton(onClick = { showSettings = true }) { Text("Settings", color = NEON) }
        }

        AiCircle(status)
        Text(status.uppercase() + (if (awake) "  •  AWAKE" else ""), color = NEON, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = vm::listen, colors = ButtonDefaults.buttonColors(containerColor = DEEP_BLUE)) {
                Text("Mic")
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = vm::emergencyStop, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))) {
                Text("EMERGENCY STOP")
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(messages.reversed()) { m ->
                Text(
                    (if (m.isUser) "You: " else "JARVIS: ") + m.text,
                    color = if (m.isUser) Color.White else NEON,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Settings") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bangla recognition", Modifier.weight(1f))
                        Switch(checked = bangla, onCheckedChange = vm::setBangla)
                    }
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) { Text("Enable accessibility control") }
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Close") } }
        )
    }
}

@Composable
fun AiCircle(status: String) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val speed = if (status == "Listening" || status == "Speaking") 600 else 1800
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(speed), RepeatMode.Reverse),
        label = "pulseValue"
    )
    Canvas(Modifier.size(240.dp)) {
        val r = size.minDimension / 2
        drawCircle(
            brush = Brush.radialGradient(listOf(NEON.copy(alpha = 0.6f), Color.Transparent), radius = r * pulse),
            radius = r * pulse
        )
        drawCircle(color = NEON, radius = r * 0.6f * pulse, style = Stroke(width = 6f))
        drawCircle(color = DEEP_BLUE, radius = r * 0.35f, alpha = 0.8f)
    }
}
