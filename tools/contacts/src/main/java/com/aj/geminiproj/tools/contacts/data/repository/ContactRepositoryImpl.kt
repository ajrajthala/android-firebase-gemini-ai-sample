package com.aj.geminiproj.tools.contacts.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.aj.geminiproj.tools.contacts.data.datasource.AndroidContactsDataSource
import com.aj.geminiproj.tools.contacts.data.datasource.ContactsDataSource
import com.aj.geminiproj.tools.contacts.domain.model.ContactInfo
import com.aj.geminiproj.tools.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepositoryImpl(private val dataSource: ContactsDataSource) : ContactRepository {
    override suspend fun findContactsByName(name: String): List<ContactInfo> = dataSource.findByName(name)
}