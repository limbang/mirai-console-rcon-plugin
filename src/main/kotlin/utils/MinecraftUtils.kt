/*
 * Copyright 2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.minecraft.utils

/**
 * 以前面加空格的方式拼接成命令
 *
 * @return
 */
fun Array<out String>.toCommand(): String {
    var command = ""
    forEach {
        command += " $it"
    }
    return command.trim()
}