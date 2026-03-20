package org.delcom.laundry.dao

import org.delcom.laundry.tables.OrderTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class OrderDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, OrderDAO>(OrderTable)

    var userId        by OrderTable.userId
    var customerName  by OrderTable.customerName
    var contactNumber by OrderTable.contactNumber
    var serviceType   by OrderTable.serviceType
    var weightKg      by OrderTable.weightKg
    var status        by OrderTable.status
    var totalCost     by OrderTable.totalCost
    var pickupDate    by OrderTable.pickupDate
    var notes         by OrderTable.notes
    var createdAt     by OrderTable.createdAt
    var updatedAt     by OrderTable.updatedAt
}
