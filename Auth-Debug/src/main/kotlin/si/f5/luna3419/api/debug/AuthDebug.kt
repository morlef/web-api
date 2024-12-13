package si.f5.luna3419.api.debug

import si.f5.luna3419.api.Listen
import si.f5.luna3419.api.Method
import si.f5.luna3419.api.ResponseType

class AuthDebug {
    @Listen("/debug/auth/get", [], Method.GET, ResponseType.JSON, true)
    fun debugGet(map: Map<String, String>): Any {
        return Result(map.getOrDefault("user_id", "null"), Method.GET)
    }

    @Listen("/debug/auth/post", [], Method.POST, ResponseType.JSON, true)
    fun debugPost(map: Map<String, String>): Any {
        return Result(map.getOrDefault("user_id", "null"), Method.POST)
    }

    data class Result(
        val userID: String,
        val method: Method
    )
}