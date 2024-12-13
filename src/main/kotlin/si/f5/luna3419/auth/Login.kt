package si.f5.luna3419.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import si.f5.luna3419.configLoader
import si.f5.luna3419.logger
import si.f5.luna3419.`object`.LoginRequest
import si.f5.luna3419.`object`.LoginResponse
import si.f5.luna3419.properties
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

val userConfig = configLoader.loadUsers()

fun Route.login() {
    route("/auth") {
        post("/login") {
            call.receive<LoginRequest>().let {
                val id = userConfig.firstOrNull { data -> data.userId == it.userId }?.userId ?: return@post call.respond(LoginResponse("not found!", "null"))
                logger.info("Found id: $id")

                userConfig.firstOrNull { data -> data.userId == it.userId }?.let { data ->
                    if (BCrypt.checkpw(it.password, data.encryptedPassword).not()) {
                        return@post call.respond(LoginResponse("not found!", "null"))
                    }
                }

                logger.info("Logged in with id: $id")
                call.respond(
                    LoginResponse(
                        id,
                        JWT.create()
                            .withAudience("dummy")
                            .withExpiresAt(Date.from(LocalDateTime.now().plusHours(1).plusMinutes(30).toInstant(ZoneOffset.UTC)))
                            .withClaim("user_id", id)
                            .withIssuer("https://luna3419.f5.si")
                            .sign(Algorithm.HMAC256(properties.getProperty("secret")))
                    )
                )
            }
        }
    }
}