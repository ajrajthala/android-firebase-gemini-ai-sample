package com.aj.geminiproj.core.model.permission

interface PermissionManager {
    fun isGranted(permission: String): Boolean
    suspend fun requirePermission(permission: String): PermissionStatus
}