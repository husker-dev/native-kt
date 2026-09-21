package com.huskerdev.nativekt.utils

import io.github.vinceglb.filekit.*
import kotlinx.io.*

/**
 * Globalizes (makes external) the specified local symbols in a Mach-O object file
 * by setting the N_EXT bit (0x01) in each matching nlist64.n_type.
 *
 * This replaces `objcopy --globalize-symbols` which is unavailable on macOS.
 */
internal fun globalizeMachOSymbols(file: PlatformFile, symbols: List<String>) {
    if (symbols.isEmpty()) return

    val content = file.source().buffered().use { it.readByteArray() }

    val is64 = when (content.getUIntLe(0)) {
        0xFEEDFACFu -> true
        0xFEEDFACEu -> false
        else -> throw IllegalArgumentException("Not a Mach-O file: ${file.name}")
    }

    val headerSize = if (is64) 32 else 28
    val nlistSize = if (is64) 16 else 12
    val ncmds = content.getIntLe(16)

    // Locate LC_SYMTAB (0x02)
    var symoff = 0
    var nsyms = 0
    var stroff = 0
    var strsize = 0

    var cmdOffset = headerSize
    repeat(ncmds) {
        val cmd = content.getIntLe(cmdOffset)
        val cmdsize = content.getIntLe(cmdOffset + 4)
        if (cmd == 0x02) {
            symoff = content.getIntLe(cmdOffset + 8)
            nsyms = content.getIntLe(cmdOffset + 12)
            stroff = content.getIntLe(cmdOffset + 16)
            strsize = content.getIntLe(cmdOffset + 20)
        }
        cmdOffset += cmdsize
    }

    if (symoff == 0) return

    var changed = false
    for (i in 0 until nsyms) {
        val entryOffset = symoff + i * nlistSize
        val nStrx = content.getIntLe(entryOffset)
        val nType = content[entryOffset + 4].toInt() and 0xFF

        // Skip if already external
        if (nType and 0x01 != 0) continue
        if (nStrx < 1 || nStrx >= strsize) continue

        // Read null-terminated symbol name from string table
        val nameStart = stroff + nStrx
        var nameEnd = nameStart
        while (nameEnd < content.size && content[nameEnd].toInt() != 0) nameEnd++

        if (content.copyOfRange(nameStart, nameEnd).decodeToString() in symbols) {
            content[entryOffset + 4] = (nType or 0x01).toByte()
            changed = true
        }
    }

    if (changed) {
        // Write the modified image back through a kotlinx-io sink.
        file.sink().buffered().use { it.write(content) }
    }
}

private fun ByteArray.getIntLe(offset: Int): Int =
    (get(offset).toInt() and 0xFF) or
        ((get(offset + 1).toInt() and 0xFF) shl 8) or
        ((get(offset + 2).toInt() and 0xFF) shl 16) or
        ((get(offset + 3).toInt() and 0xFF) shl 24)

private fun ByteArray.getUIntLe(offset: Int): UInt = getIntLe(offset).toUInt()