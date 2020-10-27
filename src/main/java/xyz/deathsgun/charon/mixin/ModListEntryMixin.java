/*
 * Copyright (C) 2020 DeathsGun
 * deathsgun@protonmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.deathsgun.charon.mixin;

import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.util.BadgeType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.deathsgun.charon.CharonClient;
import xyz.deathsgun.charon.utils.BadgeUtil;

@Mixin(ModListEntry.class)
public abstract class ModListEntryMixin extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {

    @Shadow
    @Final
    protected MinecraftClient client;
    private boolean outdated = false;

    @Shadow
    public abstract ModMetadata getMetadata();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(ModContainer container, ModListWidget list, CallbackInfo ci) {
        outdated = CharonClient.getService().isModOutdated(getMetadata().getId(), getMetadata().getVersion().getFriendlyString());
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY,
                         boolean isSelected, float delta, CallbackInfo ci) {
        if (outdated) {
            TextRenderer font = this.client.textRenderer;
            BadgeUtil.drawBadge(BadgeType.DEPRECATED, new TranslatableText("charon.badge.outdated"),
                    BadgeUtil.calculateStartPos(getMetadata().getId(), x + 32 + 3 + font.getWidth(getMetadata().getName()) + 2), y,
                    x + rowWidth, matrices, mouseX, mouseY);
        }
    }

}
