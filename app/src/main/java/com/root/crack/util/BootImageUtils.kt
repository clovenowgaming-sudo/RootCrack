package com.root.crack.util

import android.content.Context
import com.topjohnwu.superuser.Shell
import java.io.File

/**
 * Drives boot_patch.sh / boot_unpatch.sh (bundled in assets) against a
 * selected boot/init_boot image, using the kptools/kpimg binaries also
 * shipped in assets. This class only orchestrates files + the shell
 * session; the actual image patching logic lives in kptools (native,
 * upstream) and is invoked as a subprocess, never reimplemented here.
 */
class BootImageUtils(private val context: Context) {

    private val workDir: File get() = File(context.filesDir, "patch_work").apply { mkdirs() }

    private fun extractAsset(name: String, dest: File) {
        context.assets.open(name).use { input ->
            dest.outputStream().use { input.copyTo(it) }
        }
        dest.setExecutable(true)
    }

    fun prepareWorkDir(): File {
        val dir = workDir
        for (asset in listOf("kptools", "kpimg", "boot_patch.sh", "boot_unpatch.sh", "util_functions.sh")) {
            extractAsset(asset, File(dir, asset))
        }
        return dir
    }

    fun patch(bootImagePath: String, superKey: String, onLine: (String) -> Unit): Boolean {
        val dir = prepareWorkDir()
        val cb = object : com.topjohnwu.superuser.CallbackList<String>() {
            override fun onAddElement(e: String?) { e?.let(onLine) }
        }
        val result = Shell.cmd(
            "cd '${dir.absolutePath}' && sh boot_patch.sh '$superKey' '$bootImagePath'"
        ).to(cb, cb).exec()
        return result.isSuccess
    }

    fun unpatch(bootImagePath: String, onLine: (String) -> Unit): Boolean {
        val dir = prepareWorkDir()
        val cb = object : com.topjohnwu.superuser.CallbackList<String>() {
            override fun onAddElement(e: String?) { e?.let(onLine) }
        }
        val result = Shell.cmd(
            "cd '${dir.absolutePath}' && sh boot_unpatch.sh '$bootImagePath'"
        ).to(cb, cb).exec()
        return result.isSuccess
    }

    fun outputImage(): File = File(workDir, "new-boot.img")
}
