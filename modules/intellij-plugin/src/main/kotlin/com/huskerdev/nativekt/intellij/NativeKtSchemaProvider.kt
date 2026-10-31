package com.huskerdev.nativekt.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.*

private const val FILE_PREFIX = $$"# $scheme:nativekt"

class NativeKtSchemaProvider : JsonSchemaProviderFactory {
    override fun getProviders(
        project: Project
    ): List<JsonSchemaFileProvider> = listOf(
        object : JsonSchemaFileProvider {
            override fun isAvailable(file: VirtualFile): Boolean =
                file.inputStream.reader().useLines {
                    it.firstOrNull()?.trim()?.replace("\\s+".toRegex()," ") == FILE_PREFIX
                }

            override fun getName(): String = "Native-kt"

            override fun getSchemaType(): SchemaType =
                SchemaType.embeddedSchema

            override fun getSchemaFile(): VirtualFile? =
                JsonSchemaProviderFactory.getResourceFile(
                    NativeKtSchemaProvider::class.java,
                    "/com/huskerdev/nativekt/intellij/schemas/nativekt.schema.json"
                )
        }
    )
}
