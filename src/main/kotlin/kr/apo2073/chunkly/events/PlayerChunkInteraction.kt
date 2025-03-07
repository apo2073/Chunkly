package kr.apo2073.chunkly.events

import com.destroystokyo.paper.event.player.*
import io.papermc.paper.event.player.*
import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.data.UserData
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PlayerChunkInteraction : Listener {
    private val plugin = Chunkly.plugin

    private fun hasPermission(chunk: Chunk, player: Player): Boolean {
        val owner = chunk.persistentDataContainer.get(
            NamespacedKey(plugin, "owner"), PersistentDataType.STRING
        ) ?: return true
        if (owner==player.uniqueId.toString() || player.isOp) return true
        val shareList = UserData.getMember(UUID.fromString(owner))
        return shareList.contains(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerInteractEvent.onInteraction() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerHarvestBlockEvent.onHarvest() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerAttemptPickupItemEvent.onPickUp() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBedEnterEvent.onEnter() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketFillEvent.onBucket() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketEmptyEvent.onBucket() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerBucketEntityEvent.onBucket() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerLaunchProjectileEvent.onLaunch() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerItemFrameChangeEvent.onFrameChange() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerSetSpawnEvent.onSetSpawn() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerDropItemEvent.onDrop() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerItemMendEvent.onMend() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityPlaceEvent.onPlace() {
        val entityPlayer = player ?: return
        if (!hasPermission(entityPlayer.chunk, entityPlayer)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerAdvancementCriterionGrantEvent.onAdvancementGrant() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerChangeBeaconEffectEvent.onBeaconChange() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun ProjectileHitEvent.onProjectileHit() {
        val shooter = entity.shooter as? Player ?: return
        if (!hasPermission(shooter.chunk, shooter)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockBreakEvent.onBlockBreak() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun BlockPlaceEvent.onBlockPlace() {
        if (!hasPermission(player.chunk, player)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityDamageByEntityEvent.onEntityDamage() {
        val damaged = damager as? Player ?: return
        if (!hasPermission(damaged.chunk, damaged)) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityExplodeEvent.onEntityExplode() {
        if (!hasPermission(location.chunk, location.world.players.firstOrNull {
            it.location.chunk == location.chunk
        } ?: return)) {
            isCancelled = true
        }
    }
}
