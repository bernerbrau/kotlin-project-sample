package net.bernerbits.sample.repo

import com.github.jasync.sql.db.RowData
import kotlinx.coroutines.future.await
import net.bernerbits.sample.db.withConnection
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.model.Todo
import net.bernerbits.sample.model.TodoFields

object TodoRepo {
    suspend fun Account.todos() =
        withConnection {
            sendPreparedStatement("select * from todos where account_id = ?", listOf(id))
                .await()
                .rows
                .asSequence()
                .map(::toTodo)
        }

    suspend fun Account.getTodo(todoId: Long) =
        withConnection {
            sendPreparedStatement("select * from todos where account_id = ? and id = ?", listOf(id, todoId))
                .await()
                .rows
                .asSequence()
                .map(::toTodo)
                .singleOrNull()
        }

    suspend fun Account.createTodo(fields: TodoFields) =
        withConnection {
            val todoId = sendPreparedStatement(
                "insert into todos(account_id, description, priority, due, done) values (?,?,?,?,?) returning id",
                listOf(id, fields.description, fields.priority, fields.due, fields.done)
            )
                .await()
                .rows
                .asSequence()
                .map { it.getAs<Long>(0) }
                .single()
            Todo(todoId, fields)
        }

    suspend fun Account.updateTodo(todoId: Long, fields: TodoFields) =
        withConnection {
            val updated = sendPreparedStatement(
                "update todos set description=?, priority=?, due=?, done=? where account_id = ? and id = ?",
                listOf(fields.description, fields.priority, fields.due, fields.done, id, todoId)
            )
                .await()
                .rows
                .asSequence()
                .map { it.getInt(0) }
                .single()
            if (updated == 1) {
                Todo(todoId, fields)
            } else {
                null
            }
        }

    suspend fun Account.deleteTodo(todoId: Long) =
        withConnection {
            val updated = sendPreparedStatement(
                "delete from todos where account_id = ? and id = ?",
                listOf(id, todoId)
            )
                .await()
                .rows
                .asSequence()
                .map { it.getInt(0) }
                .single()
            updated == 1
        }

    private fun toTodo(rowData: RowData) =
        Todo(
            id=    rowData.getAs("id"),
            fields=TodoFields(
                description=rowData.getAs("description"),
                priority=   rowData.getAs("priority"),
                due=        rowData.getAs("due"),
                done=       rowData.getAs("done")
            )
        )

}
