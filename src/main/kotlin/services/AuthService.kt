package org.delcom.laundry.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.AuthRequest
import org.delcom.laundry.data.DataResponse
import org.delcom.laundry.data.RefreshTokenRequest
import org.delcom.laundry.entities.RefreshToken
import org.delcom.laundry.helpers.JWTConstants
import org.delcom.laundry.helpers.ValidatorHelper
import org.delcom.laundry.helpers.hashPassword
import org.delcom.laundry.helpers.verifyPassword
import org.delcom.laundry.repositories.IRefreshTokenRepository
import org.delcom.laundry.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
) {

    suspend fun postRegister(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name",     "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.minLength("password", 6, "Password minimal 6 karakter")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
        if (existUser != null) {
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")
        }

        request.password = hashPassword(request.password)
        val userId = userRepository.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil melakukan pendaftaran", mapOf("userId" to userId)))
    }

    suspend fun postLogin(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("username", "Username tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
            ?: throw AppException(404, "Kredensial yang digunakan tidak valid!")

        if (!verifyPassword(request.password, existUser.password)) {
            throw AppException(404, "Kredensial yang digunakan tidak valid!")
        }

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", existUser.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 hour
            .sign(Algorithm.HMAC256(jwtSecret))

        refreshTokenRepository.deleteByUserId(existUser.id)

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId       = existUser.id,
                authToken    = authToken,
                refreshToken = strRefreshToken,
            )
        )

        call.respond(
            DataResponse(
                "success",
                "Berhasil melakukan login",
                mapOf(
                    "authToken"    to authToken,
                    "refreshToken" to strRefreshToken,
                )
            )
        )
    }

    suspend fun postRefreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("refreshToken", "Refresh Token tidak boleh kosong")
        validator.required("authToken",    "Auth Token tidak boleh kosong")
        validator.validate()

        val existRefreshToken = refreshTokenRepository.getByToken(request.refreshToken, request.authToken)
            ?: throw AppException(401, "Token tidak valid!")

        val userId = existRefreshToken.userId
        val user   = userRepository.getById(userId)
            ?: throw AppException(404, "User tidak valid!")

        refreshTokenRepository.delete(request.authToken)

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId       = user.id,
                authToken    = authToken,
                refreshToken = strRefreshToken,
            )
        )

        call.respond(
            DataResponse(
                "success",
                "Berhasil melakukan refresh token",
                mapOf(
                    "authToken"    to authToken,
                    "refreshToken" to strRefreshToken,
                )
            )
        )
    }

    suspend fun postLogout(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("authToken", "Auth Token tidak boleh kosong")
        validator.validate()

        val decodedJWT = try {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(request.authToken)
        } catch (e: Exception) {
            throw AppException(401, "Token tidak valid")
        }

        val userId = decodedJWT.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")

        refreshTokenRepository.delete(request.authToken)
        refreshTokenRepository.deleteByUserId(userId)

        call.respond(DataResponse("success", "Berhasil logout", null))
    }
}
