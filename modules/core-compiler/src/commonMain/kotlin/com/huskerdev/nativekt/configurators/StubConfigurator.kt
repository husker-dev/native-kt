package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.DirectoryLayout
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.printers.kotlin.KotlinStubPrinter

fun prepareStub(
    context: NativeModuleContext,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)
    KotlinStubPrinter(
        context = context,
        target = layout.kotlinSourceFile
    )
}