package com.huskerdev.nativekt.intellij

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon


class NdlFileType private constructor() : LanguageFileType(NdlLanguage.INSTANCE) {
    companion object {
        @Suppress("unused")
        @JvmStatic
        val INSTANCE = NdlFileType()
    }

    override fun getName(): String =
        "NDL File"

    override fun getDescription(): String =
        "Native-kt description language file"

    override fun getDefaultExtension(): String =
        "ndl"

    override fun getIcon(): Icon =
        AllIcons.FileTypes.WsdlFile
}