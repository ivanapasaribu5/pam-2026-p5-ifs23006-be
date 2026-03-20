package org.delcom.laundry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.ErrorResponse
import org.delcom.laundry.helpers.JWTConstants
import org.delcom.laundry.helpers.parseMessageToMap
import org.delcom.laundry.services.AuthService
import org.delcom.laundry.services.OrderService
import org.delcom.laundry.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService: AuthService   by inject()
    val userService: UserService   by inject()
    val orderService: OrderService by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                status  = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status  = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data    = if (dataMap.isEmpty()) null else dataMap.toString(),
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status  = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status  = "error",
                    message = cause.message ?: "Unknown error",
                    data    = "",
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Laundry App API is running. 🧺")
        }

        // Auth routes
        route("/auth") {
            post("/register")      { authService.postRegister(call) }
            post("/login")         { authService.postLogin(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout")        { authService.postLogout(call) }
        }

        authenticate(JWTConstants.NAME) {
            // Profile routes
            route("/profile") {
                get          { userService.getMe(call) }
                put          { userService.putMe(call) }
                put("/password") { userService.putMyPassword(call) }
                put("/photo")    { userService.putMyPhoto(call) }
            }

            // Order routes
            route("/orders") {
                get           { orderService.getAll(call) }
                post          { orderService.post(call) }
                get("/{id}")  { orderService.getById(call) }
                put("/{id}")  { orderService.put(call) }
                delete("/{id}") { orderService.delete(call) }
            }
        }

        // Static image routes (no auth)
        route("/images") {
            get("/users/{id}") { userService.getPhoto(call) }
        }
    }
}
