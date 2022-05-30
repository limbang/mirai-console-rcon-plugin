package top.limbang.minecraft

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object MinecraftRemoteConsole : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.minecraft-remote-console",
        name = "MinecraftRemoteConsole",
        version = "0.1.0",
    ) {
        author("limbang")
        info("""Minecraft远程控制台""")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
    }
}