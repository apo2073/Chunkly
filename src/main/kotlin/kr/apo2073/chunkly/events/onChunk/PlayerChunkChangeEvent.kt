package kr.apo2073.chunkly.events.onChunk

import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerChunkChangeEvent(
    pastChunk:Chunk?,
    newChunk: Chunk,
    player: Player
): PlayerEvent(player), Cancellable {
    override fun getEventName() = "PlayerChunkChangeEvent"
    override fun getHandlers(): HandlerList = getHandlerList()
    companion object {
        private val handlers = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(setCancelled: Boolean) {
        cancelled=setCancelled
    }
}