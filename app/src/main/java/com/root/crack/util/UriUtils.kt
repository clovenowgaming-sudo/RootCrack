package com.root.crack.util

import android.content.Context
import android.net.Uri
import java.io.File

/** Copies a content:// picker result into the app cache so shell/daemon code can read it by path. */
fun copyUriToCache(context: Context, uri: Uri, fileName: String): String {
    val outFile = File(context.cacheDir, fileName)
    context.contentResolver.openInputStream(uri)?.use { input ->
        outFile.outputStream().use { input.copyTo(it) }
    }
    return outFile.absolutePath
}
