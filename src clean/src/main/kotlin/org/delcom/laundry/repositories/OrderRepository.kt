package org.delcom.laundry.repositories

import org.delcom.laundry.dao.OrderDAO
import org.delcom.laundry.entities.Order
import org.delcom.laundry.helpers.orderDAOToModel
import org.delcom.laundry.helpers.suspendTransaction
import org.delcom.laundry.tables.OrderTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class OrderRepository : IOrderRepository {

    override suspend fun getAll(search: String): List<Order> = suspendTransaction {
        if (search.isBlank()) {
            OrderDAO
                .all()
                .orderBy(OrderTable.createdAt to SortOrder.DESC)
                .map(::orderDAOToModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            OrderDAO
                .find {
                    OrderTable.customerName.lowerCase() like keyword
                }
                .orderBy(OrderTable.createdAt to SortOrder.DESC)
                .map(::orderDAOToModel)
        }
    }

    override suspend fun getById(orderId: String): Order? = suspendTransaction {
        OrderDAO
            .find { OrderTable.id eq UUID.fromString(orderId) }
            .limit(1)
            .map(::orderDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(order: Order): String = suspendTransaction {
        val dao = OrderDAO.new {
            customerName  = order.customerName
            contactNumber = order.contactNumber
            serviceType   = order.serviceType
            weightKg      = order.weightKg
            status        = order.status
            totalCost     = order.totalCost
            pickupDate    = order.pickupDate
            notes         = order.notes
            createdAt     = order.createdAt
            updatedAt     = order.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(orderId: String, newOrder: Order): Boolean = suspendTransaction {
        val dao = OrderDAO
            .find { OrderTable.id eq UUID.fromString(orderId) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.customerName  = newOrder.customerName
            dao.contactNumber = newOrder.contactNumber
            dao.serviceType   = newOrder.serviceType
            dao.weightKg      = newOrder.weightKg
            dao.status        = newOrder.status
            dao.totalCost     = newOrder.totalCost
            dao.pickupDate    = newOrder.pickupDate
            dao.notes         = newOrder.notes
            dao.updatedAt     = newOrder.updatedAt
            true
        } else false
    }

    override suspend fun delete(orderId: String): Boolean = suspendTransaction {
        val rows = OrderTable.deleteWhere {
            OrderTable.id eq UUID.fromString(orderId)
        }
        rows >= 1
    }
}
