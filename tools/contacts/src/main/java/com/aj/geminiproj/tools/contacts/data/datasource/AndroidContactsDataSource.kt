package com.aj.geminiproj.tools.contacts.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.aj.geminiproj.tools.contacts.domain.model.ContactInfo

class AndroidContactsDataSource(private val context: Context) : ContactsDataSource {
    private fun resolveAllContactIds(name: String): List<String> {
        val filterUri: Uri =
            Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(name))
        val projection =
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
//        val uri = ContactsContract.Contacts.CONTENT_URI
//        val projection = arrayOf(ContactsContract.Contacts._ID)
//        val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
//        val selectionArgs = arrayOf("%$name%")
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        val ids = mutableListOf<String>()
        context.contentResolver.query(
            filterUri,
            projection,
            null,
            null,
            sortOrder,
        )?.use { cursor ->
            Log.e("Contacts found", "Cursor count: ${cursor.count}")
            val nameIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (cursor.moveToNext()) {
                Log.e("name ", cursor.getString(nameIndex))
                ids.add(
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                    )
                )
            }
        }
        return ids
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

    override suspend fun findByName(name: String): List<ContactInfo> =
        resolveAllContactIds(name).map { contactId ->
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