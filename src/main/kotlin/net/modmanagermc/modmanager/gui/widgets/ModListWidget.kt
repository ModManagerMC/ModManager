package net.modmanagermc.modmanager.gui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import com.terraformersmc.modmenu.util.DrawingUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import net.minecraft.util.Identifier
import net.minecraft.util.Language
import net.modmanagermc.core.Core
import net.modmanagermc.core.image.IImageService
import net.modmanagermc.core.image.ImageState
import net.modmanagermc.core.mod.IModService
import net.modmanagermc.core.mod.State
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen

class ModListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parent: IListScreen
) : ListWidget<ModListWidget.Entry>(client, width, height, top, bottom, itemHeight, parent) {

    fun setMods(list: List<Mod>) {
        for (child in children()) {
            child.close()
        }
        replaceEntries(list.map { Entry(this, it) })
    }

    fun close() {
        for (child in children()) {
            child.close()
        }
    }

    class Entry(list: ListWidget<Entry>, private val mod: Mod) : ListWidget.Entry<Entry>(list, mod.id) {

        private val unknownIcon = Identifier("minecraft", "textures/misc/unknown_pack.png")
        private val loadingIcon = Identifier("modmanager", "textures/gui/loading.png")
        private var modIcon: Identifier? = null
        private val imageService: IImageService by Core.di
        private val modService: IModService by Core.di

        init {
            imageService.downloadImage(mod.iconUrl)
        }

        override fun render(
            matrices: MatrixStack?,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            val client = MinecraftClient.getInstance()
            val iconSize = 32
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            bindIcon(mod)
            RenderSystem.enableBlend()
            DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize)
            RenderSystem.disableBlend()
            val name: Text = LiteralText(mod.name)
            var trimmedName: StringVisitable = name
            var maxNameWidth = entryWidth - iconSize - 3
            val font = client.textRenderer
            var primaryColor = 0xFFFFFF
            var secondaryColor = 0xFFFFFF
            var badgeText: OrderedText? = null
            val state = modService.getModState(mod.id)
            if (state == State.INSTALLED) {
                primaryColor = 0xff0e2a55.toInt()
                secondaryColor = 0xff2b4b7c.toInt()
                badgeText = TranslatableText("modmanager.badge.installed").asOrderedText()
                maxNameWidth -= font.getWidth(badgeText) + 6
            } else if (state == State.OUTDATED) {
                primaryColor = 0xff530C17.toInt()
                secondaryColor = 0xff841426.toInt()
                badgeText = TranslatableText("modmanager.badge.outdated").asOrderedText()
                maxNameWidth -= font.getWidth(badgeText) + 6
            }

            val textWidth = font.getWidth(name)
            if (textWidth > maxNameWidth) {
                val ellipsis = StringVisitable.plain("...")
                trimmedName =
                    StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis)
            }
            font.draw(
                matrices,
                Language.getInstance().reorder(trimmedName),
                (x + iconSize + 3).toFloat(),
                (y + 1).toFloat(),
                0xFFFFFF
            )
            if (badgeText != null) {
                DrawingUtil.drawBadge(
                    matrices,
                    x + iconSize + 3 + textWidth + 3,
                    y + 1,
                    font.getWidth(badgeText) + 6,
                    badgeText,
                    secondaryColor,
                    primaryColor,
                    0xFFFFFF
                )
            }

            DrawingUtil.drawWrappedString(
                matrices,
                mod.description,
                (x + iconSize + 3 + 4),
                (y + client.textRenderer.fontHeight + 4),
                entryWidth - iconSize - 7,
                2,
                0x808080
            )
        }

        override fun getNarration(): Text {
            return LiteralText(mod.name)
        }


        private fun bindIcon(mod: Mod) {
            var identifier = when(imageService.getImageState(mod.iconUrl)) {
                ImageState.ERRORED -> unknownIcon
                ImageState.DOWNLOADING -> loadingIcon
                ImageState.DOWNLOADED -> modIcon
            }
            if (identifier != null) {
                RenderSystem.setShaderTexture(0, identifier)
                return
            }
            try {
                val image = NativeImage.read(imageService.openImage(mod.iconUrl))
                identifier = Identifier("modmanager", "textures/mod/icons/${mod.id.lowercase()}")
                MinecraftClient.getInstance().textureManager.registerTexture(identifier, NativeImageBackedTexture(image))
            } catch (e: Exception) {
                imageService.setImageState(mod.iconUrl, ImageState.ERRORED)
                RenderSystem.setShaderTexture(0, unknownIcon)
                return
            }
            modIcon = identifier
            RenderSystem.setShaderTexture(0, identifier)
        }

        fun close() {
            if (modIcon == null || modIcon == unknownIcon || modIcon == loadingIcon) {
                return
            }
            MinecraftClient.getInstance().execute {
                MinecraftClient.getInstance().textureManager.destroyTexture(modIcon!!)
            }
        }

    }
}