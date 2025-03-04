package kr.apo2073.chunkly.chunks

import kr.apo2073.chunkly.Chunkly
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.File

class Chunks {
    private var chunks: Chunk?
    private val plugin= Chunkly.plugin
    constructor(chunk: Chunk) {
        chunks=chunk
        file=File("${plugin.dataFolder}/chunkdata", "${chunks?.chunkKey}.yml")
    }
    constructor(x: Double, z: Double, world:World) {
        chunks=plugin.server.getWorld(world.uid)?.getChunkAt(x.toInt(), z.toInt())
        file=File("${plugin.dataFolder}/chunkdata", "${chunks?.chunkKey}.yml")
    }

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

    private lateinit var file:File
    fun getConfig():YamlConfiguration {
        file=File("${plugin.dataFolder}/chunkdata", "${chunks?.chunkKey}.yml")
        return YamlConfiguration.loadConfiguration(file)
    }

    fun getChunk(): Chunk? = chunks

    fun canBuy():Boolean = getOwner()==null
}