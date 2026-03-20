package org.delcom.laundry.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.laundry.dao.OrderDAO
import org.delcom.laundry.entities.Order
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// ───────────── Transaction helper ─────────────

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

// ───────────── DAO → Model mapping ─────────────

fun orderDAOToModel(dao: OrderDAO) = Order(
    id            = dao.id.value.toString(),
    customerName  = dao.customerName,
    contactNumber = dao.contactNumber,
    serviceType   = dao.serviceType,
    weightKg      = dao.weightKg,
    status        = dao.status,
    totalCost     = dao.totalCost,
    pickupDate    = dao.pickupDate,
    notes         = dao.notes,
    createdAt     = dao.createdAt,
    updatedAt     = dao.updatedAt,
)
