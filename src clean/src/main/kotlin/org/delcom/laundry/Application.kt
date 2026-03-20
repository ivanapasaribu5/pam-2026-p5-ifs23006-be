package org.delcom.laundry

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.datetime.Instant
import org.delcom.laundry.helpers.configureDatabases
import org.delcom.laundry.module.appModule
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    // Load .env jika ada
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }
    dotenv.entries().forEach {
        if (it.value.isNotBlank()) System.setProperty(it.key, it.value)
    }

    EngineMain.main(args)
}

fun Application.module() {

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint        = true
            ignoreUnknownKeys  = true
            explicitNulls      = false
            serializersModule  = SerializersModule {
                contextual(Instant::class, kotlinx.datetime.serializers.InstantIso8601Serializer)
            }
        })
    }

    install(Koin) {
        modules(appModule())
    }

    configureDatabases()
    configureRouting()
}
