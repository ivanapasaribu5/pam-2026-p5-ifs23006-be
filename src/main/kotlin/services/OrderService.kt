package org.delcom.laundry.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.DataResponse
import org.delcom.laundry.data.OrderListData
import org.delcom.laundry.data.OrderRequest
import org.delcom.laundry.helpers.OrderStatuses
import org.delcom.laundry.helpers.ServiceHelper
import org.delcom.laundry.helpers.ServiceTypes
import org.delcom.laundry.helpers.ValidatorHelper
import org.delcom.laundry.repositories.IOrderRepository
import org.delcom.laundry.repositories.IUserRepository

class OrderService(
    private val userRepo: IOrderRepository,
    private val orderRepo: IOrderRepository,
    private val userRepository: IUserRepository,
) {
    constructor(orderRepo: IOrderRepository, userRepository: IUserRepository) :
        this(orderRepo, orderRepo, userRepository)

    // GET /orders?search=...
    suspend fun getAll(call: ApplicationCall) {
        val user   = ServiceHelper.getAuthUser(call, userRepository)
        val search = call.request.queryParameters["search"] ?: ""

        val orders = orderRepo.getAll(user.id, search)

        val response = DataResponse(
            status  = "success",
            message = "Berhasil mengambil daftar order",
            data = OrderListData(
                orders               = orders,
                totalOrders          = orders.size,
                newOrders            = orders.count { it.status == "New" },
                inProgressOrders     = orders.count { it.status in setOf("Washing", "In Progress") },
                readyForPickupOrders = orders.count { it.status == "Ready for Pickup" },
                completedOrders      = orders.count { it.status == "Completed" },
            )
        )
        call.respond(response)
    }

    // GET /orders/{id}
    suspend fun getById(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid")

        val user  = ServiceHelper.getAuthUser(call, userRepository)
        val order = orderRepo.getById(orderId)

        if (order == null || order.userId != user.id) {
            throw AppException(404, "Data order tidak ditemukan")
        }

        call.respond(DataResponse("success", "Berhasil mengambil data order", mapOf("order" to order)))
    }

    // POST /orders
    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepository)
        val request = call.receive<OrderRequest>()
        request.userId = user.id

        // Validasi
        val validator = ValidatorHelper(request.toMap())
        validator.required("customerName",  "Nama pelanggan tidak boleh kosong")
        validator.required("contactNumber", "Nomor kontak tidak boleh kosong")
        validator.required("serviceType",   "Jenis layanan tidak boleh kosong")
        validator.required("pickupDate",    "Tanggal pickup tidak boleh kosong")
        validator.validate()

        if (request.serviceType !in ServiceTypes.ALLOWED) {
            throw AppException(400, "Jenis layanan tidak valid. Pilih: ${ServiceTypes.ALLOWED.joinToString(", ")}")
        }
        if (request.weightKg <= 0) {
            throw AppException(400, "Berat harus lebih dari 0 kg")
        }

        // Auto-calculate cost jika tidak dikirim
        if (request.totalCost <= 0L) {
            request.totalCost = ServiceTypes.calculateCost(request.serviceType, request.weightKg)
        }

        val orderId = orderRepo.create(request.toEntity())

        call.respond(DataResponse("success", "Berhasil menambahkan order", mapOf("orderId" to orderId)))
    }

    // PUT /orders/{id}
    suspend fun put(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid")

        val user    = ServiceHelper.getAuthUser(call, userRepository)
        val request = call.receive<OrderRequest>()
        request.userId = user.id

        // Validasi
        val validator = ValidatorHelper(request.toMap())
        validator.required("customerName",  "Nama pelanggan tidak boleh kosong")
        validator.required("contactNumber", "Nomor kontak tidak boleh kosong")
        validator.required("serviceType",   "Jenis layanan tidak boleh kosong")
        validator.required("status",        "Status tidak boleh kosong")
        validator.validate()

        if (request.serviceType !in ServiceTypes.ALLOWED) {
            throw AppException(400, "Jenis layanan tidak valid")
        }
        if (request.status !in OrderStatuses.ALLOWED) {
            throw AppException(400, "Status tidak valid. Pilih: ${OrderStatuses.ALLOWED.joinToString(", ")}")
        }

        val oldOrder = orderRepo.getById(orderId)
        if (oldOrder == null || oldOrder.userId != user.id) {
            throw AppException(404, "Data order tidak ditemukan")
        }

        if (request.totalCost <= 0L) {
            request.totalCost = ServiceTypes.calculateCost(request.serviceType, request.weightKg)
        }

        val updated = orderRepo.update(user.id, orderId, request.toEntity())
        if (!updated) throw AppException(400, "Gagal memperbarui data order")

        call.respond(DataResponse("success", "Berhasil memperbarui order", null))
    }

    // DELETE /orders/{id}
    suspend fun delete(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid")

        val user  = ServiceHelper.getAuthUser(call, userRepository)
        val order = orderRepo.getById(orderId)

        if (order == null || order.userId != user.id) {
            throw AppException(404, "Data order tidak ditemukan")
        }

        val deleted = orderRepo.delete(user.id, orderId)
        if (!deleted) throw AppException(400, "Gagal menghapus data order")

        call.respond(DataResponse("success", "Berhasil menghapus order", null))
    }
}
