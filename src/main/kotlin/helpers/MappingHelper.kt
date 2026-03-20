package org.delcom.laundry.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.laundry.dao.OrderDAO
import org.delcom.laundry.dao.RefreshTokenDAO
import org.delcom.laundry.dao.UserDAO
import org.delcom.laundry.entities.Order
import org.delcom.laundry.entities.RefreshToken
import org.delcom.laundry.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    id        = dao.id.value.toString(),
    name      = dao.name,
    username  = dao.username,
    password  = dao.password,
    photo     = dao.photo,
    about     = dao.about,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt,
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id           = dao.id.value.toString(),
    userId       = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken    = dao.authToken,
    createdAt    = dao.createdAt,
)

fun orderDAOToModel(dao: OrderDAO) = Order(
    id            = dao.id.value.toString(),
    userId        = dao.userId.toString(),
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
