package net.bernerbits.sample

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.locations.*
import io.ktor.features.*
import org.slf4j.event.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.jackson.*
import io.ktor.auth.*
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import net.bernerbits.sample.auth.AccountPrincipal
import net.bernerbits.sample.auth.asAccount
import net.bernerbits.sample.auth.asPrincipal
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.model.TodoFields
import net.bernerbits.sample.repo.AccountRepo
import net.bernerbits.sample.repo.TodoRepo
import net.bernerbits.sample.routes.Todos
import java.io.File
import java.util.*


fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(Locations) {
    }

    install(AutoHeadResponse)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        jackson {
            setTimeZone(TimeZone.getDefault())

            registerModule(JodaModule().apply {
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            })
        }
    }

    install(Sessions) {
        cookie<AccountPrincipal>(
            "TODOS_SESSION_ID",
            directorySessionStorage(File(".sessions"), cached=true)
        ) {
            cookie.httpOnly = true
            cookie.path = "/"
            serializer = object : SessionSerializer {
                private val om = jacksonObjectMapper()
                override fun deserialize(text: String) = om.readValue<AccountPrincipal>(text)
                override fun serialize(session: Any) = om.writeValueAsString(session)
            }
        }
    }

    install(Authentication) {
        session<AccountPrincipal>("session")
        basic("basic") {
            realm = "TODO Service"
            validate { (name, password) ->
                val account = AccountRepo.login(name,password)?.asPrincipal()
                if (account !== null) {
                    sessions.set(account)
                }
                account
            }
        }
    }

    routing {
        authenticate("session","basic") {

            get<Todos> {
                asAccount { account ->
                    TodoRepo.run {
                        call.respond(account.todos().toList())
                    }
                }
            }

            post<Todos> {
                asAccount { account ->
                    TodoRepo.run {
                        val todo = account.createTodo(call.receive<TodoFields>())
                        call.response.header("Location",application.locations.href(Todos.Todo(id=todo.id)))
                        call.respond(
                            HttpStatusCode.Created,
                            todo
                        )
                    }
                }
            }

            get<Todos.Todo> { (_,todoId) ->
                asAccount { account ->
                    TodoRepo.run {
                        call.respond(account.getTodo(todoId) ?: throw NotFoundException())
                    }
                }
            }

            put<Todos.Todo> { (_,todoId) ->
                asAccount { account ->
                    TodoRepo.run {
                        val todo = account.updateTodo(todoId, call.receive<TodoFields>()) ?: throw NotFoundException()
                        call.respond(todo)
                    }
                }
            }

            delete<Todos.Todo> { (_,todoId) ->
                asAccount { account ->
                    TodoRepo.run {
                        if (account.deleteTodo(todoId)) {
                            call.respond(HttpStatusCode.NoContent,"")
                        } else {
                            throw NotFoundException()
                        }
                    }
                }
            }
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        install(StatusPages) {
            exception<AuthenticationException> {
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> {
                call.respond(HttpStatusCode.Forbidden)
            }

        }

    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
