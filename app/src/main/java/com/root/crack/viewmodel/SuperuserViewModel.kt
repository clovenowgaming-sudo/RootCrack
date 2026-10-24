package com.root.crack.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.root.crack.model.SuGrant
import com.root.crack.util.RcCli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuperuserViewModel(app: Application) : AndroidViewModel(app) {
    private val pm: PackageManager = app.packageManager

    private val _grants = MutableStateFlow<List<SuGrant>>(emptyList())
    val grants: StateFlow<List<SuGrant>> = _grants.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RcCli.listSu()
            _grants.value = result.out.mapNotNull { line ->
                val m = Regex("""^(\d+) -> allow=(\w+) to_uid=(\d+)$""").find(line) ?: return@mapNotNull null
                val uid = m.groupValues[1].toInt()
                val pkgs = pm.getPackagesForUid(uid)
                SuGrant(
                    uid = uid,
                    packageName = pkgs?.firstOrNull() ?: "uid:$uid",
                    appName = pkgs?.firstOrNull() ?: "uid:$uid",
                    toUid = m.groupValues[3].toIntOrNull() ?: 0,
                    allowed = m.groupValues[2].toBoolean(),
                )
            }
        }
    }

    fun grant(uid: Int) {
        viewModelScope.launch(Dispatchers.IO) { RcCli.grantSu(uid); refresh() }
    }

    fun revoke(uid: Int) {
        viewModelScope.launch(Dispatchers.IO) { RcCli.revokeSu(uid); refresh() }
    }
}
