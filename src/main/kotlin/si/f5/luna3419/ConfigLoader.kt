package si.f5.luna3419

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import si.f5.luna3419.`object`.UserData
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList


class ConfigLoader {
    fun loadUsers() = load("users.json", Array<UserData>::class.java) ?: ArrayList()
    fun saveUsers(users: List<UserData>) = save("users.json", users)

    private fun <T> load(file: String, clazz: Class<T>) : T? {
        saveResource(file)
        return loadFile(File(file), clazz)
    }

    private fun <T> load(file: String, clazz: Class<Array<T>>) : ArrayList<T>? {
        saveResource(file)
        return loadFileAsList(File(file), clazz)
    }

    private fun save(file: String, obj: Any) {
        FileWriter(file, StandardCharsets.UTF_8).use {
            GsonBuilder().setPrettyPrinting().create().toJson(obj, it)
        }
    }

    private fun <T> loadFile(file: File, clazz: Class<T>) : T? {
        try {
            FileInputStream(file).use { fin ->
                InputStreamReader(fin, StandardCharsets.UTF_8).use {
                    return Gson().fromJson(it, clazz)
                }
            }
        } catch (e: FileNotFoundException) {
            logger.error("Can't load config file.", e)
            return null
        }
    }

    private fun <T> loadFileAsList(file: File, clazz: Class<Array<T>>?): ArrayList<T>? {
        try {
            FileInputStream(file).use { fin ->
                InputStreamReader(fin, StandardCharsets.UTF_8).use {
                    val arr = Gson().fromJson(it, clazz)
                    return arrayListOf(*arr)
                }
            }
        } catch (e: FileNotFoundException) {
            logger.error("Can't load config file.", e)
            return null
        }
    }

    private fun saveResource(file: String) {
        if (!File(file).exists()) {
            try {
                getResource(file).use { `in` ->
                    requireNotNull(`in`) { "The embedded resource \"bungee_config.json\" cannot be found in the file" }
                    val outFile = File(file)
                    try {
                        FileOutputStream(outFile).use { out ->
                            val buf = ByteArray(1024)
                            var len: Int
                            while ((`in`.read(buf).also { len = it }) > 0) {
                                out.write(buf, 0, len)
                            }
                        }
                    } catch (e: IOException) {
                        throw IOException("Could not save " + outFile.name + " to " + outFile)
                    }
                }
            } catch (e: IOException) {
                throw IllegalArgumentException("ResourcePath cannot be null or empty")
            }
        }
    }

    private fun getResource(filename: String): InputStream? {
        val url = this.javaClass.classLoader.getResource(filename) ?: return null
        try {
            val connection = url.openConnection()
            connection.useCaches = false
            return connection.getInputStream()
        } catch (var4: IOException) {
            return null
        }
    }
}