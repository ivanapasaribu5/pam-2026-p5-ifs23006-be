package org.delcom.laundry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.ErrorResponse
import org.delcom.laundry.helpers.parseMessageToMap
import org.delcom.laundry.services.OrderService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
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
                    message = cause.message ?: "Internal server error",
                    data    = null,
                )
            )
        }
    }

    routing {

        get("/") {
            call.respondText("Laundry API berjalan! 🧺")
        }

        // ─── Orders ───────────────────────────────────────────────
        route("/orders") {

            // GET  /orders?search=xxx
            get {
                orderService.getAll(call)
            }

            // POST /orders
            post {
                orderService.post(call)
            }

            // GET  /orders/stats
            get("/stats") {
                orderService.getStats(call)
            }

            // GET  /orders/{id}
            get("/{id}") {
                orderService.getById(call)
            }

            // PUT  /orders/{id}
            put("/{id}") {
                orderService.put(call)
            }

            // PATCH /orders/{id}/status
            patch("/{id}/status") {
                orderService.patchStatus(call)
            }

            // DELETE /orders/{id}
            delete("/{id}") {
                orderService.delete(call)
            }
        }
    }
}
