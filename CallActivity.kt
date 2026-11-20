package com.example.noirdialer.telecom

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        setContent { CallScreen(onFinish = { finish() }) }
    }
}

@Composable
private fun CallScreen(onFinish: () -> Unit) {
    val call = ActiveCallHolder.call
    var elapsed by remember { mutableIntStateOf(0) }
    val incoming = call?.state == Call.STATE_RINGING
    val address = call?.details?.handle?.schemeSpecificPart ?: "Unknown number"
    LaunchedEffect(call) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (call?.state == Call.STATE_ACTIVE) elapsed++
        }
    }
    Box(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF555555), Color(0xFF191919), Color.Black)))) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(72.dp))
            Box(Modifier.size(116.dp).clip(RoundedCornerShape(32.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF888888), Color(0xFF242424))))
                .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
                Text(address.take(1).uppercase(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Light)
            }
            Spacer(Modifier.height(24.dp))
            Text(address, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
            Text(when {
                incoming -> "Incoming call"
                call?.state == Call.STATE_ACTIVE -> "%02d:%02d".format(elapsed / 60, elapsed % 60)
                else -> "Calling…"
            }, color = Color.LightGray, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            if (incoming) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CallAction(Color(0xFF9E4848), Icons.Default.CallEnd, "Decline") { call?.reject(false, null); onFinish() }
                    CallAction(Color(0xFF5D805F), Icons.Default.Call, "Answer") { call?.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY) }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CallAction(Color(0x553FFFFFFF), Icons.Default.Mic, "Mute") { ActiveCallHolder.service?.setMuted(true) }
                    CallAction(Color(0x553FFFFFFF), Icons.Default.VolumeUp, "Speaker") { ActiveCallHolder.service?.setAudioRoute(android.telecom.CallAudioState.ROUTE_SPEAKER) }
                    CallAction(Color(0xFF9E4848), Icons.Default.CallEnd, "End") { call?.disconnect(); onFinish() }
                }
            }
            Spacer(Modifier.height(55.dp))
        }
    }
}

@Composable
private fun CallAction(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(68.dp).clip(CircleShape)
            .background(color).border(1.dp, Color(0x66FFFFFF), CircleShape)) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}
