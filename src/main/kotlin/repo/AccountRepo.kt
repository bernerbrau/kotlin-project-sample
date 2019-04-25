package net.bernerbits.sample.repo

import com.github.jasync.sql.db.RowData
import kotlinx.coroutines.future.await
import net.bernerbits.sample.db.useConnection
import net.bernerbits.sample.model.Account

object AccountRepo {
    suspend fun login(login: String, password: String): Account? =
        useConnection {
            val result = sendPreparedStatement("select * from accounts where login = ? and pwd_hash = crypt(?, pwd_hash)", listOf(login, password)).await()
            result.rows
                .asSequence()
                .map(::toAccount)
                .singleOrNull()
        }

    private fun toAccount(rowData: RowData) =
        Account(
            id=         rowData.getAs("id"),
            login=      rowData.getAs("login"),
            displayName=rowData.getAs("display_name")
        )

}
