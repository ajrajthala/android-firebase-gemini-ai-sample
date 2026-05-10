package com.aj.geminiproj.core.data.contacts

import android.content.Context
import android.provider.ContactsContract
import com.aj.geminiproj.core.model.contacts.ContactInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    override suspend fun findContactsByName(name: String): List<ContactInfo> =
        withContext(Dispatchers.IO) {
            val contactIds = resolveAllContactIds(name)

            contactIds.map { contactId ->
                val email = resolveEmail(contactId)
                val phone = resolvePhone(contactId)
                val displayName = resolveDisplayName(contactId) ?: name

                ContactInfo(
                    displayName = displayName,
                    email = email,
                    phone = phone
                )
            }
        }

    private fun resolveAllContactIds(name: String): List<String> {
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts._ID)
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        val selectionArgs = arrayOf("%$name%")
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        val ids = mutableListOf<String>()
        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                    )
                )
            }
        }
        return ids
    }

    private fun resolveContactId(name: String): String? {
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts._ID)
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        val selectionArgs = arrayOf("%$name%")
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                )
            } else null
        }
    }

    /**
     * Resolves the full display name for a contact ID.
     */
    private fun resolveDisplayName(contactId: String): String? {
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val selection = "${ContactsContract.Contacts._ID} = ?"
        val selectionArgs = arrayOf(contactId)

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                    )
                )
            } else null
        }
    }

    /**
     * Resolves the primary email for a contact ID.
     * Returns null if no email is stored.
     */
    private fun resolveEmail(contactId: String): String? {
        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Email.ADDRESS
                    )
                )
            } else null
        }
    }

    /**
     * Resolves the primary phone number for a contact ID.
     * Returns null if no phone is stored.
     */
    private fun resolvePhone(contactId: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
            } else null
        }
    }
}