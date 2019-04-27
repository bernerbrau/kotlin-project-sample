package net.bernerbits.sample.model

import io.ktor.auth.Principal
import net.bernerbits.sample.db.Saved
import java.io.Serializable

data class AccountFields(val login: String, val displayName: String)
typealias Account = Saved<Long,AccountFields>
