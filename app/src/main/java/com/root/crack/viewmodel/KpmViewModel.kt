package com.root.crack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.root.crack.model.KpmModuleInfo
import com.root.crack.util.RcCli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KpmViewModel : ViewModel() {
    private val _modules = MutableStateFlow<List<KpmModuleInfo>>(emptyList())
    val modules: StateFlow<List<KpmModuleInfo>> = _modules.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.listKpm()
            _modules.value = result.out.filter { it.isNotBlank() }.map { KpmModuleInfo(name = it.trim()) }
        }
    }

    fun load(path: String, args: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.loadKpm(path, args)
            onDone(result.isSuccess, (result.out + result.err).joinToString("\n"))
            refresh()
        }
    }

    fun unload(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            RcCli.unloadKpm(name)
            refresh()
        }
    }
}
