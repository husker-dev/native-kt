package com.huskerdev.nativekt.intellij

import com.intellij.lang.Language


class NdlLanguage : Language("NDL") {
    companion object {
        val INSTANCE = NdlLanguage()
    }
}