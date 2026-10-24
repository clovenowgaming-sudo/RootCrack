package com.root.crack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.root.crack.model.KernelInfo
import com.root.crack.util.Prefs
import com.root.crack.util.RcCli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(KernelInfo())
    val state: StateFlow<KernelInfo> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (!Prefs.hasSuperKey()) {
            _state.value = KernelInfo(superKeyValid = false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.status()
            if (!result.isSuccess) {
                _state.value = KernelInfo(superKeyValid = false)
                return@launch
            }
            val out = result.out.joinToString("\n")
            val version = Regex("KernelPatch : (.+)").find(out)?.groupValues?.get(1) ?: "unknown"
            val rootEnabled = out.contains("root enabled: true")
            _state.value = KernelInfo(
                kernelPatchVersion = version,
                rootEnabled = rootEnabled,
                superKeyValid = true,
            )
        }
    }
}
