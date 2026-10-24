package com.root.crack.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.root.crack.viewmodel.ModuleViewModel

@Composable
fun ModulesScreen(vm: ModuleViewModel = viewModel()) {
    val modules by vm.modules.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var statusMsg by remember { mutableStateOf<String?>(null) }

    val pickZip = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val path = com.root.crack.util.copyUriToCache(context, uri, "module.zip")
        vm.install(path) { ok, msg -> statusMsg = if (ok) "Modul terpasang, aktif setelah reboot." else "Gagal: $msg" }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { pickZip.launch("application/zip") }) {
                Icon(Icons.Default.Add, contentDescription = "Install module")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            statusMsg?.let {
                Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
            if (modules.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Belum ada module terpasang.")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modules) { m ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(m.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${m.id} · v${m.version}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    Switch(checked = m.enabled, onCheckedChange = { vm.setEnabled(m.id, it) })
                                    IconButton(onClick = { vm.remove(m.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
