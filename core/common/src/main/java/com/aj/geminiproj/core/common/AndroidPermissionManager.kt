package com.aj.geminiproj.core.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.core.model.permission.PermissionStatus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidPermissionManager : PermissionManager {
    private var launcher: ActivityResultLauncherWrapper? = null
    private var appContext: Context? = null
    private var pendingContinuation: ((PermissionStatus) -> Unit)? = null
    private var pendingPermission: String? = null

    override fun isGranted(permission: String): Boolean {
        val context = appContext ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requirePermission(permission: String): PermissionStatus {
        if (isGranted(permission)) return PermissionStatus.Granted

        val activeLauncher =
            launcher ?: return PermissionStatus.Denied(permission = permission, isPermanent = false)

        return suspendCancellableCoroutine { continuation ->
            pendingPermission = permission
            pendingContinuation = { status ->
                pendingPermission = null
                pendingContinuation = null
                continuation.resume(status)
            }

            continuation.invokeOnCancellation {
                pendingPermission = null
                pendingContinuation = null
            }
            activeLauncher.launch(permission)
        }
    }

    fun registerLauncher(launcher: ActivityResultLauncherWrapper, context: Context) {
        this.launcher = launcher
        this.appContext = context
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        val permission = pendingPermission ?: return
        val continuation = pendingContinuation ?: return
        continuation(
            if (isGranted) PermissionStatus.Granted
            else PermissionStatus.Denied(permission = permission)
        )
    }
}