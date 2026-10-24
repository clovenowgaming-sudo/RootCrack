package com.root.crack.service

import android.content.Intent
import android.os.IBinder
import com.root.crack.IRootCrackService
import com.topjohnwu.superuser.ipc.RootService

/**
 * Runs as root (via libsu RootService) so the app can keep a long-lived
 * privileged channel open to rfd, instead of spawning a fresh su shell for
 * every single call.
 */
class RootCrackRootService : RootService() {
    private val binder = object : IRootCrackService.Stub() {
        override fun runDaemon(args: Array<String>): String {
            val process = ProcessBuilder(args.toList()).redirectErrorStream(true).start()
            return process.inputStream.bufferedReader().readText()
        }

        override fun isDaemonAlive(): Boolean = true
    }

    override fun onBind(intent: Intent): IBinder = binder
}
