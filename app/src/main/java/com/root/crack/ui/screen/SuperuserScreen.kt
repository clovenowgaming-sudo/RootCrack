package com.root.crack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.root.crack.viewmodel.SuperuserViewModel

@Composable
fun SuperuserScreen(vm: SuperuserViewModel = viewModel()) {
    val grants by vm.grants.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Text(
            "Aplikasi yang punya akses root",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        if (grants.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Belum ada aplikasi yang diberi akses root.")
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(grants) { g ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(g.appName)
                                Text("uid ${g.uid} -> ${g.toUid}", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = g.allowed,
                                onCheckedChange = { checked ->
                                    if (checked) vm.grant(g.uid) else vm.revoke(g.uid)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
