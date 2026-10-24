package com.root.crack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.root.crack.viewmodel.HomeViewModel

@Composable
fun HomeScreen(onGoInstall: () -> Unit, vm: HomeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("RootCrack", style = MaterialTheme.typography.headlineMedium)

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!state.superKeyValid) {
                    Text("Belum dipatch / SuperKey belum diset.", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = onGoInstall) { Text("Patch boot image sekarang") }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Root aktif")
                    }
                    Text("KernelPatch: ${state.kernelPatchVersion}")
                    Text("Root enabled: ${state.rootEnabled}")
                    OutlinedButton(onClick = vm::refresh) { Text("Refresh status") }
                }
            }
        }
    }
}
