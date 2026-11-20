package com.example.noirdialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.util.Date

private val Ink = Color(0xFF05070B)
private val Panel = Color(0xFF111722)
private val Blue = Color(0xFFD8D8D8)
private val Green = Color(0xFFE8E8E8)
private val Muted = Color(0xFF8D9AAA)

data class RecentCall(val name: String, val number: String, val detail: String, val incoming: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NoirApp() }
    }
}

@Composable
private fun NoirApp() {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var number by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<RecentCall?>(null) }
    var light by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("English") }
    var showSettings by remember { mutableStateOf(false) }
    var showDialpad by remember { mutableStateOf(true) }

    val recent = remember {
        listOf(
            RecentCall("Alex Morgan", "+1 202 555 0148", "Mobile · Today, 10:42", true),
            RecentCall("Mina Rahimi", "+98 912 345 6789", "Mobile · Yesterday, 18:20", false),
            RecentCall("Noah Williams", "+44 7700 900123", "Mobile · Yesterday, 14:05", true),
            RecentCall("Sara Chen", "+86 138 0013 8000", "Mobile · Monday, 09:12", true),
            RecentCall("Unknown caller", "+994 50 555 01 02", "Mobile · Monday, 08:41", false)
        )
    }
    val bg = if (light) Color(0xFFF3F7FA) else Ink
    val fg = if (light) Color(0xFF101820) else Color.White

    MaterialTheme(colorScheme = darkColorScheme(
        background = bg, surface = if (light) Color.White else Panel,
        onBackground = fg, onSurface = fg, primary = Blue
    )) {
        Box(Modifier.fillMaxSize().background(bg)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("NOIR", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        Text(when(tab) { 0 -> "Recents"; 1 -> "Contacts"; else -> "Keypad" },
                            color = fg, fontSize = 31.sp, fontWeight = FontWeight.Light)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Tune, null, tint = Green)
                    }
                }

                AnimatedVisibility(visible = tab != 2) {
                    Column {
                        Spacer(Modifier.height(14.dp))
                        GlassSearch(query, { query = it }, light)
                        Spacer(Modifier.height(18.dp))
                    }
                }

                when (tab) {
                    0 -> {
                        Text("RECENT ACTIVITY", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
                            items(recent.filter { it.name.contains(query, true) || it.number.contains(query) }) { call ->
                                RecentRow(call, fg) { selected = call }
                            }
                        }
                    }
                    1 -> {
                        Text("YOUR PEOPLE", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.weight(1f)) {
                            items(recent.filter { it.name.contains(query, true) || it.number.contains(query) }.distinctBy { it.name }) { call ->
                                RecentRow(call, fg) { selected = call }
                            }
                        }
                    }
                    else -> {
                        Spacer(Modifier.weight(1f))
                        Text(number.ifBlank { "Enter number" }, Modifier.fillMaxWidth(), color = fg, fontSize = 30.sp)
                        Spacer(Modifier.height(18.dp))
                        DialPad(onDigit = { number += it }, onDelete = { number = number.dropLast(1) })
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = { if (number.isNotBlank()) placeCall(context, number) },
                                modifier = Modifier.size(66.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE4E4E4), contentColor = Ink)
                            ) { Icon(Icons.Default.Call, "Call", modifier = Modifier.size(28.dp)) }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }

                NavigationBar(containerColor = if (light) Color.White else Color(0xE8171D26), tonalElevation = 0.dp) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Recents") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.People, null) }, label = { Text("Contacts") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Dialpad, null) }, label = { Text("Keypad") })
                }
                Spacer(Modifier.height(8.dp))
            }

            selected?.let { call ->
                ContactSheet(call, onDismiss = { selected = null }, onCall = { placeCall(context, call.number); selected = null })
            }
            if (showSettings) {
                SettingsSheet(
                    light = light, onLight = { light = it },
                    language = language, onLanguage = { language = it },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@Composable
private fun GlassSearch(value: String, onValue: (String) -> Unit, light: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(if (light) Color(0xFFE7EDF2) else Color(0xCC202A36))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = Blue)
        Spacer(Modifier.width(10.dp))
        BasicTextField(value, onValue, modifier = Modifier.weight(1f), singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = if (light) Ink else Color.White, fontSize = 14.sp),
            decorationBox = { inner -> if (value.isEmpty()) Text("Search contacts…", color = Muted); inner() })
    }
}

@Composable
private fun RecentRow(call: RecentCall, fg: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF777777), Color(0xFF252525)))),
            contentAlignment = Alignment.Center) {
            Text(call.name.take(1).uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(call.name, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("${if (call.incoming) "↙" else "↗"}  ${call.detail}", color = Muted, fontSize = 11.sp)
        }
        IconButton(onClick = onClick) { Icon(Icons.Default.Call, "Call ${call.name}", tint = Green) }
    }
}

@Composable
private fun DialPad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val keys = listOf("1" to "", "2" to "ABC", "3" to "DEF", "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ", "*" to "", "0" to "+", "#" to "")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { (digit, letters) ->
                    Column(
                        Modifier.weight(1f).padding(horizontal = 8.dp).aspectRatio(1f)
                            .clip(CircleShape).background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x44FFFFFF), CircleShape).clickable { onDigit(digit) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(digit, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
                        if (letters.isNotEmpty()) Text(letters, color = Color(0xFF999999), fontSize = 8.sp, letterSpacing = 2.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDelete) { Icon(Icons.Default.Backspace, null, tint = Color.LightGray); Text("  Delete", color = Color.LightGray) }
        }
    }
}

@Composable
private fun ContactSheet(call: RecentCall, onDismiss: () -> Unit, onCall: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(82.dp).clip(RoundedCornerShape(25.dp)).background(Brush.linearGradient(listOf(Color(0xFF777777), Color(0xFF292929)))), contentAlignment = Alignment.Center) {
                    Text(call.name.take(1), fontSize = 34.sp, color = Ink, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text(call.name, color = Color.White, fontSize = 24.sp)
                Text(call.number, color = Muted, fontSize = 14.sp)
            }
        },
        text = { Text("Contact details · Add notes and reminders in a future release.", color = Muted) },
        confirmButton = { TextButton(onClick = onCall) { Icon(Icons.Default.Call, null, tint = Green); Text("  Call", color = Green) } },
        dismissButton = { TextButton(onClick = onDismiss) { Icon(Icons.Default.Message, null, tint = Blue); Text("  SMS", color = Blue) } }
    )
}

@Composable
private fun SettingsSheet(light: Boolean, onLight: (Boolean) -> Unit, language: String, onLanguage: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Panel,
        title = { Text("Appearance & language", color = Color.White) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Light theme", Modifier.weight(1f), color = Color.White)
                    Switch(checked = light, onCheckedChange = onLight)
                }
                Spacer(Modifier.height(8.dp))
                Text("Language", color = Muted)
                listOf("English", "فارسی", "中文", "हिन्दी").forEach {
                    Row(Modifier.fillMaxWidth().clickable { onLanguage(it) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = language == it, onClick = { onLanguage(it) })
                        Text(it, color = Color.White)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = Green) } }
    )
}

private fun placeCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(number)}"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        runCatching { context.startActivity(intent) }
    } else {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")))
    }
}
