package com.akshayashokcode.mediakitcore.launcher

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Generic permission request launcher shared across MediaKit modules.
 *
 * Registers with [ActivityResultCaller] eagerly. Must be constructed before
 * the host Activity/Fragment reaches STARTED.
 */
class PermissionLauncher(
    caller: ActivityResultCaller,
    private val onResult: (allGranted: Boolean) -> Unit
) {
    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        onResult(permissions.values.all { it })
    }

    fun launch(permissions: Array<String>) = launcher.launch(permissions)

    companion object {
        fun isGranted(context: Context, permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        fun areGranted(context: Context, vararg permissions: String): Boolean =
            permissions.all { isGranted(context, it) }
    }
}
