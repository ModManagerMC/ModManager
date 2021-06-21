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

import net.minecraft.client.MinecraftClient;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.Category;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.api.provider.IModProvider;
import xyz.deathsgun.modmanager.downloader.IconDownloader;
import xyz.deathsgun.modmanager.gui.ModsOverviewScreen;
import xyz.deathsgun.modmanager.gui.widget.better.BetterListWidget;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class ModListWidget extends BetterListWidget<ModListEntry> {

    private final int limit = 20;
    private final IconDownloader iconDownloader = new IconDownloader();
    private final ArrayList<SummarizedMod> mods = new ArrayList<>();
    private int page = 0;
    private Category category;

    public ModListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, ModsOverviewScreen parentScreen) {
        super(client, width, height, top, bottom, itemHeight, parentScreen);
        setScrollAmount(0.0 * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
    }

    @Override
    public void init() {
    }

    public void setCategory(Category category, boolean force) {
        setScrollAmount(0.0 * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
        if (this.category != null && Objects.equals(this.category.id(), category.id()) && !force) {
            return;
        }
        this.category = category;
        this.mods.clear();
        for (int i = 0; i < this.getEntryCount(); i++) {
            this.remove(i);
        }
        this.clearEntries();
        try {
            Optional<IModProvider> provider = ModManager.getModProvider();
            if (provider.isEmpty()) {
                return;
            }
            IModProvider modProvider = provider.get();
            modProvider.getMods(category, page, limit).forEach(mod -> this.addEntry(new ModListEntry(this, iconDownloader, mod)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int addEntry(ModListEntry entry) {
        if (mods.contains(entry.getMod())) {
            return 0;
        }
        mods.add(entry.getMod());
        int i = super.addEntry(entry);
        if (entry.id().equals(selectedId)) {
            setSelected(entry);
        }
        return i;
    }

    @Override
    protected boolean removeEntry(ModListEntry entry) {
        mods.remove(entry.getMod());
        iconDownloader.destroyIcon(entry.getMod());
        return super.removeEntry(entry);
    }

    @Override
    protected ModListEntry remove(int index) {
        ModListEntry entry = getEntry(index);
        mods.remove(entry.getMod());
        iconDownloader.destroyIcon(entry.getMod());
        return super.remove(index);
    }

    public void showNextPage() {
        this.page++;
        this.setCategory(category, true);
    }
}
