package org.delcom.laundry.data

import kotlinx.serialization.Serializable
import org.delcom.laundry.entities.Order

@Serializable
data class OrderListData(
    val orders: List<Order>,
    val totalOrders: Int,
    val newOrders: Int,
    val inProgressOrders: Int,
    val readyForPickupOrders: Int,
    val completedOrders: Int,
)
