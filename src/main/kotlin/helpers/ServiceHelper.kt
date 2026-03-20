package org.delcom.laundry.helpers

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.entities.User
import org.delcom.laundry.repositories.IUserRepository

object ServiceHelper {
    suspend fun getAuthUser(call: ApplicationCall, userRepository: IUserRepository): User {
        val principal = call.principal<JWTPrincipal>()
            ?: throw AppException(401, "Unauthorized")

        val userId = principal.payload.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")

        return userRepository.getById(userId)
            ?: throw AppException(401, "User tidak valid")
    }
}
