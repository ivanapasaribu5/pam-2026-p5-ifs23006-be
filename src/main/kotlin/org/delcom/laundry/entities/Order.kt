package org.delcom.laundry.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Order(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var customerName: String,
    var contactNumber: String,
    var serviceType: String,   // "Wash & Fold", "Dry Cleaning", "Iron Only", "Wash & Iron"
    var weightKg: Double,
    var status: String = "New",  // New, Washing, In Progress, Ready for Pickup, Completed
    var totalCost: Long,
    var pickupDate: String,
    var notes: String = "",

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
