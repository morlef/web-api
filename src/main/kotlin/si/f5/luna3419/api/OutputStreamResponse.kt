package si.f5.luna3419.api

import io.ktor.http.*
import java.io.OutputStream

data class OutputStreamResponse(val contentType: ContentType, val statusCode: HttpStatusCode, val body: suspend OutputStream.() -> Unit)