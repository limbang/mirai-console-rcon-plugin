/*
 * Copyright 2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

import top.limbang.minecraft.Rcon
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RconTest {

    lateinit var rcon: Rcon

    @BeforeTest
    fun connect() {
        rcon = Rcon.open("127.0.0.1", 25575)
    }

    @Test
    fun send() {
        println(rcon.authenticate("12345678"))
        println(rcon.sendCommand("list"))
    }

    @AfterTest
    fun close() {
        rcon.close()
    }
}