package com.root.crack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.root.crack.model.PatchStage
import com.root.crack.viewmodel.InstallViewModel

@Composable
fun InstallScreen(vm: InstallViewModel = viewModel()) {
    val stage by vm.stage.collectAsStateWithLifecycle()
    val log by vm.log.collectAsStateWithLifecycle()

    var bootImagePath by remember { mutableStateOf("") }
    var superKey by remember { mutableStateOf(vm.generateSuperKey()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Patch Boot Image", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Pilih boot.img atau init_boot.img hasil extract dari firmware HP kamu sendiri. " +
            "RootCrack akan nyuntik kpimg ke kernel-nya lewat kptools, lalu kasih output new-boot.img buat diflash.",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedTextField(
            value = bootImagePath,
            onValueChange = { bootImagePath = it },
            label = { Text("Path boot.img / init_boot.img") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = superKey,
            onValueChange = { superKey = it },
            label = { Text("SuperKey") },
            supportingText = { Text("Simpan baik-baik, ini kunci buat semua operasi root nanti.") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.patch(bootImagePath, superKey) },
                enabled = bootImagePath.isNotBlank() && superKey.isNotBlank(),
            ) { Text("Patch") }

            OutlinedButton(
                onClick = { vm.unpatch(bootImagePath) },
                enabled = bootImagePath.isNotBlank(),
            ) { Text("Unpatch") }
        }

        when (val s = stage) {
            is PatchStage.Done -> Text("Selesai. Output: ${s.outputPath}", color = MaterialTheme.colorScheme.primary)
            is PatchStage.Failed -> Text("Gagal: ${s.message}", color = MaterialTheme.colorScheme.error)
            PatchStage.Unpacking, PatchStage.Patching, PatchStage.Repacking -> LinearProgressIndicator(Modifier.fillMaxWidth())
            else -> {}
        }

        if (log.isNotBlank()) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Text(log, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
