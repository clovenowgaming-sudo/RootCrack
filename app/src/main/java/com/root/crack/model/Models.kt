package com.root.crack.model

data class KernelInfo(
    val kernelPatchVersion: String = "unknown",
    val rootEnabled: Boolean = false,
    val superKeyValid: Boolean = false,
)

data class RcModule(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Long,
    val author: String,
    val description: String,
    val enabled: Boolean,
    val hasWebUi: Boolean = false,
)

data class KpmModuleInfo(
    val name: String,
    val version: String = "",
    val license: String = "",
    val author: String = "",
    val description: String = "",
)

data class SuGrant(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val toUid: Int = 0,
    val allowed: Boolean,
    val logAccess: Boolean = true,
    val notify: Boolean = true,
)

sealed class PatchStage {
    data object Idle : PatchStage()
    data object SelectingImage : PatchStage()
    data object Unpacking : PatchStage()
    data object Patching : PatchStage()
    data object Repacking : PatchStage()
    data class Done(val outputPath: String) : PatchStage()
    data class Failed(val message: String) : PatchStage()
}
