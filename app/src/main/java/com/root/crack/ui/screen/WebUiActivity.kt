package com.root.crack.ui.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.root.crack.ui.theme.RootCrackTheme
import java.io.File
import java.io.FileInputStream

/**
 * Hosts a module's bundled `webroot/` folder (served from the module's own
 * directory under /data/adb/rootcrack/modules/<id>/webroot) in an isolated
 * WebView — this is how modules ship a settings UI without needing their
 * own launcher activity.
 */
class WebUiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val moduleId = intent.getStringExtra(EXTRA_MODULE_ID) ?: run { finish(); return }
        val webRoot = File("/data/adb/rootcrack/modules/$moduleId/webroot")

        setContent {
            RootCrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ModuleWebView(webRoot)
                }
            }
        }
    }

    companion object {
        const val EXTRA_MODULE_ID = "module_id"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ModuleWebView(webRoot: File) {
    AndroidView(factory = { ctx ->
        WebView(ctx).apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse? {
                    val path = request.url.path ?: return null
                    val file = File(webRoot, path.removePrefix("/"))
                    if (!file.canonicalPath.startsWith(webRoot.canonicalPath)) return null
                    if (!file.exists()) return null
                    val mime = when (file.extension) {
                        "html" -> "text/html"
                        "js" -> "application/javascript"
                        "css" -> "text/css"
                        "png" -> "image/png"
                        "json" -> "application/json"
                        else -> "application/octet-stream"
                    }
                    return WebResourceResponse(mime, "utf-8", FileInputStream(file))
                }
            }
            loadUrl("https://appassets.androidplatform.net/index.html")
        }
    })
}
