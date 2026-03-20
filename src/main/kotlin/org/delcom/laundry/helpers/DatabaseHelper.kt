package org.delcom.laundry.helpers

import io.ktor.server.application.*
import org.postgresql.util.PSQLException
import org.delcom.laundry.tables.OrderTable
import org.delcom.laundry.tables.RefreshTokenTable
import org.delcom.laundry.tables.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

fun Application.configureDatabases() {
    val dbHost     = environment.config.property("ktor.database.host").getString()
    val dbPort     = environment.config.property("ktor.database.port").getString()
    val dbName     = environment.config.property("ktor.database.name").getString()
    val dbUser     = environment.config.property("ktor.database.user").getString()
    val dbPassword = environment.config.property("ktor.database.password").getString()

    val dbUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

    try {
        Database.connect(
            url = dbUrl,
            user = dbUser,
            password = dbPassword,
        )
    } catch (e: PSQLException) {
        val isDev = (System.getProperty("io.ktor.development") ?: "false").toBoolean()

        // SQLState 3D000 = invalid_catalog_name (database does not exist)
        if (isDev && e.sqlState == "3D000") {
            val safeDbName = dbName.takeIf { it.matches(Regex("^[A-Za-z0-9_]+$")) }
                ?: throw IllegalStateException(
                    "DB_NAME tidak valid untuk auto-create database (hanya huruf/angka/_). " +
                        "Buat database manual: CREATE DATABASE \"$dbName\";",
                    e
                )

            val adminUrl = "jdbc:postgresql://$dbHost:$dbPort/postgres"
            DriverManager.getConnection(adminUrl, dbUser, dbPassword).use { conn ->
                conn.autoCommit = true // CREATE DATABASE cannot run inside a transaction
                conn.createStatement().use { stmt ->
                    try {
                        stmt.execute("CREATE DATABASE \"$safeDbName\";")
                    } catch (createErr: PSQLException) {
                        // SQLState 42P04 = duplicate_database
                        if (createErr.sqlState != "42P04") throw createErr
                    }
                }
            }

            Database.connect(
                url = dbUrl,
                user = dbUser,
                password = dbPassword,
            )
        } else {
            throw e
        }
    }

    // Auto-create tables if they don't exist
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UserTable,
            RefreshTokenTable,
            OrderTable,
        )
    }
}
