package org.delcom.laundry.repositories

import org.delcom.laundry.entities.User

interface IUserRepository {
    suspend fun getById(userId: String): User?
    suspend fun getByUsername(username: String): User?
    suspend fun create(user: User): String
    suspend fun update(userId: String, newUser: User): Boolean
    suspend fun updatePhoto(userId: String, photo: String): Boolean
}
