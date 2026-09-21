package com.huskerdev.nativekt.serialzation

import com.huskerdev.nativekt.*
import io.github.vinceglb.filekit.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*

val nativektSerializersModule = SerializersModule {
    polymorphic(NativeProject::class) {
        subclass(Multiplatform::class)
        subclass(SinglePlatform::class)
    }
    polymorphic(NativeKtConfiguration.Impl::class) {
        subclass(NativeKtMultiplatformConfiguration.Impl::class)
        subclass(NativeKtNativeConfiguration.Impl::class)
        subclass(NativeKtAndroidConfiguration.Impl::class)
        subclass(NativeKtJsConfiguration.Impl::class)
        subclass(NativeKtJvmConfiguration.Impl::class)
    }
}

val nativektJson = Json {
    serializersModule = nativektSerializersModule
}

object ContextSerializer: KSerializer<NativeModuleContext> {
    private val configurationPolymorphicSerializer = PolymorphicSerializer(NativeKtConfiguration.Impl::class)
    private val modulePolymorphicSerializer = PolymorphicSerializer(NativeProject::class)

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("User") {
        element<Int>("configuration")
        element<Int>("module")
        element<String>("build_dir")
        element<String>("src_dir")
    }

    override fun serialize(encoder: Encoder, value: NativeModuleContext) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, configurationPolymorphicSerializer, value.configuration as NativeKtConfiguration.Impl)
            encodeSerializableElement(descriptor, 1, modulePolymorphicSerializer, value.module)
            encodeStringElement(descriptor, 2, value.buildDir.absolutePath())
        }
    }

    override fun deserialize(decoder: Decoder): NativeModuleContext {
        return decoder.decodeStructure(descriptor) {
            lateinit var configuration: NativeKtConfiguration
            lateinit var module: NativeProject
            lateinit var buildDir: String

            @OptIn(ExperimentalSerializationApi::class)
            if (decodeSequentially()) {
                configuration = decodeSerializableElement(descriptor, 0, configurationPolymorphicSerializer) as NativeKtConfiguration
                module = decodeSerializableElement(descriptor, 1, modulePolymorphicSerializer)
                buildDir = decodeStringElement(descriptor, 2)
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> configuration = decodeSerializableElement(descriptor, 0, configurationPolymorphicSerializer) as NativeKtConfiguration
                    1 -> module = decodeSerializableElement(descriptor, 1, modulePolymorphicSerializer)
                    2 -> buildDir = decodeStringElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            NativeModuleContext(
                configuration = configuration,
                module = module,
                buildDir = PlatformFile(buildDir),
                executor = null,
                logger = null
            )
        }
    }
}