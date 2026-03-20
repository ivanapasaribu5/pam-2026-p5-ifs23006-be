package org.delcom.laundry.repositories

import org.delcom.laundry.entities.Order

interface IOrderRepository {
    suspend fun getAll(userId: String, search: String = ""): List<Order>
    suspend fun getById(orderId: String): Order?
    suspend fun create(order: Order): String
    suspend fun update(userId: String, orderId: String, newOrder: Order): Boolean
    suspend fun delete(userId: String, orderId: String): Boolean
}
