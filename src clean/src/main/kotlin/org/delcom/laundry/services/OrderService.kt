package org.delcom.laundry.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.laundry.data.AppException
import org.delcom.laundry.data.DataResponse
import org.delcom.laundry.data.OrderListData
import org.delcom.laundry.data.OrderRequest
import org.delcom.laundry.helpers.ValidatorHelper
import org.delcom.laundry.repositories.IOrderRepository

class OrderService(
    private val orderRepo: IOrderRepository
) {
    private val allowedServiceTypes = setOf("Wash & Fold", "Dry Cleaning", "Iron Only", "Wash & Iron")
    private val allowedStatuses = setOf("New", "Washing", "In Progress", "Ready for Pickup", "Completed")

    // Harga per kg dalam Rupiah
    private val pricePerKg = mapOf(
        "Wash & Fold"  to 15_000L,
        "Dry Cleaning" to 20_000L,
        "Iron Only"    to 8_000L,
        "Wash & Iron"  to 18_000L,
    )

    private fun calculateCost(serviceType: String, weightKg: Double): Long {
        val price = pricePerKg[serviceType] ?: 15_000L
        return (price * weightKg).toLong()
    }

    private fun validateServiceType(serviceType: String) {
        if (serviceType.isNotBlank() && serviceType !in allowedServiceTypes) {
            throw AppException(400, "Tipe layanan harus salah satu dari: ${allowedServiceTypes.joinToString(", ")}")
        }
    }

    private fun validateStatus(status: String) {
        if (status.isNotBlank() && status !in allowedStatuses) {
            throw AppException(400, "Status harus salah satu dari: ${allowedStatuses.joinToString(", ")}")
        }
    }

    // GET /orders — daftar semua order
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val orders = orderRepo.getAll(search)

        val response = DataResponse(
            status  = "success",
            message = "Berhasil mengambil daftar order",
            data    = OrderListData(
                orders               = orders,
                totalOrders          = orders.size,
                newOrders            = orders.count { it.status == "New" },
                inProgressOrders     = orders.count { it.status == "In Progress" || it.status == "Washing" },
                readyForPickupOrders = orders.count { it.status == "Ready for Pickup" },
                completedOrders      = orders.count { it.status == "Completed" },
            )
        )
        call.respond(response)
    }

    // GET /orders/{id}
    suspend fun getById(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid!")

        val order = orderRepo.getById(orderId)
            ?: throw AppException(404, "Data order tidak ditemukan!")

        call.respond(DataResponse("success", "Berhasil mengambil data order", mapOf("order" to order)))
    }

    // POST /orders — tambah order baru
    suspend fun post(call: ApplicationCall) {
        val request = call.receive<OrderRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("customerName",  "Nama pelanggan tidak boleh kosong")
        validator.required("contactNumber", "Nomor kontak tidak boleh kosong")
        validator.required("serviceType",   "Tipe layanan tidak boleh kosong")
        validator.required("pickupDate",    "Tanggal pickup tidak boleh kosong")
        validator.validate()

        validateServiceType(request.serviceType)
        validateStatus(request.status)

        if (request.weightKg <= 0) {
            throw AppException(400, "Berat cucian harus lebih dari 0 kg")
        }

        // Auto-hitung biaya jika belum diisi
        if (request.totalCost == 0L) {
            request.totalCost = calculateCost(request.serviceType, request.weightKg)
        }

        val orderId = orderRepo.create(request.toEntity())

        call.respond(
            DataResponse("success", "Berhasil menambahkan order", mapOf("orderId" to orderId))
        )
    }

    // PUT /orders/{id} — update order
    suspend fun put(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid!")

        val existing = orderRepo.getById(orderId)
            ?: throw AppException(404, "Data order tidak ditemukan!")

        val request = call.receive<OrderRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("customerName",  "Nama pelanggan tidak boleh kosong")
        validator.required("contactNumber", "Nomor kontak tidak boleh kosong")
        validator.required("serviceType",   "Tipe layanan tidak boleh kosong")
        validator.required("pickupDate",    "Tanggal pickup tidak boleh kosong")
        validator.validate()

        validateServiceType(request.serviceType)
        validateStatus(request.status)

        if (request.weightKg <= 0) {
            throw AppException(400, "Berat cucian harus lebih dari 0 kg")
        }

        // Recalculate cost jika berat atau service berubah
        request.totalCost = calculateCost(request.serviceType, request.weightKg)

        val isUpdated = orderRepo.update(orderId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data order!")
        }

        call.respond(DataResponse("success", "Berhasil memperbarui order", null))
    }

    // PATCH /orders/{id}/status — update status saja
    suspend fun patchStatus(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid!")

        val existing = orderRepo.getById(orderId)
            ?: throw AppException(404, "Data order tidak ditemukan!")

        val body = call.receive<Map<String, String>>()
        val newStatus = body["status"]
            ?: throw AppException(400, "Status tidak boleh kosong")

        validateStatus(newStatus)

        val updated = existing.copy(status = newStatus)
        val isUpdated = orderRepo.update(orderId, updated)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui status order!")
        }

        call.respond(DataResponse("success", "Status order berhasil diperbarui", mapOf("status" to newStatus)))
    }

    // DELETE /orders/{id}
    suspend fun delete(call: ApplicationCall) {
        val orderId = call.parameters["id"]
            ?: throw AppException(400, "ID order tidak valid!")

        orderRepo.getById(orderId)
            ?: throw AppException(404, "Data order tidak ditemukan!")

        val isDeleted = orderRepo.delete(orderId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data order!")
        }

        call.respond(DataResponse("success", "Berhasil menghapus order", null))
    }

    // GET /orders/stats — statistik dashboard
    suspend fun getStats(call: ApplicationCall) {
        val orders = orderRepo.getAll()
        val stats = mapOf(
            "totalOrders"          to orders.size,
            "newOrders"            to orders.count { it.status == "New" },
            "inProgressOrders"     to orders.count { it.status == "In Progress" || it.status == "Washing" },
            "readyForPickupOrders" to orders.count { it.status == "Ready for Pickup" },
            "completedOrders"      to orders.count { it.status == "Completed" },
            "totalRevenue"         to orders.filter { it.status == "Completed" }.sumOf { it.totalCost },
        )
        call.respond(DataResponse("success", "Berhasil mengambil statistik", stats))
    }
}
