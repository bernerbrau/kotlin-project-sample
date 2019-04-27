package net.bernerbits.sample.repo

import com.github.jasync.sql.db.RowData
import kotlinx.coroutines.future.await
import net.bernerbits.sample.db.withConnection
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.model.AccountFields

object AccountRepo {
    suspend fun login(login: String, password: String): Account? =
        withConnection {
            val result = sendPreparedStatement("select * from accounts where login = ? and pwd_hash = crypt(?, pwd_hash)", listOf(login, password)).await()
            result.rows
                .asSequence()
                .map(::toAccount)
                .singleOrNull()
        }

    private fun toAccount(rowData: RowData) =
        Account(
            id=    rowData.getAs("id"),
            fields=AccountFields(
                login=      rowData.getAs("login"),
                displayName=rowData.getAs("display_name")
            )
        )

}
