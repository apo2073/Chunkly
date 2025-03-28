package kr.apo2073.chunkly.chunks

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.utils.ConfigManager.getConfigFile
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class ChunkBorder {
    private val plugin = Chunkly.plugin

    companion object {
        fun start() {
            Bukkit.getScheduler().runTaskTimer(ChunkBorder().plugin, Runnable {
                for (player in Bukkit.getOnlinePlayers()) {
                    Bukkit.getScheduler().runTaskAsynchronously(ChunkBorder().plugin, Runnable {
                        val file = getConfigFile("items")
                        val config = YamlConfiguration.loadConfiguration(file)
                        val chunkItem = config.getItemStack("items") ?: return@Runnable

                        val itemInHand = player.inventory.itemInMainHand
                        if (!itemInHand.isSimilar(chunkItem)) return@Runnable

                        val particle = Particle.valueOf(
                            ChunkBorder().plugin.config
                                .getString("particle")?.uppercase() ?: return@Runnable
                        )

                        Bukkit.getScheduler().runTask(ChunkBorder().plugin, Runnable {
                            ChunkBorder().show(player, particle)
                        })
                    })
                }
            }, 9L, 9)
        }
    }

    private fun show(player: Player, particle: Particle) {
        if (
            player.chunk.persistentDataContainer.get(
                NamespacedKey(Chunkly.plugin, "owner"),
                PersistentDataType.STRING
            ) != null
        ) return

        val loc = player.location

        val chunkX = loc.blockX shr 4
        val chunkZ = loc.blockZ shr 4
        val minX = chunkX * 16
        val maxX = minX + 15
        val minZ = chunkZ * 16
        val maxZ = minZ + 15

        val minY = (loc.y - 20).toInt()
        val maxY = (loc.y + 20).toInt()

        val spacing = 1

        drawBorder(
            player,
            minX, maxX,
            minZ, maxZ,
            minY, maxY,
            spacing, particle
        )
    }

    private fun drawBorder(
        player: Player,
        minX: Int, maxX: Int,
        minZ: Int, maxZ: Int,
        minY: Int, maxY: Int,
        spacing: Int, particle: Particle
    ) {
        listOf(minZ, maxZ).forEach { z ->
            for (x in minX..maxX step spacing) {
                for (y in minY..maxY step spacing) {
                    player.spawnParticle(
                        particle,
                        x.toDouble(), y.toDouble(), z.toDouble(),
                        1, 0.0, 0.0, 0.0, 0.0
                    )
                }
            }
        }

        for (x in listOf(minX, maxX)) {
            for (z in minZ..maxZ step spacing) {
                for (y in minY..maxY step spacing) {
                    player.spawnParticle(
                        particle,
                        x.toDouble(), y.toDouble(), z.toDouble(),
                        1, 0.0, 0.0, 0.0, 0.0
                    )
                }
            }
        }
    }
}