/*
 * Copyright 2021 DeathsGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.deathsgun.modmanager.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.gui.widget.better.BetterListWidget;

import java.util.concurrent.CompletableFuture;

public class ModListEntry extends BetterListWidget.BetterListEntry<ModListEntry> {

    public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
    public static final Identifier LOADING_ICON = new Identifier("modmanager", "textures/gui/loading.png");

    private final SummarizedMod mod;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private Identifier iconLocation;

    public ModListEntry(ModListWidget list, SummarizedMod mod) {
        super(list, new LiteralText(mod.name()));
        this.mod = mod;
        CompletableFuture<NativeImageBackedTexture> icon = mod.getIcon();
    }

    @Override
    public String id() {
        return mod.id();
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int iconSize = 19;
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindIconTexture();
    }


    private void bindIconTexture() {
//        if (this.iconLocation == null) {
//            this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.id() + "_icon");
//            if (icon != null) {
//                this.client.getTextureManager().registerTexture(this.iconLocation, icon);
//            } else {
//                this.iconLocation = UNKNOWN_ICON;
//            }
//        }
//        this.client.getTextureManager().bindTexture(this.iconLocation);
    }

    public SummarizedMod getMod() {
        return mod;
    }
}
