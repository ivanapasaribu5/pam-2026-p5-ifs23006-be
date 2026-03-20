package org.delcom.laundry.data

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val username: String,
    val photo: String?,
    val about: String?,
)
