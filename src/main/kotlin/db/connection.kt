package net.bernerbits.sample.db

import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import kotlinx.coroutines.future.await

private fun getConnection() =
    PostgreSQLConnectionBuilder.createConnectionPool(
        "jdbc:postgresql://192.168.7.27:5432/todos"
    ) {
        username = "app_user"
        password = "correct horse battery staple"
    }

suspend fun <T> withConnection(action: suspend ConnectionPool<PostgreSQLConnection>.() -> T): T =
    with(getConnection()) {
        try {
            action()
        } finally {
            disconnect().await()
        }
    }
