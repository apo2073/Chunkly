package kr.apo2073.chunkly.data

import kr.apo2073.chunkly.Chunkly
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class UserData {
    companion object {
        fun getConfig(uuid: UUID): YamlConfiguration {
            val file= File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
            return YamlConfiguration.loadConfiguration(file)
        }
        fun setValue(path:String, value:Any, uuid:UUID) {
            val file= File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
            val config=YamlConfiguration.loadConfiguration(file)
            config.set(path, value)
            config.save(file)
        }
    }
}