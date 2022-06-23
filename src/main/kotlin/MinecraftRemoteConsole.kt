package top.limbang.minecraft

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.globalEventChannel
import top.limbang.minecraft.PluginData.serverInfo
import top.limbang.minecraft.utils.toCommand
import top.limbang.mirai.event.RenameEvent
import java.io.IOException

object MinecraftRemoteConsole : KotlinPlugin(JvmPluginDescription(
    id = "top.limbang.minecraft-remote-console",
    name = "MinecraftRemoteConsole",
    version = "0.1.1",
) {
    author("limbang")
    info("""Minecraft远程控制台""")
    dependsOn("top.limbang.general-plugin-interface")
}) {

    private val rconList: MutableMap<String, Rcon> = mutableMapOf()

    override fun onDisable() {
        // 关闭全部 rcon 连接
        rconList.forEach {
            it.value.close()
        }
    }

    override fun onEnable() {
        // 加载插件数据和注册插件命令
        PluginData.reload()
        PluginCompositeCommand.register()
        // 根据配置打开rcon连接
        serverInfo.forEach {
            openRcon(it.key, it.value.ip, it.value.port, it.value.password)
        }
        // 监听改名事件
        globalEventChannel().subscribeAlways<RenameEvent> {
            logger.info("RenameEvent: pluginId = $pluginId oldName = $oldName newName = $newName")
            if (!PluginData.isPluginLinkage) return@subscribeAlways
            if (pluginId == MinecraftRemoteConsole.id) return@subscribeAlways
            PluginCompositeCommand.renameServer(oldName, newName, true)
        }
    }


    /**
     * ## 打开rcon连接
     *
     * @param name 服务器名称
     * @param ip 服务器 ip
     * @param port 服务器 rcon 端口
     * @param password 服务器 rcon 密码
     */
    fun openRcon(name: String, ip: String, port: Int, password: String): Boolean {
        runCatching { Rcon.open(ip, port) }.onSuccess { rcon ->
            // 判断是否认证失败
            if (!rcon.authenticate(password)) {
                // 输出认证失败消息,并关闭
                logger.error("$name authentication failed, please check the rcon.password value in the server.properties file.")
                rcon.close()
            } else {
                rconList[name] = rcon
                return true
            }
        }.onFailure { logger.error(it) }
        return false
    }

    /**
     * ## 向指定服务器发送远程命令
     *
     * @param name 服务器名称
     * @param command 命令
     */
    fun sendCommand(name: String, command: String): String {
        return try {
            rconList[name]?.sendCommand(command) ?: "Server $name does not exist or did not connect successfully"
        } catch (e: IOException) {
            "The remote server $name is down!!!"
        }
    }

    /**
     * 修改服务器名称
     *
     * @param name 原名称
     * @param newName 新名称
     * @return
     */
    fun rename(name: String, newName: String): Boolean {
        rconList[name]?.let {
            rconList[newName] = it
            return true
        }
        return false
    }

    /**
     * 删除正在连接的服务器
     *
     * @param name
     */
    fun delete(name: String): Boolean {
        rconList[name]?.let {
            it.close()
            return rconList.remove(name) != null
        }
        return false
    }
}

/**
 * ## 支持`rcon`协议的服务器
 *
 * @property ip 服务器地址
 * @property port rcon 端口
 * @property password rcon 验证密码
 */
@Serializable
data class RconServer(val ip: String, val port: Int, val password: String)

/**
 * ## 插件内部数据
 */
object PluginData : AutoSavePluginData("MinecraftRemoteConsole") {
    val serverInfo: MutableMap<String, RconServer> by value()

    @ValueDescription("插件联动,默认打开")
    var isPluginLinkage: Boolean by value(true)
}

/**
 * ## 插件复合命令
 */
object PluginCompositeCommand : CompositeCommand(MinecraftRemoteConsole, "rcon") {

    @SubCommand
    @Description("向服务器发送远程命令")
    suspend fun CommandSender.cmd(name: String, vararg command: String) {
        sendMessage(MinecraftRemoteConsole.sendCommand(name, command.toCommand()))
    }

    @SubCommand
    @Description("添加服务器")
    suspend fun CommandSender.add(name: String, ip: String, port: Int, password: String) {
        if (MinecraftRemoteConsole.openRcon(name, ip, port, password)) {
            serverInfo[name] = RconServer(ip, port, password)
            sendMessage("Add $name server successfully")
        } else sendMessage("Add $name server failed,check the logs for the specific reason")

    }

    @SubCommand
    @Description("删除服务器")
    suspend fun CommandSender.delete(name: String) {
        if (serverInfo.remove(name) != null) {
            MinecraftRemoteConsole.delete(name)
            sendMessage("Delete $name server successfully")
        } else sendMessage("Server does not exist")
    }

    @SubCommand
    @Description("重命名服务器")
    suspend fun CommandSender.rename(name: String, newName: String) {
        sendMessage(renameServer(name, newName, false))
    }

    internal suspend fun renameServer(name: String, newName: String, isEvent: Boolean): String {
        MinecraftRemoteConsole.logger.info("4")
        serverInfo[name].let {
            MinecraftRemoteConsole.logger.info("5")
            if (it == null) {
                return "Server does not exist"
            }
            serverInfo[newName] = it
            serverInfo.remove(name)
            MinecraftRemoteConsole.logger.info("6")
            return if (MinecraftRemoteConsole.rename(name, newName)) {
                MinecraftRemoteConsole.logger.info("7")
                // 不是事件就发布改名广播
                if (!isEvent) RenameEvent(MinecraftRemoteConsole.id, name, newName).broadcast()
                "Server rename successful: $name -> $newName"
            } else "Server $name not connected successfully, rename failed"
        }
    }

    @SubCommand
    @Description("服务器列表")
    suspend fun CommandSender.list() {
        if (serverInfo.isEmpty()) {
            sendMessage("Current server list is empty")
            return
        }
        var list = "The server list is as follows:"
        serverInfo.forEach {
            list += "\n${it.key} : ${it.value.ip} ${it.value.port}"
        }
        sendMessage(list)
    }

    @SubCommand
    @Description("设置插件联动")
    suspend fun CommandSender.setPluginLinkage(value: Boolean) {
        PluginData.isPluginLinkage = value
        sendMessage("插件联动:${PluginData.isPluginLinkage}")
    }
}
