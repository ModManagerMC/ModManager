package xyz.deathsgun.modmanager.api

import net.minecraft.text.TranslatableText

sealed class ModInstallResult {

    object Success : ModInstallResult()
    data class Error(val text: TranslatableText, val cause: Exception? = null) : ModInstallResult()
}
