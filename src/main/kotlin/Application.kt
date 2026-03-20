package org.delcom.laundry

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.delcom.laundry.helpers.JWTConstants
import org.delcom.laundry.helpers.configureDatabases
import org.delcom.laundry.module.appModule
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }

    dotenv.entries().forEach {
        if (it.value.isNotBlank()) {
            System.setProperty(it.key, it.value)
        }
    }

    EngineMain.main(args)
}

fun Application.laundryModule() {

    val jwtSecret = environment.config
        .propertyOrNull("ktor.jwt.secret")
        ?.getString()
        ?.trim()
        .orEmpty()
        .ifBlank {
            throw IllegalStateException("JWT secret kosong. Set JWT_SECRET di .env atau environment variables.")
        }

    install(Authentication) {
        jwt(JWTConstants.NAME) {
            realm = JWTConstants.REALM

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWTConstants.ISSUER)
                    .withAudience(JWTConstants.AUDIENCE)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "Token tidak valid atau sudah expired")
                )
            }
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls    = false
                prettyPrint      = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(Koin) {
        modules(appModule(jwtSecret))
    }

    configureDatabases()
    configureRouting()
}
