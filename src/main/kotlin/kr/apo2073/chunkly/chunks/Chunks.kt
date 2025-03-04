package kr.apo2073.chunkly.chunks

import kr.apo2073.chunkly.Chunkly
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.File

class Chunks(x: Double, z: Double, world:World) {
    private val plugin= Chunkly.plugin
    private var chunks: Chunk? = plugin.server.getWorld(world.uid)?.getChunkAt(x.toInt(), z.toInt())
    fun getOwner():String? {
        return chunks?.persistentDataContainer?.get(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING
        )
    }

    fun setOwner(player: Player) {
        chunks?.persistentDataContainer?.set(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING, player.name
        ) ?: return
    }

    fun addMember(player: Player) {
        val list=getConfig().getStringList("members")
        list.add(player.name)
        getConfig().save(file)
    }

    fun getMembers(): MutableList<String> {
        return getConfig().getStringList("members")
    }

    private var file=File("${plugin.dataFolder}/chunkdata", "${chunks?.chunkKey}.yml")
    fun getConfig():YamlConfiguration {
        file=File("${plugin.dataFolder}/chunkdata", "${chunks?.chunkKey}.yml")
        return YamlConfiguration.loadConfiguration(file)
    }

    fun getChunk(): Chunk? = chunks
}