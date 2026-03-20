package org.delcom.laundry.repositories

import org.delcom.laundry.dao.RefreshTokenDAO
import org.delcom.laundry.entities.RefreshToken
import org.delcom.laundry.helpers.refreshTokenDAOToModel
import org.delcom.laundry.helpers.suspendTransaction
import org.delcom.laundry.tables.RefreshTokenTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class RefreshTokenRepository : IRefreshTokenRepository {

    override suspend fun getByToken(refreshToken: String, authToken: String): RefreshToken? = suspendTransaction {
        RefreshTokenDAO
            .find { (RefreshTokenTable.refreshToken eq refreshToken) and (RefreshTokenTable.authToken eq authToken) }
            .limit(1).map(::refreshTokenDAOToModel).firstOrNull()
    }

    override suspend fun create(newRefreshToken: RefreshToken): String = suspendTransaction {
        val dao = RefreshTokenDAO.new {
            userId           = UUID.fromString(newRefreshToken.userId)
            this.refreshToken = newRefreshToken.refreshToken
            authToken        = newRefreshToken.authToken
            createdAt        = newRefreshToken.createdAt
        }
        dao.id.value.toString()
    }

    override suspend fun delete(authToken: String): Boolean = suspendTransaction {
        val rows = RefreshTokenTable.deleteWhere { RefreshTokenTable.authToken eq authToken }
        rows >= 1
    }

    override suspend fun deleteByUserId(userId: String): Boolean = suspendTransaction {
        val rows = RefreshTokenTable.deleteWhere { RefreshTokenTable.userId eq UUID.fromString(userId) }
        rows >= 1
    }
}
