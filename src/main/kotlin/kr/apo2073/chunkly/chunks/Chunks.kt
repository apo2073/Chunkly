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
        return getConfig().getString("chunk.owner")
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
        UserData.getConfig(
            Bukkit.getPlayer(getOwner().toString())?.uniqueId ?: return
        ) {
            val list=it.getStringList("user.share-permissions")
            list.add(player.name)
            UserData.setValue(
                "user.share-permissions", list,
                Bukkit.getPlayer(getOwner() ?: return@getConfig)?.uniqueId ?: return@getConfig
            )
        }
    }

    fun getMembers(callback: (MutableList<String>?) -> Unit) {
        val ownerPlayer = Bukkit.getPlayer(getOwner().toString()) ?: run {
            callback(null)
            return
        }
        UserData.getConfig(ownerPlayer.uniqueId) { config ->
            val members = config.getStringList("user.share-permissions").toMutableList()
            callback(members)
        }
    }

    private var file:File
    fun getConfig():YamlConfiguration {
        file=File("${plugin.dataFolder}/chunkdata", "${chunk?.chunkKey}.yml")
        return YamlConfiguration.loadConfiguration(file)
    }

    fun getChunk(): Chunk? = chunk

    fun canBuy():Boolean = getOwner()==null
    fun setCanBuy(boolean: Boolean) {
        if (!boolean) {
            chunk?.persistentDataContainer?.set(
                NamespacedKey(plugin, "owner"), PersistentDataType.STRING, "none")
            val config=getConfig()
            config.set("chunk.owner", "cantbuy")
            config.set("chunk.key", chunk?.chunkKey)
            config.set("chunk.location.x", chunk?.x)
            config.set("chunk.location.z", chunk?.z)
            config.set("chunk.world", chunk?.world?.uid.toString())
            config.save(file)
        } else {
            chunk?.persistentDataContainer?.remove(NamespacedKey(plugin, "owner"))
            file.delete()
        }
    }
}