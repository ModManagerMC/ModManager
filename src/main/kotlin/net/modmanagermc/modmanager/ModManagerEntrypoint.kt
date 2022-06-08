package net.modmanagermc.modmanager

import net.fabricmc.api.ClientModInitializer
import net.minecraft.util.Language
import net.modmanagermc.core.Core
import net.modmanagermc.core.update.IUpdateService
import net.modmanagermc.modmanager.icon.IconCache

object ModManagerEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        Core.init()
        Core.di.bind<IconCache> { IconCache() }
        val cache: IconCache by Core.di
        cache.cleanupCache()
        val updateService: IUpdateService by Core.di
        updateService.checkUpdate()
    }
}