package com.root.crack.util

import android.content.Context
import com.root.crack.RootCrackApp

object Prefs {
    private const val FILE = "rootcrack_prefs"
    private const val KEY_SUPERKEY = "superkey"

    private val prefs by lazy {
        RootCrackApp.instance.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    fun getSuperKey(): String? = prefs.getString(KEY_SUPERKEY, null)

    fun setSuperKey(value: String) {
        prefs.edit().putString(KEY_SUPERKEY, value).apply()
    }

    fun hasSuperKey(): Boolean = !getSuperKey().isNullOrBlank()
}
