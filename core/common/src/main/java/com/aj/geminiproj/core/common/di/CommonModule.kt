package com.aj.geminiproj.core.common.di

import com.aj.geminiproj.core.common.AndroidPermissionManager
import com.aj.geminiproj.core.model.permission.PermissionManager
import org.koin.dsl.module

val commonModule = module {
    single { AndroidPermissionManager() }
    single<PermissionManager> { get() }
}