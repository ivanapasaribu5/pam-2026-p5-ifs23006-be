package org.delcom.laundry.data

import kotlinx.serialization.Serializable
import org.delcom.laundry.entities.User

@Serializable
data class AuthRequest(
    var name: String = "",
    var username: String = "",
    var password: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name"     to name,
        "username" to username,
        "password" to password,
    )

    fun toEntity(): User = User(
        name     = name,
        username = username,
        password = password,
    )
}
