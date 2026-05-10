package com.aj.geminiproj.core.data.contacts

import com.aj.geminiproj.core.model.contacts.ContactInfo

interface ContactRepository {
    suspend fun findContactByName(name: String): ContactInfo?
}