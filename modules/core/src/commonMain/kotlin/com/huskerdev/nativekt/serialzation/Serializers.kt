package com.huskerdev.nativekt.serialzation

import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.stdlibx.fs.SyncPath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer


object ContextSerializer: KSerializer<NativeModuleContext> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.huserdev.nativekt.NativeModuleContext") {
        element<Int>("module")
        element<String>("build_dir")
    }

    override fun serialize(encoder: Encoder, value: NativeModuleContext) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, serializer(), value.module)
            encodeStringElement(descriptor, 1, value.buildDir.absoluteString())
        }
    }

    override fun deserialize(decoder: Decoder): NativeModuleContext {
        return decoder.decodeStructure(descriptor) {
            lateinit var module: NativeKtModule
            lateinit var buildDir: String

            @OptIn(ExperimentalSerializationApi::class)
            if (decodeSequentially()) {
                module = decodeSerializableElement(descriptor, 0, serializer())
                buildDir = decodeStringElement(descriptor, 1)
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> module = decodeSerializableElement(descriptor, 0, serializer())
                    1 -> buildDir = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            NativeModuleContext(
                module = module,
                buildDir = SyncPath(buildDir),
                executor = null,
                logger = null
            )
        }
    }
}