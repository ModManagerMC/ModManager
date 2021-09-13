package xyz.deathsgun.modmanager.api

import net.minecraft.text.TranslatableText

sealed class ModRemoveResult {
    object Success : ModRemoveResult()
    data class Error(val text: TranslatableText, val cause: Exception? = null) : ModRemoveResult()
}
