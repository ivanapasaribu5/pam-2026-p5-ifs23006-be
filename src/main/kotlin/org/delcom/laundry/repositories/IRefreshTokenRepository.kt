package org.delcom.laundry.repositories

import org.delcom.laundry.entities.RefreshToken

interface IRefreshTokenRepository {
    suspend fun getByToken(refreshToken: String, authToken: String): RefreshToken?
    suspend fun create(newRefreshToken: RefreshToken): String
    suspend fun delete(authToken: String): Boolean
    suspend fun deleteByUserId(userId: String): Boolean
}
