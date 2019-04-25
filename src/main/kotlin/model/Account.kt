package net.bernerbits.sample.model

import io.ktor.auth.Principal
import java.io.Serializable

data class Account(val id: Long, val login: String, val displayName: String): Principal, Serializable
