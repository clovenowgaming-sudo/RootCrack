package com.root.crack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.root.crack.model.RcModule
import com.root.crack.util.RcCli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModuleViewModel : ViewModel() {
    private val _modules = MutableStateFlow<List<RcModule>>(emptyList())
    val modules: StateFlow<List<RcModule>> = _modules.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.listModules()
            _modules.value = result.out.mapNotNull { line ->
                // "<id> (<name>) v<version>"
                val m = Regex("""^(\S+) \((.+)\) v(.+)$""").find(line) ?: return@mapNotNull null
                RcModule(
                    id = m.groupValues[1],
                    name = m.groupValues[2],
                    version = m.groupValues[3],
                    versionCode = 0,
                    author = "",
                    description = "",
                    enabled = true,
                )
            }
        }
    }

    fun install(zipPath: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.installModule(zipPath)
            onDone(result.isSuccess, result.out.joinToString("\n") + result.err.joinToString("\n"))
            refresh()
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            RcCli.setModuleEnabled(id, enabled)
            refresh()
        }
    }

    fun remove(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            RcCli.removeModule(id)
            refresh()
        }
    }
}
