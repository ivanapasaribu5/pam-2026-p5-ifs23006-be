package org.delcom.laundry.data

import kotlinx.serialization.Serializable
import org.delcom.laundry.entities.RefreshToken

@Serializable
data class RefreshTokenRequest(
    var userId: String = "",
    var refreshToken: String = "",
    var authToken: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "refreshToken" to refreshToken,
        "authToken"    to authToken,
    )

    fun toEntity(): RefreshToken = RefreshToken(
        userId       = userId,
        refreshToken = refreshToken,
        authToken    = authToken,
    )
}
