package kr.apo2073.chunkly.chunks

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import kr.apo2073.chunkly.Chunkly
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.plugin.java.JavaPlugin

class RegionsInChunks(private val plugin: JavaPlugin) {
    private val regionCache = mutableMapOf<String, List<ProtectedRegion>>()
    fun getRegionsInChunkAsync(
        chunk: Chunk,
        callback: (List<ProtectedRegion>) -> Unit
    ) {
        val plugin = Chunkly.plugin
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val cacheKey = "${chunk.world.name}:${chunk.x}:${chunk.z}"

            regionCache[cacheKey]?.let {
                runOnMainThread { callback(it) }
                return@Runnable
            }

            val regions = computeRegionsInChunk(chunk)

            regionCache[cacheKey] = regions

            runOnMainThread { callback(regions) }
        })
    }

    private fun computeRegionsInChunk(chunk: Chunk): List<ProtectedRegion> {
        val worldGuard = WorldGuard.getInstance()
        val regionContainer = worldGuard.platform.regionContainer
        val world = BukkitAdapter.adapt(chunk.world)

        val minX = chunk.x shl 4
        val minZ = chunk.z shl 4
        val maxX = minX + 15
        val maxZ = minZ + 15
        val minY = chunk.world.minHeight
        val maxY = chunk.world.maxHeight

        val minPoint = BlockVector3.at(minX, minY, minZ)
        val maxPoint = BlockVector3.at(maxX, maxY, maxZ)
        val chunkRegion = ProtectedCuboidRegion("temp_chunk_region", minPoint, maxPoint)

        val regionManager = regionContainer.get(world) ?: return emptyList()
        val applicableRegions = regionManager.getApplicableRegions(chunkRegion)
        return applicableRegions.regions.toList()
    }

    private fun runOnMainThread(action: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(Chunkly.plugin, Runnable {
            action()
        })
    }
}