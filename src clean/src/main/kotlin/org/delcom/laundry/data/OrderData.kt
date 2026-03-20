package org.delcom.laundry.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.laundry.entities.Order

@Serializable
data class OrderRequest(
    var customerName: String  = "",
    var contactNumber: String = "",
    var serviceType: String   = "",
    var weightKg: Double      = 0.0,
    var status: String        = "New",
    var totalCost: Long       = 0L,
    var pickupDate: String    = "",
    var notes: String         = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "customerName"  to customerName,
        "contactNumber" to contactNumber,
        "serviceType"   to serviceType,
        "weightKg"      to weightKg,
        "status"        to status,
        "totalCost"     to totalCost,
        "pickupDate"    to pickupDate,
        "notes"         to notes,
    )

    fun toEntity(): Order = Order(
        customerName  = customerName,
        contactNumber = contactNumber,
        serviceType   = serviceType,
        weightKg      = weightKg,
        status        = status,
        totalCost     = totalCost,
        pickupDate    = pickupDate,
        notes         = notes,
        updatedAt     = Clock.System.now(),
    )
}

@Serializable
data class OrderListData(
    val orders: List<org.delcom.laundry.entities.Order>,
    val totalOrders: Int,
    val newOrders: Int,
    val inProgressOrders: Int,
    val readyForPickupOrders: Int,
    val completedOrders: Int,
)

@Serializable
data class DataResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null,
)

@Serializable
data class ErrorResponse(
    val status: String,
    val message: String,
    val data: String? = null,
)

@Serializable
data class AppException(val code: Int, override val message: String) : Exception(message)
