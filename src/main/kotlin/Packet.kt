/*
 * Copyright 2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.minecraft

import top.limbang.minecraft.PacketType.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 数据包
 *
 * @property requestId 请求ID
 * @property type 请求类型
 * @property payload 有效载荷
 */
data class Packet(val requestId: Int, val type: PacketType, val payload: String) {
    val isValid = requestId != -1
}

/**
 * 数据包类型
 * - [SERVERDATA_RESPONSE_VALUE] 是对 SERVERDATA_EXECCOMMAND 请求的响应
 * - [SERVERDATA_EXECCOMMAND] 表示客户端向服务器发出的命令
 * - [SERVERDATA_AUTH_RESPONSE] 连接当前身份验证状态的通知
 * - [SERVERDATA_AUTH] 用于验证与服务器的连接
 * @property value
 */
enum class PacketType(val value: Int) {
    SERVERDATA_RESPONSE_VALUE(0),
    SERVERDATA_EXECCOMMAND(2),
    SERVERDATA_AUTH_RESPONSE(2),
    SERVERDATA_AUTH(3);

    companion object {
        private val valueMap = values().associateBy { it.value }
        fun fromValue(value: Int) = valueMap[value] ?: throw RuntimeException("未知的数据包类型：$value")
    }
}

/**
 * 数据包编解码
 *
 * @property charset 字符编码,默认为 [StandardCharsets.UTF_8]
 */
class PacketCodec(private val charset: Charset = StandardCharsets.UTF_8) {

    fun encode(packet: Packet, buf: ByteBuffer) {
        buf.putInt(packet.requestId)
        buf.putInt(packet.type.value)
        buf.put(charset.encode(packet.payload))
        buf.put(0x00.toByte())
        buf.put(0x00.toByte())
    }

    fun decode(buf: ByteBuffer, length: Int): Packet {
        val requestId: Int = buf.int
        val packetType: PacketType = PacketType.fromValue(buf.int)
        val limit: Int = buf.limit()
        buf.limit(buf.position() + length - 10)
        val payload = charset.decode(buf).toString()
        buf.limit(limit)
        buf.get()
        buf.get()
        return Packet(requestId, packetType, payload)
    }
}
