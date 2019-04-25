package net.bernerbits.sample.repo

import com.github.jasync.sql.db.RowData
import kotlinx.coroutines.future.await
import net.bernerbits.sample.db.useConnection
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.model.Todo

object TodoRepo {
    suspend fun Account.todos() =
        useConnection {
            sendPreparedStatement("select * from todos where account_id = ?", listOf(id))
                .await()
                .rows
                .asSequence()
                .map(::toTodo)
        }

    private fun toTodo(rowData: RowData) =
        Todo(
            id=         rowData.getAs("id"),
            description=rowData.getAs("description"),
            priority=   rowData.getAs("priority"),
            due=        rowData.getAs("due"),
            done=       rowData.getAs("done")
        )

}
