package com.huskerdev.nativekt.jvm

private val time = System.currentTimeMillis()

/**
 * Keeps object usable to prevent dropping by ProGuard, GraalVM Native Image, etc...
 */
fun keep(vararg obj: Any) {
    if(time == Long.MIN_VALUE)
        println(obj.toList())
}