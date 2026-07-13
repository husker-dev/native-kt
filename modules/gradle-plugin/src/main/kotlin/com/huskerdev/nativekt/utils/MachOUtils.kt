package com.huskerdev.nativekt.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Globalizes (makes external) the specified local symbols in a Mach-O object file
 * by setting the N_EXT bit (0x01) in each matching nlist64.n_type.
 *
 * This replaces `objcopy --globalize-symbols` which is unavailable on macOS.
 */
internal fun globalizeMachOSymbols(file: File, symbols: List<String>) {
    if (symbols.isEmpty()) return

    RandomAccessFile(file, "rw").use { raf ->
        val channel = raf.channel
        val buf = ByteBuffer.allocate(channel.size().toInt()).order(ByteOrder.LITTLE_ENDIAN)
        channel.read(buf)
        buf.flip()

        val magic = buf.getInt(0)
        val is64 = when (magic) {
            0xFEEDFACF.toInt() -> true
            0xFEEDFACE.toInt() -> false
            else -> throw IllegalArgumentException("Not a Mach-O file: ${file.name}")
        }

        val headerSize = if (is64) 32 else 28
        val nlistSize = if (is64) 16 else 12
        val ncmds = buf.getInt(16)

        // Find LC_SYMTAB (0x02)
        var symoff = 0
        var nsyms = 0
        var stroff = 0
        var strsize = 0

        var cmdOffset = headerSize
        repeat(ncmds) {
            val cmd = buf.getInt(cmdOffset)
            val cmdsize = buf.getInt(cmdOffset + 4)
            if (cmd == 0x02) {
                symoff = buf.getInt(cmdOffset + 8)
                nsyms = buf.getInt(cmdOffset + 12)
                stroff = buf.getInt(cmdOffset + 16)
                strsize = buf.getInt(cmdOffset + 20)
            }
            cmdOffset += cmdsize
        }

        if (symoff == 0) return

        val array = buf.array()
        for (i in 0 until nsyms) {
            val entryOffset = symoff + i * nlistSize
            val nStrx = buf.getInt(entryOffset)
            val nType = buf.get(entryOffset + 4).toInt() and 0xFF

            // Skip if already external
            if (nType and 0x01 != 0) continue
            if (nStrx <= 0 || nStrx >= strsize) continue

            // Read null-terminated symbol name from string table
            val nameStart = stroff + nStrx
            var nameEnd = nameStart
            while (nameEnd < buf.limit() && array[nameEnd].toInt() != 0) nameEnd++
            val name = String(array, nameStart, nameEnd - nameStart, Charsets.US_ASCII)

            if (name in symbols) {
                raf.seek((entryOffset + 4).toLong())
                raf.write(nType or 0x01)
            }
        }
    }
}
