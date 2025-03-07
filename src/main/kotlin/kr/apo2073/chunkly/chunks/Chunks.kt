package kr.apo2073.chunkly.chunks

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.data.UserData
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.File

class Chunks {
    private var chunk: Chunk?
    private val plugin= Chunkly.plugin
    constructor(chunk: Chunk) {
        this.chunk =chunk
        file=File("${plugin.dataFolder}/chunkdata", "${this.chunk?.chunkKey}.yml")
    }
    constructor(x: Double, z: Double, world:World) {
        chunk=plugin.server.getWorld(world.uid)?.getChunkAt(x.toInt(), z.toInt())
        file=File("${plugin.dataFolder}/chunkdata", "${chunk?.chunkKey}.yml")
    }

    fun getOwner():String? {
        return chunk?.persistentDataContainer?.get(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING
        )
    }

    fun setOwner(player: Player?) {
        chunk?.persistentDataContainer?.set(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING, (player?.uniqueId ?: run {
                chunk?.persistentDataContainer?.remove(NamespacedKey(plugin, "owner"))
                return
            }).toString()
        ) ?: return

        val config=getConfig()
        config.set("chunk.key", chunk?.chunkKey)
        config.set("chunk.location.x", chunk?.x)
        config.set("chunk.location.z", chunk?.z)
        config.set("chunk.world", chunk?.world?.uid.toString())
        config.set("chunk.owner", player?.name)
        config.save(file)
    }

    fun addMember(player: Player) {
        val list=UserData.getConfig(
            Bukkit.getPlayer(getOwner().toString())?.uniqueId ?: return
        ).getStringList("user.share-permissions")
        list.add(player.name)
        UserData.setValue(
            "user.share-permissions", list,
            Bukkit.getPlayer(getOwner() ?: return)?.uniqueId ?: return
        )
    }

    fun getMembers(): MutableList<String>? {
//        return getConfig().getStringList("members")
        return UserData.getConfig(
            Bukkit.getPlayer(getOwner().toString())?.uniqueId ?: return null
        ).getStringList("user.share-permissions")
    }

    private var file:File
    fun getConfig():YamlConfiguration {
        file=File("${plugin.dataFolder}/chunkdata", "${chunk?.chunkKey}.yml")
        return YamlConfiguration.loadConfiguration(file)
    }

    fun getChunk(): Chunk? = chunk

    fun canBuy():Boolean = getOwner()==null
}