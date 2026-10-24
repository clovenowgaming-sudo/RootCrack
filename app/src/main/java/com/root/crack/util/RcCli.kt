package com.root.crack.util

import com.topjohnwu.superuser.Shell

/**
 * Thin wrapper over libsu that shells out to the rfd daemon binary. The
 * daemon (librfd.so, installed under the app's native lib dir) does the
 * actual privileged work; this class just builds argv and reads stdout.
 */
object RcCli {
    private val daemonPath: String
        get() = "${com.root.crack.RootCrackApp.instance.applicationInfo.nativeLibraryDir}/librfd.so"

    private fun superKey(): String =
        Prefs.getSuperKey() ?: error("SuperKey not configured")

    fun exec(vararg args: String): Shell.Result {
        val cmd = mutableListOf(daemonPath, "--key", superKey())
        cmd.addAll(args)
        return Shell.cmd(cmd.joinToString(" ") { if (it.contains(" ")) "'$it'" else it }).exec()
    }

    fun status(): Shell.Result = exec("status")
    fun listModules(): Shell.Result = exec("module", "list")
    fun installModule(zipPath: String): Shell.Result = exec("module", "install", zipPath)
    fun setModuleEnabled(id: String, enabled: Boolean): Shell.Result =
        exec("module", if (enabled) "enable" else "disable", id)
    fun removeModule(id: String): Shell.Result = exec("module", "remove", id)

    fun listKpm(): Shell.Result = exec("kpm", "list")
    fun loadKpm(path: String, args: String = ""): Shell.Result = exec("kpm", "load", path, args)
    fun unloadKpm(name: String): Shell.Result = exec("kpm", "unload", name)

    fun grantSu(uid: Int, toUid: Int = 0): Shell.Result =
        exec("su", "grant", uid.toString(), toUid.toString())
    fun revokeSu(uid: Int): Shell.Result = exec("su", "revoke", uid.toString())
    fun listSu(): Shell.Result = exec("su", "list")
}
