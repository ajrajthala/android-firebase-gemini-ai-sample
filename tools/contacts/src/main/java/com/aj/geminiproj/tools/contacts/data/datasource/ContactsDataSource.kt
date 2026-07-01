package com.aj.geminiproj.tools.contacts.data.datasource

import com.aj.geminiproj.tools.contacts.domain.model.ContactInfo

interface ContactsDataSource {
    suspend fun findByName(name: String): List<ContactInfo>
}