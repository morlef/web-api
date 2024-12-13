package si.f5.luna3419

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import si.f5.luna3419.auth.UserCommand
import si.f5.luna3419.plugins.configureAuth
import si.f5.luna3419.plugins.configureHTTP
import si.f5.luna3419.plugins.configureRouting
import si.f5.luna3419.plugins.configureSerialization
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess

lateinit var properties: Properties
val logger: Logger = LoggerFactory.getLogger("API")
val configLoader = ConfigLoader()

fun main() {
    properties = Properties().apply { File("server.properties").inputStream().use { load(it) } }

    embeddedServer(Netty, port = properties.getProperty("server-port").toInt(), host = properties.getProperty("server-ip"), module = Application::module).start(wait = true)
}

fun Application.module() {
    PluginLoader().load()

    Thread {
        System.`in`.bufferedReader().use { br ->
            while (true) {
                val line = br.readLine().split(" ")

                when (line[0].lowercase()) {
                    "stop" -> {
                        PluginLoader.PLUGINS_STOP.forEach {
                            it.invoke(it.javaClass.getDeclaredConstructor().newInstance())
                        }
                        exitProcess(0)
                    }
                    "user" -> {
                        UserCommand().onCommand(line.subList(1, line.size).toMutableList())
                    }
                }

                val method = PluginLoader.PLUGINS_COMMAND[line[0]]

                method?.invoke(method.javaClass.getDeclaredConstructor().newInstance(), line.subList(1, line.size).toMutableList())
            }
        }
    }.start()

    configureHTTP()
    configureAuth()
    configureSerialization()
    configureRouting()

    install(CallLogging)
}