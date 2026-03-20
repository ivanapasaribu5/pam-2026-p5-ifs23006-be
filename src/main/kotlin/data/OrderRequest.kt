package org.delcom.laundry.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.laundry.entities.Order

@Serializable
data class OrderRequest(
    var userId: String = "",
    var customerName: String = "",
    var contactNumber: String = "",
    var serviceType: String = "",
    var weightKg: Double = 0.0,
    var status: String = "New",
    var totalCost: Long = 0L,
    var pickupDate: String = "",
    var notes: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"        to userId,
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
        userId        = userId,
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
