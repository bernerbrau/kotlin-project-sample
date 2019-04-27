package net.bernerbits.sample.model

import net.bernerbits.sample.db.Saved
import org.joda.time.DateTime

data class TodoFields(
    val description: String,
    val priority: Int,
    val due: DateTime,
    val done: DateTime?
)

typealias Todo = Saved<Long, TodoFields>