package com.aj.geminiproj.tools.di

import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.tools.FindContactTool
import org.koin.dsl.bind
import org.koin.dsl.module

val contactsToolModule = module {
    single {
        FindContactTool(contactRepository = get(), permissionManager = get())
    } bind Tool::class
}