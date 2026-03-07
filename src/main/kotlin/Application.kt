package org.delcom

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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.configureDatabases
import org.delcom.module.appModule
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }

    dotenv.entries().forEach {
        // Only propagate non-empty values from .env so empty env/system values don't break runtime config.
        if (it.value.isNotBlank()) {
            System.setProperty(it.key, it.value)
        }
    }

    EngineMain.main(args)
}

fun Application.module() {

    val jwtSecret = environment.config
        .propertyOrNull("ktor.jwt.secret")
        ?.getString()
        ?.trim()
        .orEmpty()
        .ifBlank {
            throw IllegalStateException(
                "JWT secret is empty. Set JWT_SECRET in .env or environment variables."
            )
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
                val userId = credential.payload
                    .getClaim("userId")
                    .asString()

                if (!userId.isNullOrBlank())
                    JWTPrincipal(credential.payload)
                else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "status" to "error",
                        "message" to "Token tidak valid"
                    )
                )
            }
        }
    }

    install(CORS) {
        anyHost()
    }

    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls = false
                prettyPrint = true
                ignoreUnknownKeys = true
                serializersModule = SerializersModule {
                    contextual(Instant::class, Instant.serializer())
                }
            }
        )
    }

    install(Koin) {
        modules(appModule(jwtSecret))
    }

    configureDatabases()
    configureRouting()
}
