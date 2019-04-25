package net.bernerbits.sample.model

import org.joda.time.DateTime

data class Todo(
    val id: Long,
    val description: String,
    val priority: Int,
    val due: DateTime,
    val done: DateTime?
)
