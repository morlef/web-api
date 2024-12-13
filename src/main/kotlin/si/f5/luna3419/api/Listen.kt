package si.f5.luna3419.api

annotation class Listen(val path: String, val require: Array<String>, val method: Method, val responseType: ResponseType, val requireAuth: Boolean)
