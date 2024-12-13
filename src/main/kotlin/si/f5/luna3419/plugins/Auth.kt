package si.f5.luna3419.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import si.f5.luna3419.properties

fun Application.configureAuth() {
    install(Authentication) {
        jwt {
            realm = "si.f5.luna3419.api"
            verifier(
                JWT.require(Algorithm.HMAC256(properties.getProperty("secret")))
                    .withAudience("dummy").withIssuer("https://luna3419.f5.si").build()
            )

            validate {
                it.payload.getClaim("user_id").let { claim ->
                    if (!claim.isNull) {
                        UserIdPrincipal(claim.asString())
                    } else {
                        null
                    }
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
