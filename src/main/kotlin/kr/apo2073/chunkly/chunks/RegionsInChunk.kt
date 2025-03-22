package kr.apo2073.chunkly.chunks

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.ApplicableRegionSet
import org.bukkit.Bukkit
import org.bukkit.Chunk

class RegionsInChunk {
    companion object {
        fun getRegion(chunk: Chunk): ApplicableRegionSet? {
            if(Bukkit.getPluginManager().getPlugin("WorldGuard")==null) return null
            val world=chunk.world
            val container=WorldGuard.getInstance().platform.regionContainer
            val regions=container.get(BukkitAdapter.adapt(world))
            val chunkPos=BlockVector3.at(chunk.x shl 4 + 8, 64, chunk.z shl 4 + 8)
            val regionSet=regions?.getApplicableRegions(chunkPos)
            return regionSet
        }
    }
}