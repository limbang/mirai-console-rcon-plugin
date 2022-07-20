<div align="center">

[![](https://img.shields.io/github/v/release/limbang/mirai-console-rcon-plugin?include_prereleases)](https://github.com/limbang/mirai-console-rcon-plugin/releases)
![](https://img.shields.io/github/downloads/limbang/mirai-console-rcon-plugin/total)
[![](https://img.shields.io/github/license/limbang/mirai-console-rcon-plugin)](https://github.com/limbang/mirai-console-rcon-plugin/blob/master/LICENSE)
[![](https://img.shields.io/badge/mirai-2.12.0-69c1b9)](https://github.com/mamoe/mirai)

本项目是基于 Mirai Console 编写的插件
<p>使用 <a href = "https://wiki.vg/RCON">RCON</a> 协议远程控制Minecraft服务器</p>
</div>

可选前置插件[mirai-plugin-general-interface](https://github.com/limbang/mirai-plugin-general-interface)用来支持事件

## 命令
```shell
/rcon add <name> <ip> <port> <password>    # 添加服务器
/rcon cmd <name> <command>    # 向服务器发送远程命令
/rcon delete <name>    # 删除服务器
/rcon list    # 服务器列表
/rcon rename <name> <newName>    # 重命名服务器
```

## 使用方法
- 1.修改服务器配置文件`server.properties`里面的`enable-rcon`改成`=true`
- 2.修改服务器配置文件`server.properties`里面的(没有就直接添加)`rcon.port`改成你需要的端口如`=25575`
- 3.修改服务器配置文件`server.properties`里面的(没有就直接添加)`rcon.password`改成你需要的密码如`=ZTb0S4Vju7nqRJ6Ym6bVOmBPZg`
- 4.重启你的服务器,等待服务器启动完成
- 5.在控制台或者qq向机器人发送`/rcon add 服务器名称 服务器IP 25575 ZTb0S4Vju7nqRJ6Ym6bVOmBPZg` 添加成功后就可以正常使用
- 6.给服务器发送命令使用`/rcon cmd 服务器名称 命令`