package com.aj.geminiproj.tools.contacts.di

import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.tools.contacts.data.datasource.AndroidContactsDataSource
import com.aj.geminiproj.tools.contacts.data.datasource.ContactsDataSource
import com.aj.geminiproj.tools.contacts.data.repository.ContactRepositoryImpl
import com.aj.geminiproj.tools.contacts.domain.repository.ContactRepository
import com.aj.geminiproj.tools.contacts.tool.FindContactTool
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val contactsToolModule = module {
    single<ContactsDataSource> { AndroidContactsDataSource(androidContext()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single {
        FindContactTool(contactRepository = get(), permissionManager = get())
    } bind Tool::class
}