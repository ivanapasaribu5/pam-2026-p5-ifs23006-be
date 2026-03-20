package org.delcom.laundry.repositories

import org.delcom.laundry.dao.UserDAO
import org.delcom.laundry.entities.User
import org.delcom.laundry.helpers.suspendTransaction
import org.delcom.laundry.helpers.userDAOToModel
import org.delcom.laundry.tables.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class UserRepository : IUserRepository {

    override suspend fun getById(userId: String): User? = suspendTransaction {
        UserDAO.find { UserTable.id eq UUID.fromString(userId) }
            .limit(1).map(::userDAOToModel).firstOrNull()
    }

    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDAO.find { UserTable.username eq username }
            .limit(1).map(::userDAOToModel).firstOrNull()
    }

    override suspend fun create(user: User): String = suspendTransaction {
        val dao = UserDAO.new {
            name      = user.name
            username  = user.username
            password  = user.password
            photo     = user.photo
            about     = user.about
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, newUser: User): Boolean = suspendTransaction {
        val dao = UserDAO.find { UserTable.id eq UUID.fromString(userId) }
            .limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.name      = newUser.name
        dao.username  = newUser.username
        dao.password  = newUser.password
        dao.about     = newUser.about
        dao.updatedAt = newUser.updatedAt
        true
    }

    override suspend fun updatePhoto(userId: String, photo: String): Boolean = suspendTransaction {
        val dao = UserDAO.find { UserTable.id eq UUID.fromString(userId) }
            .limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.photo = photo
        true
    }
}
