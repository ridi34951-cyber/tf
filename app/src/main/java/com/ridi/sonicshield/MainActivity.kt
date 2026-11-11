package com.ridi.sonicshield

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private enum class SoundMode(val label: String, val detail: String) {
    ANC("ANC", "Meminta mode peredam bising pada perangkat, jika didukung."),
    TRANSPARENCY("Transparency", "Meminta mode suara sekitar pada perangkat, jika didukung."),
    OFF("Off", "Meminta mode normal tanpa ANC atau Transparency.")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SonicShieldScreen(this) }
    }
}

@Composable
private fun SonicShieldScreen(activity: ComponentActivity) {
    var selectedMode by remember { mutableStateOf(SoundMode.OFF) }
    var deviceStatus by remember { mutableStateOf("Belum diperiksa") }
    var deviceName by remember { mutableStateOf("Belum terdeteksi") }
    var micStatus by remember { mutableStateOf("Belum diuji") }
    var showPermissionInfo by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val btGranted = Build.VERSION.SDK_INT < 31 ||
            grants[Manifest.permission.BLUETOOTH_CONNECT] == true ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        if (btGranted) {
            val result = inspectBluetooth(activity)
            deviceStatus = result.first
            deviceName = result.second
        } else {
            deviceStatus = "Izin Bluetooth tidak diberikan"
        }
        micStatus = if (grants[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) "Izin mikrofon tersedia; akses mikrofon TWS tetap bergantung pada routing Android."
        else "Izin mikrofon belum diberikan"
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF79B8FF),
            secondary = Color(0xFF8DE3C0),
            background = Color(0xFF080B12),
            surface = Color(0xFF121927)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF101B30), Color(0xFF080B12), Color(0xFF090D15)))
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, null, tint = Color(0xFF79B8FF), modifier = Modifier.size(27.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("SONICSHIELD", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color.White)
                }
                Text("ADVANCED AUDIO COMPANION", color = Color(0xFF8E9BB0), fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(30.dp))

                Box(
                    modifier = Modifier.size(178.dp).border(1.dp, Color(0xFF41638E), CircleShape)
                        .background(Color(0x222C68A8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(145.dp).border(1.dp, Color(0x5579B8FF), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Headphones, null, tint = Color(0xFF9ACBFF), modifier = Modifier.size(68.dp))
                    }
                }
                Spacer(Modifier.height(15.dp))
                Text("AUDIO CONTROL", color = Color(0xFF9EB5D2), fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(23.dp))

                Text("Pilih mode", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Kontrol akan aktif jika protokol TWS dapat diakses.", color = Color(0xFF9BA8BA), fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(15.dp))

                SoundMode.values().forEach { mode ->
                    val active = selectedMode == mode
                    val accent = when (mode) {
                        SoundMode.ANC -> Color(0xFF78B7FF)
                        SoundMode.TRANSPARENCY -> Color(0xFF82DDB2)
                        SoundMode.OFF -> Color(0xFFB9C1CE)
                    }
                    Card(
                        onClick = { selectedMode = mode },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                            .border(if (active) 1.dp else 0.5.dp, if (active) accent else Color(0x334D607A), RoundedCornerShape(17.dp)),
                        colors = CardDefaults.cardColors(containerColor = if (active) accent.copy(alpha = 0.13f) else Color(0x99121B2A)),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (mode) {
                                    SoundMode.ANC -> Icons.Default.PowerSettingsNew
                                    SoundMode.TRANSPARENCY -> Icons.Default.Mic
                                    SoundMode.OFF -> Icons.Default.Headphones
                                }, null, tint = accent, modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(mode.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(mode.detail, color = Color(0xFF9BA8BA), fontSize = 11.sp)
                            }
                            RadioButton(selected = active, onClick = { selectedMode = mode }, colors = RadioButtonDefaults.colors(selectedColor = accent))
                        }
                    }
                }

                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = {
                        val permissions = buildList {
                            if (Build.VERSION.SDK_INT >= 31 && ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                                add(Manifest.permission.BLUETOOTH_CONNECT)
                            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                                add(Manifest.permission.RECORD_AUDIO)
                        }
                        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions.toTypedArray())
                        else {
                            val result = inspectBluetooth(activity)
                            deviceStatus = result.first
                            deviceName = result.second
                            micStatus = "Izin mikrofon tersedia; sumber mikrofon aktif belum dapat dipastikan."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF235B91)),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(Icons.Default.Bluetooth, null)
                    Spacer(Modifier.width(8.dp))
                    Text("PERIKSA KONEKSI TWS", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(13.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xAA111A28)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("DIAGNOSTIK", color = Color(0xFF8DBEFF), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        InfoLine("Bluetooth", deviceStatus)
                        InfoLine("Perangkat", deviceName)
                        InfoLine("Mikrofon", micStatus)
                        Divider(color = Color(0x334D607A))
                        Text("Mode dipilih: ${selectedMode.label}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Status: pilihan tersimpan di antarmuka; perintah ANC belum dikirim ke A9 Pro.", color = Color(0xFFFFD28A), fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.weight(1f))
                Text("POWERED BY RIDI", color = Color(0xFF9BAAC0), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("SonicShield 0.1.0 • Diagnostic Prototype", color = Color(0xFF64748B), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, color = Color(0xFF8D9BB0), fontSize = 11.sp)
        Spacer(Modifier.width(10.dp))
        Text(value, color = Color(0xFFE3EAF4), fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

private fun inspectBluetooth(context: Context): Pair<String, String> {
    return try {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return "Bluetooth tidak tersedia" to "-"
        if (!adapter.isEnabled) return "Bluetooth mati" to "-"
        val names = adapter.bondedDevices?.mapNotNull { it.name } ?: emptyList()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
        val connectedOutputNames = outputs.mapNotNull { it.productName?.toString() }.distinct()
        val display = (connectedOutputNames + names).distinct().joinToString(", ")
        if (display.isBlank()) "Bluetooth aktif; TWS belum terdeteksi sebagai perangkat audio" to "Tidak ditemukan"
        else "Bluetooth aktif; perangkat terdaftar/terlihat" to display
    } catch (_: SecurityException) {
        "Izin Bluetooth diperlukan" to "-"
    } catch (e: Exception) {
        "Pemeriksaan gagal: ${e.javaClass.simpleName}" to "-"
    }
}
