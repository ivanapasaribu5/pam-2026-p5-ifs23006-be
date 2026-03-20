package org.delcom.laundry.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrderTable : UUIDTable("orders") {
    val userId      = uuid("user_id")
    val customerName = varchar("customer_name", 150)
    val contactNumber = varchar("contact_number", 20)
    val serviceType = varchar("service_type", 50)
    val weightKg    = double("weight_kg")
    val status      = varchar("status", 30).default("New")
    val totalCost   = long("total_cost")
    val pickupDate  = varchar("pickup_date", 20)
    val notes       = text("notes").default("")
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}
