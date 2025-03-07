package kr.apo2073.chunkly.data

import kr.apo2073.chunkly.Chunkly
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.*

class UserData {
    companion object {
        fun getConfig(uuid: UUID): YamlConfiguration {
            val file= File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
            if (!file.exists()) file.createNewFile()
            return YamlConfiguration.loadConfiguration(file)
        }
        fun setValue(path:String, value:Any, uuid:UUID) {
            val file= File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
            val config=YamlConfiguration.loadConfiguration(file)
            config.set(path, value)
            config.save(file)
        }

        fun addChunk(chunkKey:String, uuid: UUID) {
            val list= getConfig(uuid).getStringList("user.has-chunk")
            list.add(chunkKey)
            setValue("user.has-chunk", list, uuid)
        }

        fun getMember(uuid:UUID): List<UUID> {
            val list= getConfig(uuid).getStringList("user.share-permissions")
            return list.map { UUID.fromString(it) }
        }
        fun addMember(player: Player, uuid: UUID) {
            val list= getConfig(uuid).getStringList("user.share-permissions")
            list.add(player.uniqueId.toString())
            setValue("user.share-permissions", list, uuid)
        }
        fun removeMember(player: Player, uuid: UUID) {
            val list= getConfig(uuid).getStringList("user.share-permissions")
            list.remove(player.uniqueId.toString())
            setValue("user.share-permissions", list, uuid)
        }

        fun countChunkInWorld(world: UUID, uuid: UUID):Int {
            var count=0
            val worlds=Bukkit.getWorld(world) ?: return 0
            val list= getConfig(uuid).getStringList("user.has-chunk")
            for (chunk in list) {
                val key=chunk.toLongOrNull() ?: continue
                val owner=worlds.getChunkAt(key).persistentDataContainer.get(
                    NamespacedKey(Chunkly.plugin, "owner"), PersistentDataType.STRING
                ) ?: continue
                if (owner==uuid.toString()) count+=1
            }
            return count
        }
    }
}