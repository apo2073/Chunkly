package kr.apo2073.chunkly.papi

import kr.apo2073.chunkly.Chunkly
import kr.apo2073.chunkly.chunks.Chunks
import kr.apo2073.chunkly.data.UserData
import kr.apo2073.chunkly.utils.LangManager.translate
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlaceHolderHandler: PlaceholderExpansion() {
    override fun getIdentifier(): String = "Chunkly"
    override fun getAuthor(): String = Chunkly.plugin.pluginMeta.authors.joinToString(", ")
    override fun getVersion(): String = Chunkly.plugin.pluginMeta.version

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player==null) return null
        if (!params.startsWith("chunkly")) return null
        val param=params.removePrefix("chunkly.")
        if (param=="canbuy") return Chunks(player.x, player.z, player.world).canBuy().toString()
        if (param=="owner") return Chunks(player.x, player.z, player.world).getOwner() ?: translate("placeholder.owner.nul")
        if (param.contains("share")) {
            val paramSpit=param.split(".")
            val owner= Bukkit.getOfflinePlayer(paramSpit[0])
            val num=paramSpit[2].toIntOrNull() ?: return null

            val shareList=UserData.getConfig(owner.uniqueId).getStringList("user.share-permissions")
            return shareList[num]
        }
        return null
    }
}