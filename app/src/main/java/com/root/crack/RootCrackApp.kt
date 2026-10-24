package com.root.crack

import android.app.Application
import com.topjohnwu.superuser.Shell

class RootCrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR)
        )
    }

    companion object {
        lateinit var instance: RootCrackApp
            private set
    }

    init {
        instance = this
    }
}
