package xyz.deathsgun.modmanager.api

import net.minecraft.text.TranslatableText

sealed class ModUpdateResult {
    object Success : ModUpdateResult()
    data class Error(val text: TranslatableText, val cause: Exception? = null) : ModUpdateResult()
}
