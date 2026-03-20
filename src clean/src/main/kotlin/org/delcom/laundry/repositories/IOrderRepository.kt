package org.delcom.laundry.repositories

import org.delcom.laundry.entities.Order

interface IOrderRepository {
    suspend fun getAll(search: String = ""): List<Order>
    suspend fun getById(orderId: String): Order?
    suspend fun create(order: Order): String
    suspend fun update(orderId: String, newOrder: Order): Boolean
    suspend fun delete(orderId: String): Boolean
}
