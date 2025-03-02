package kr.apo2073.chunkly.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.apo2073.chunkly.Chunkly
import java.io.File

object LangManager {
    private val plugin=Chunkly.plugin
    private var language= plugin.config.getString("lang", "ko")
    private var langFile=File("${plugin.dataFolder}/lang", "${language}.json")
    init {
        plugin.reloadConfig()
        language= plugin.config.getString("lang", "ko")
        langFile= File("${plugin.dataFolder}/lang", "${language}.json")
    }
    fun translate(text:String):String {
        try {
            val lang= langFile.readText()
            val langJson= Gson().fromJson(lang, JsonObject::class.java)
            return langJson.get(text).asString
                .replace("{lang}", language.toString())
                .replace("&", "§")
        } catch (e: Exception) { return text }
    }
}