package kr.apo2073.chunkly.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.apo2073.chunkly.Chunkly
import java.io.File

object LangManager {
    private val plugin = Chunkly.plugin
    private var language = plugin.config.getString("lang", "ko")
    private var langFile = File("${plugin.dataFolder}/lang", "${language}.json")
    private val langJson: JsonObject

    init {
        plugin.reloadConfig()
        language = plugin.config.getString("lang", "ko") ?: "ko"
        langFile = File("${plugin.dataFolder}/lang", "${language}.json")
        langJson = try {
            val lang = langFile.readText()
            Gson().fromJson(lang, JsonObject::class.java)
        } catch (e: Exception) {
            JsonObject()
        }
    }

    fun translate(text: String): String {
        return try {
            langJson.get(text)?.asString
                ?.replace("{lang}", language.toString())
                ?.replace("&", "§")
                ?: text
        } catch (e: Exception) {
            text
        }
    }
}