package com.aj.geminiproj.core.model.permission

sealed class PermissionStatus {
    object Granted : PermissionStatus()
    data class Denied(val permission: String, val isPermanent: Boolean = false) : PermissionStatus()
    data class Requesting(val permission: String) : PermissionStatus()
}