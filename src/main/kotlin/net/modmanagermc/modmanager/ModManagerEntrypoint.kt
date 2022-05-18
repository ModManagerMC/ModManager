package net.modmanagermc.modmanager

import net.fabricmc.api.ClientModInitializer
import net.modmanagermc.core.Core
import net.modmanagermc.core.update.IUpdateService

object ModManagerEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        Core.init()
        val updateService: IUpdateService by Core.di
        updateService.checkUpdate()
    }
}