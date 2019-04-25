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
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import io.ktor.auth.*
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import net.bernerbits.sample.model.Account
import net.bernerbits.sample.repo.AccountRepo
import net.bernerbits.sample.repo.TodoRepo
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
        cookie<Account>(
            "TODOS_SESSION_ID",
            directorySessionStorage(File(".sessions"), cached=true)
        ) {
            cookie.httpOnly = true
            cookie.path = "/"
            serializer = object : SessionSerializer {
                private val om = jacksonObjectMapper()
                override fun deserialize(text: String) = om.readValue<Account>(text)
                override fun serialize(session: Any) = om.writeValueAsString(session)
            }
        }
    }

    install(Authentication) {
        session<Account>("session")
        basic("basic") {
            realm = "TODO Service"
            validate { (name, password) ->
                val account = AccountRepo.login(name,password)
                if (account !== null) {
                    sessions.set(account)
                }
                account
            }
        }
    }

    routing {
        authenticate("session","basic") {

            get("/") {
                val account = call.authentication.principal<Account>() ?: throw AuthenticationException()
                call.respondText("Hello, ${account.displayName}!", contentType = ContentType.Text.Plain)
            }

            get("/todos") {
                val account = call.authentication.principal<Account>() ?: throw AuthenticationException()
                TodoRepo.run {
                    call.respond(account.todos().toList())
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

        get<MyLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

    }
}

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}") data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
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
