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
        fun getConfig(uuid: UUID, callback: (YamlConfiguration) -> Unit) {
            Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
                val file = File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
                if (!file.exists()) file.createNewFile()
                val config = YamlConfiguration.loadConfiguration(file)
                Bukkit.getScheduler().runTask(Chunkly.plugin, Runnable {
                    callback(config)
                })
            })
        }

        fun setValue(path: String, value: Any, uuid: UUID) {
            Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
                val file = File("${Chunkly.plugin.dataFolder}/userdata", "$uuid.yml")
                val config = YamlConfiguration.loadConfiguration(file)
                config.set(path, value)
                config.save(file)
            })
        }

        fun addChunk(chunkKey: String, uuid: UUID) {
            getConfig(uuid) { config ->
                val list = config.getStringList("user.has-chunk")
                list.add(chunkKey)
                setValue("user.has-chunk", list, uuid)
            }
        }

        fun removeChunk(chunkKey: String, uuid: UUID) {
            getConfig(uuid) { config ->
                val list = config.getStringList("user.has-chunk")
                list.remove(chunkKey)
                setValue("user.has-chunk", list, uuid)
            }
        }

        fun getMember(uuid: UUID, callback: (List<UUID>) -> Unit) {
            getConfig(uuid) { config ->
                val list = config.getStringList("user.share-permissions")
                val members = list.map { UUID.fromString(it) }
                callback(members)
            }
        }

        fun addMember(player: Player, uuid: UUID) {
            getConfig(uuid) { config ->
                val list = config.getStringList("user.share-permissions")
                list.add(player.uniqueId.toString())
                setValue("user.share-permissions", list, uuid)
            }
        }

        fun removeMember(player: Player, uuid: UUID) {
            getConfig(uuid) { config ->
                val list = config.getStringList("user.share-permissions")
                list.remove(player.uniqueId.toString())
                setValue("user.share-permissions", list, uuid)
            }
        }

        fun countChunkInWorld(world: UUID, uuid: UUID, callback: (Int) -> Unit) {
            Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
                val worlds = Bukkit.getWorld(world) ?: run {
                    Bukkit.getScheduler().runTask(Chunkly.plugin, Runnable {
                        callback(0)
                    })
                    return@Runnable
                }
                getConfig(uuid) { config ->
                    val list = config.getStringList("user.has-chunk")
                    var count = 0
                    for (chunk in list) {
                        val key = chunk.toLongOrNull() ?: continue
                        val owner = worlds.getChunkAt(key).persistentDataContainer.get(
                            NamespacedKey(Chunkly.plugin, "owner"), PersistentDataType.STRING
                        ) ?: continue
                        if (owner == uuid.toString()) count += 1
                    }
                    Bukkit.getScheduler().runTask(Chunkly.plugin, Runnable {
                        callback(count)
                    })
                }
            })
        }
    }
}