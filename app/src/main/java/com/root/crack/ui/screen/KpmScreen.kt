package com.root.crack.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.root.crack.viewmodel.KpmViewModel

@Composable
fun KpmScreen(vm: KpmViewModel = viewModel()) {
    val modules by vm.modules.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var msg by remember { mutableStateOf<String?>(null) }

    val pickKpm = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val path = com.root.crack.util.copyUriToCache(context, uri, "module.kpm")
        vm.load(path, "") { ok, out -> msg = if (ok) "KPM dimuat." else "Gagal: $out" }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { pickKpm.launch("*/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Load KPM")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                "KPM (Kernel-mode Module) berjalan langsung di kernel space via kpimg. " +
                "Cuma load .kpm dari sumber yang kamu percaya.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
            )
            msg?.let { Text(it, modifier = Modifier.padding(horizontal = 12.dp)) }

            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(modules) { k ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(k.name)
                            TextButton(onClick = { vm.unload(k.name) }) { Text("Unload") }
                        }
                    }
                }
            }
        }
    }
}
