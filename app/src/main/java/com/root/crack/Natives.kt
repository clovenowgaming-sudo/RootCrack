package com.root.crack

/**
 * JNI surface for native_bridge.cpp, which issues the kpimg supercall
 * syscall on our behalf. All calls require the SuperKey established when
 * the boot image was patched.
 */
object Natives {
    init {
        System.loadLibrary("rootcrack_native")
    }

    external fun nativeHello(key: String): String
    external fun nativeKernelPatchVersion(key: String): String
    external fun nativeSuEnabled(key: String): Boolean
    external fun nativeKpmNums(key: String): Int
}
