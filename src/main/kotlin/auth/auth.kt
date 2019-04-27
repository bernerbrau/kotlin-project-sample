package net.bernerbits.sample.auth

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.util.pipeline.PipelineContext
import net.bernerbits.sample.AuthenticationException
import net.bernerbits.sample.model.Account

suspend fun <R> PipelineContext<Unit, ApplicationCall>.asAccount(action: suspend (Account) -> R): R {
    val principal = call.authentication.principal<AccountPrincipal>() ?: throw AuthenticationException()
    return action(principal.account)
}
