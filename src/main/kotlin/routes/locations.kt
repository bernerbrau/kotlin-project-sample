package net.bernerbits.sample.routes

import io.ktor.locations.Location

@Location("/todos") object Todos {
    @Location("/{id}") data class Todo(val todos: Todos=Todos, val id: Long)
}