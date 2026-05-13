package com.aj.geminiproj.tools.contacts.tool

import android.Manifest
import android.util.Log
import com.aj.geminiproj.tools.contacts.domain.repository.ContactRepository
import com.aj.geminiproj.core.model.permission.PermissionManager
import com.aj.geminiproj.core.model.permission.PermissionStatus
import com.aj.geminiproj.core.model.tool.ParameterType
import com.aj.geminiproj.core.model.tool.Tool
import com.aj.geminiproj.core.model.tool.ToolDefinition
import com.aj.geminiproj.core.model.tool.ToolParameter
import com.aj.geminiproj.core.model.tool.ToolResult

class FindContactTool(
    private val contactRepository: ContactRepository,
    private val permissionManager: PermissionManager
) : Tool {

    override val definition = ToolDefinition(
        functionName = "find_contact",
        description = """
            Find a contact by name or phone number.
            Use this tool to search for a contact when user mentions a person by name and you need to retrieve their contact information to perform an action like sending a message or making a call or scheduling a meeting or send an invitation.
            Perform a partial match search if the name is not an exact match. For example, if the user mentions "John", you can return contacts like "John Doe", "Johnny Appleseed", etc.
            Returns found=false if no contact is found matching the name - still continue the task without contact information.
            Returns needs_confirmation=true if multiple contacts are found matching the name - ask user to confirm which contact they meant before proceeding with the task.
        """.trimIndent(),
        parameters = listOf(
            ToolParameter(
                name = "name",
                type = ParameterType.STRING,
                description = "The name of the contact to search for. Can be a full or partial name.",
                required = true,
            )
        ),
        displayName = "Looking up contact...",
    )

    override suspend fun execute(parameters: Map<String, Any>): ToolResult {
        Log.d("FindContactTool", "Executing find_contact with parameters: $parameters")
        parameters.forEach { (key, value) ->
            Log.d("FindContactTool", "Param: key='$key', value='$value', type=${value.javaClass.simpleName}")
        }

//       Check for contacts permission
        val permissionStatus =
            permissionManager.requirePermission(Manifest.permission.READ_CONTACTS)

        if (permissionStatus is PermissionStatus.Denied) {
            return ToolResult.PermissionDenied(
                message = "Contact access is required to look up the person's contact information. Please grant the permission and try again.",
                permission = Manifest.permission.READ_CONTACTS,

                )
        }

        // Extract name robustly
        val name = (parameters["name"] ?: parameters["Name"] ?: parameters.entries.find { it.key.equals("name", ignoreCase = true) }?.value)?.toString()
            ?: return ToolResult.Error(message = "No name is provided to search for...", isRetryable = false)

        val contacts = contactRepository.findContactsByName(name)

        return when {
            // No contact found matching the name
            contacts.isEmpty() -> ToolResult.Success(
                data = buildMap {
                    put("found", false)
                    put("searched_name", name)
                    put(
                        "message", "No contact found matching the name '$name'."
                    )
                })

            // Contact found - return contact information
            contacts.size == 1 -> {
                val contact = contacts.first()
                ToolResult.Success(
                    data = buildMap {
                        put("found", true)
                        put("displayName", contact.displayName)
                        contact.email?.let { put("email", it) }
                        contact.phone?.let { put("phone", it) }
                    }
                )
            }

            // Multiple contacts found matching the name - ask user to confirm which contact they meant
            else -> {
                val options = contacts.map { contact ->
                    buildString {
                        append(contact.displayName)
                        contact.email?.let { append(" (email: $it)") }
                            ?: contact.phone?.let { append(" (phone: $it)") }
                            ?: append(" (no email or phone)")
                    }
                }

                val contactsContext = contacts.mapIndexed { index, contact ->
                    "contact_$index" to buildMap<String, Any> {
                        put("displayName", contact.displayName)
                        contact.email?.let { put("email", it) }
                        contact.phone?.let { put("phone", it) }
                    }
                }.toMap()
                ToolResult.NeedsConfirmation(
                    message = "Multiple contacts found matching the name '$name'. Ask the user to confirm which one they mean before continuing.",
                    options = options,
                    context = contactsContext
                )
            }
        }
    }
}