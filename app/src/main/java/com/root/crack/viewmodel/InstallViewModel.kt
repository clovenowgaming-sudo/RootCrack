package com.root.crack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.root.crack.model.PatchStage
import com.root.crack.util.BootImageUtils
import com.root.crack.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom

class InstallViewModel(app: Application) : AndroidViewModel(app) {
    private val bootUtils = BootImageUtils(app)

    private val _stage = MutableStateFlow<PatchStage>(PatchStage.Idle)
    val stage: StateFlow<PatchStage> = _stage.asStateFlow()

    private val _log = MutableStateFlow("")
    val log: StateFlow<String> = _log.asStateFlow()

    fun generateSuperKey(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val rnd = SecureRandom()
        return (1..24).map { chars[rnd.nextInt(chars.size)] }.joinToString("")
    }

    fun patch(bootImagePath: String, superKey: String) {
        _stage.value = PatchStage.Unpacking
        _log.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            val ok = bootUtils.patch(bootImagePath, superKey) { line ->
                _log.value += line + "\n"
            }
            if (ok) {
                Prefs.setSuperKey(superKey)
                _stage.value = PatchStage.Done(bootUtils.outputImage().absolutePath)
            } else {
                _stage.value = PatchStage.Failed("Patch gagal, lihat log di bawah.")
            }
        }
    }

    fun unpatch(bootImagePath: String) {
        _stage.value = PatchStage.Unpacking
        _log.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            val ok = bootUtils.unpatch(bootImagePath) { line ->
                _log.value += line + "\n"
            }
            _stage.value = if (ok) PatchStage.Done(bootUtils.outputImage().absolutePath)
                            else PatchStage.Failed("Unpatch gagal.")
        }
    }
}
