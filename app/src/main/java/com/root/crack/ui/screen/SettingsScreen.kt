package com.root.crack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.root.crack.BuildConfig
import com.root.crack.util.Prefs

@Composable
fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pengaturan", style = MaterialTheme.typography.headlineSmall)

        ListItem(
            headlineContent = { Text("Target KernelPatch ABI") },
            supportingContent = { Text(BuildConfig.KP_ABI_VERSION) },
        )
        ListItem(
            headlineContent = { Text("Versi app") },
            supportingContent = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
        )
        ListItem(
            headlineContent = { Text("SuperKey tersimpan") },
            supportingContent = { Text(if (Prefs.hasSuperKey()) "Ya" else "Belum") },
        )
    }
}
