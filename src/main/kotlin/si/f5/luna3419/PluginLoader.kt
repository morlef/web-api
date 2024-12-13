package si.f5.luna3419

import si.f5.luna3419.api.Command
import si.f5.luna3419.api.Listen
import si.f5.luna3419.api.OnStart
import si.f5.luna3419.api.OnStop
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile

class PluginLoader {
    companion object {
        val PLUGINS = hashMapOf<Listen, Method>()
        val PLUGINS_COMMAND = hashMapOf<String, Method>()
        val PLUGINS_STOP = arrayListOf<Method>()
    }

    fun load() {
        val file = File("plugins")

        if (!file.exists()) {
            file.mkdirs()
        }

        file.listFiles { _, name -> name.endsWith(".jar") }?.forEach { jar ->
            val jarFile = JarFile(jar.path)
            val entries = jarFile.entries()

            val urls = arrayOf(URL("jar:file:" + jar.path + "!/"))
            val loader = URLClassLoader.newInstance(urls, javaClass.classLoader)

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory || !entry.name.endsWith(".class") || entry.name.endsWith("module-info.class")) {
                    continue
                }

                val className = entry.name.substring(0, entry.name.length - 6).replace('/', '.')

                for (method in loader.loadClass(className).methods) {
                    method.getAnnotation(Listen::class.java)?.let {
                        PLUGINS[it] = method
                        logger.info("Register listener ${method.name} (/v1${if (it.path.startsWith("/")) it.path else "/" + it.path})")
                    }
                    method.getAnnotation(Command::class.java)?.let {
                        PLUGINS_COMMAND[it.name] = method
                        logger.info("Register Command ${it.name}")
                    }
                    method.getAnnotation(OnStop::class.java)?.let { PLUGINS_STOP.add(method) }
                    method.getAnnotation(OnStart::class.java)?.let { method.invoke(method.javaClass.getDeclaredConstructor().newInstance()) }
                }
            }
        }
    }
}