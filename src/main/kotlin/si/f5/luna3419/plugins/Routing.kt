package si.f5.luna3419.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import si.f5.luna3419.PluginLoader
import si.f5.luna3419.api.Method
import si.f5.luna3419.api.OutputStreamResponse
import si.f5.luna3419.api.ResponseType
import si.f5.luna3419.auth.login
import si.f5.luna3419.logger
import java.io.File

fun Application.configureRouting() {
    routing {
        login()
        get("/") {
            call.respondText("404!")
        }
        authenticate {
            get("/hello") {
                call.principal<UserIdPrincipal>()?.name?.let {
                    call.respondText("Hello $it!")
                }
            }
        }
        route("/v1") {
            PluginLoader.PLUGINS.forEach {
                val annotation = it.key
                val method = it.value
                val map = HashMap<String, String>()

                if (annotation.requireAuth) {
                    authenticate {
                        when (annotation.method) {
                            Method.GET -> {
                                get(if (annotation.path.startsWith("/")) annotation.path else "/" + annotation.path) {
                                    call.principal<UserIdPrincipal>()?.name?.let { uid ->
                                        map["user_id"] = uid
                                    }
                                    if (map.containsKey("user_id").not()) {
                                        return@get
                                    }
                                    annotation.require.forEach { require ->
                                        map[require] = call.parameters[require] ?: "null"
                                    }
                                    respond(annotation.responseType, call, method, map)
                                }
                            }

                            Method.POST -> {
                                post(if (annotation.path.startsWith("/")) annotation.path else "/" + annotation.path) {
                                    val receive = call.receive<Map<String, String>>()
                                    call.principal<UserIdPrincipal>()?.name?.let { uid ->
                                        map["user_id"] = uid
                                    }
                                    if (map.containsKey("user_id").not()) {
                                        return@post
                                    }
                                    annotation.require.forEach { require ->
                                        map[require] = receive[require] ?: "null"
                                    }
                                    respond(annotation.responseType, call, method, map)
                                }
                            }
                        }
                    }
                } else {
                    when (annotation.method) {
                        Method.GET -> {
                            get(if (annotation.path.startsWith("/")) annotation.path else "/" + annotation.path) {
                                annotation.require.forEach { require ->
                                    map[require] = call.parameters[require] ?: "null"
                                }
                                respond(annotation.responseType, call, method, map)
                            }
                        }

                        Method.POST -> {
                            post(if (annotation.path.startsWith("/")) annotation.path else "/" + annotation.path) {
                                val receive = call.receive<Map<String, String>>()
                                annotation.require.forEach { require ->
                                    map[require] = receive[require] ?: "null"
                                }
                                respond(annotation.responseType, call, method, map)
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun respond(
    type: ResponseType,
    call: ApplicationCall,
    method: java.lang.reflect.Method,
    map: HashMap<String, String>
) {

    val response = method.invoke(method.declaringClass.getDeclaredConstructor().newInstance(), map)

    when (type) {
        ResponseType.TEXT -> call.respondText(response as String)
        ResponseType.JSON -> call.respond(response)
        ResponseType.OUTPUT_STREAM -> {
            val out = response as OutputStreamResponse
            call.respondOutputStream(out.contentType, out.statusCode, out.body)
        }

        ResponseType.FILE -> call.respondFile(response as File)
        ResponseType.REDIRECT -> call.respondRedirect(response as String)
    }
}