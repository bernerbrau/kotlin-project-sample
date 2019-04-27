package net.bernerbits.sample.auth

import io.ktor.auth.Principal
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.model.AccountFields

data class AccountPrincipal(val id: Long, val fields: AccountFields): Principal
fun Account.asPrincipal() = AccountPrincipal(id, fields)
val AccountPrincipal.account get() = Account(id, fields)