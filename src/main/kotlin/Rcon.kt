/*
 * Copyright 2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.minecraft

import java.io.Closeable
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ByteChannel
import java.nio.channels.SocketChannel

/**
 * ## 支持`Rcon`协议的客户端
 *
 * 使用方法:
 * ```
 * // 创建 Rcon
 * val rcon = Rcon.open("127.0.0.1", 25575)
 * // 验证密码
 * rcon.authenticate("12345678")
 * // 发送命令
 * rcon.sendCommand("list")
 * ```
 */
class Rcon(private val channel: ByteChannel, private val codec: PacketCodec) : Closeable {

    private val readerBuffer = ByteBuffer.allocate(4110).order(ByteOrder.LITTLE_ENDIAN)
    private val writerBuffer = ByteBuffer.allocate(1460).order(ByteOrder.LITTLE_ENDIAN)

    @Volatile
    private var requestCounter = 0

    /**
     * ## Rcon协议认证
     *
     * @param password 密码
     */
    fun authenticate(password: String): Boolean {
        var response: Packet
        synchronized(this) {
            response = writeAndRead(PacketType.SERVERDATA_AUTH, password)
            if (response.type == PacketType.SERVERDATA_RESPONSE_VALUE) {
                response = read(response.requestId)
            }
        }
        if (response.type != PacketType.SERVERDATA_AUTH_RESPONSE) {
            throw IOException("Invalid authentication response type: " + response.type)
        }
        return response.isValid
    }

    /**
     * ## 发送命令
     *
     * @param command 命令
     */
    fun sendCommand(command: String): String {
        val response = writeAndRead(PacketType.SERVERDATA_EXECCOMMAND, command)
        if (response.type != PacketType.SERVERDATA_RESPONSE_VALUE) {
            throw IOException("Bad command response type: " + response.type)
        }
        if (!response.isValid) {
            throw IOException("Invalid command response: " + response.payload)
        }
        return response.payload
    }

    /**
     * ## 写入数据包后读取数据包
     *
     * @param packetType 数据包类型
     * @param payload 数据包有效载荷
     * @return
     */
    @Synchronized
    private fun writeAndRead(packetType: PacketType, payload: String): Packet {
        val requestId = requestCounter++
        write(Packet(requestId, packetType, payload))
        return read(requestId)
    }

    /**
     * ## 写数据包
     *
     * @param packet 数据包
     * @return
     */
    @Synchronized
    private fun write(packet: Packet): Int {
        if (packet.payload.length > 1460) throw IllegalArgumentException("Packet payload is too large")

        writerBuffer.clear()
        writerBuffer.position(Int.SIZE_BYTES)
        codec.encode(packet, writerBuffer)
        writerBuffer.putInt(0, writerBuffer.position() - Int.SIZE_BYTES)
        writerBuffer.flip()
        return channel.write(writerBuffer)
    }

    /**
     * ## 读数据包
     *
     * @param expectedRequestId 预期的请求 ID
     * @return
     */
    @Synchronized
    private fun read(expectedRequestId: Int): Packet {
        // 读取数据包长度
        readUntilAvailable(readerBuffer, Int.SIZE_BYTES)
        readerBuffer.flip()
        val length = readerBuffer.int
        readerBuffer.compact()

        // 读取包
        readUntilAvailable(readerBuffer, length)
        readerBuffer.flip()
        val packet = codec.decode(readerBuffer, length)
        readerBuffer.compact()

        // 判断是否验证和请求id是否正确
        if (packet.isValid && packet.requestId != expectedRequestId) {
            throw IOException("Unexpected response ID ($expectedRequestId -> ${packet.requestId})")
        }
        return packet
    }

    private fun readUntilAvailable(buffer: ByteBuffer, bytesAvailable: Int) {
        while (buffer.position() < bytesAvailable) {
            if (channel.read(buffer) == -1) {
                throw EOFException()
            }
        }
    }

    override fun close() {
        channel.close()
    }

    companion object {
        fun open(ip: String, port: Int): Rcon {
            return Rcon(SocketChannel.open(InetSocketAddress(ip, port)), PacketCodec())
        }
    }

}
