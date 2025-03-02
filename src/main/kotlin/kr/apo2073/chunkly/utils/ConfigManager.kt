package kr.apo2073.chunkly.utils

import kr.apo2073.chunkly.Chunkly
import java.io.File

object ConfigManager {
    private val plugin=Chunkly.plugin
    fun getConfigFile(name:String): File = File(plugin.dataFolder, "${name}.yml")
}