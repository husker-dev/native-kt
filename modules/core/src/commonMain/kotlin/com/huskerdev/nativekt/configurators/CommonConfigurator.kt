package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.*

fun prepareCommon(
    context: NativeModuleContext
) {
    val layout = DirectoryLayout.of(context, null)
    KotlinCommonPrinter(
        context = context,
        target = layout.kotlinSourceFile
    )
}