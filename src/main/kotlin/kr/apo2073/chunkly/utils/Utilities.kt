package kr.apo2073.chunkly.utils

import net.kyori.adventure.text.minimessage.MiniMessage

private val prefix= MiniMessage.miniMessage().deserialize("<b><gradient:#DBCDF0:#8962C3>[ Chunkly ]</gradient></b> ")
fun org.bukkit.command.CommandSender.sendMessage(
    message:String,
    prefix:Boolean
) {
    this.sendMessage(if (prefix) {
        kr.apo2073.chunkly.utils.prefix.append(message.replace("&", "§").str2Component())
    }else {message.str2Component()})
}