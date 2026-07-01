package com.aj.geminiproj.tools.contacts.domain.repository

import com.aj.geminiproj.tools.contacts.domain.model.ContactInfo

interface ContactRepository {
    suspend fun findContactsByName(name: String): List<ContactInfo>
}