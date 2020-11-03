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

package xyz.deathsgun.charon.service;

import com.vdurmont.semver4j.Semver;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.fabricmc.loader.util.version.SemanticVersionPredicateParser;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xyz.deathsgun.charon.model.Artifact;
import xyz.deathsgun.charon.model.Mod;
import xyz.deathsgun.charon.service.db.IDatabase;
import xyz.deathsgun.charon.service.db.ILocalStorage;
import xyz.deathsgun.charon.service.db.LocalStorage;
import xyz.deathsgun.charon.service.db.SQLiteDatabase;
import xyz.deathsgun.charon.service.workers.InstallThread;
import xyz.deathsgun.charon.service.workers.RemoveThread;
import xyz.deathsgun.charon.service.workers.SyncThread;
import xyz.deathsgun.charon.service.workers.UpdateThread;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class CharonService {

    private final HashMap<String, ProcessingType> processes = new HashMap<>();
    private final HashMap<String, Exception> errors = new HashMap<>();
    private final Logger logger = LogManager.getLogger();
    private final IDatabase database;
    private final ILocalStorage storage;

    public CharonService() {
        try {
            this.database = new SQLiteDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.storage = new LocalStorage();
    }

    public void update() {
        logger.info("Synchronizing with upstream...");
        new SyncThread(this);
    }

    public List<Mod> queryMods(String name) {
        return this.database.queryMods(name);
    }

    public boolean isModOutdated(Mod mod) {
        ModMetadata metadata = getMetadata(mod);
        if (metadata == null)
            return false;
        return isModOutdated(metadata.getId(), metadata.getVersion().getFriendlyString());
    }

    private ModMetadata getMetadata(Mod mod) {
        ModContainer container = FabricLoader.getInstance().getModContainer(mod.id).orElse(null);
        if (container == null)
            return null;
        return container.getMetadata();
    }

    public boolean isModOutdated(String id, String version) {
        Mod dbMod = this.database.getModById(id);
        if (dbMod == null)
            return false;
        return getLatestCompatibleVersion(dbMod, version) != null;
    }

    public Artifact getLatestCompatibleVersion(Mod mod, String version) {
        version = version.replaceAll("\\+build", "");
        SemanticVersionImpl minecraft;
        try {
            minecraft = new SemanticVersionImpl(SharedConstants.getGameVersion().getId(), false);
        } catch (VersionParsingException e) {
            e.printStackTrace();
            return null;
        }
        List<Artifact> artifacts = mod.artifacts;
        Semver latest = null;
        Artifact latestArtifact = null;
        for (Artifact artifact : artifacts) {
            if (artifact.compatibility == null || artifact.compatibility.isEmpty())
                continue;
            try {
                Predicate<SemanticVersionImpl> compatibility = SemanticVersionPredicateParser.create(artifact.compatibility);
                if (compatibility.test(minecraft)) {
                    Semver ver = new Semver(artifact.version.replaceAll("\\+build", ""), Semver.SemverType.LOOSE);
                    if (latest == null && ver.isGreaterThan(version)) {
                        latest = ver;
                        latestArtifact = artifact;
                    } else if (latest != null && ver.isGreaterThan(latest)) {
                        latest = ver;
                        latestArtifact = artifact;
                    }
                }
            } catch (VersionParsingException e) {
                logger.warn(e.getMessage());
            }
        }
        return latestArtifact;
    }

    public void installMod(CharonActionCallback callback, @NotNull Mod mod) {
        logger.info("Installing {}", mod.id);
        processes.put(mod.id, ProcessingType.INSTALL);
        new InstallThread(this, callback, mod);
    }

    public ProcessingType getProcessTypes(@NotNull Mod mod) {
        return processes.getOrDefault(mod.id, ProcessingType.NONE);
    }

    public boolean isModInstalled(Mod mod) {
        return storage.isModInstalled(mod.id);
    }

    public void updateMod(CharonActionCallback callback, Mod mod) {
        logger.info("Updating {}", mod.id);
        this.processes.put(mod.id, ProcessingType.UPDATE);
        new UpdateThread(this, callback, mod);
    }

    public void removeMod(CharonActionCallback callback, Mod mod) {
        logger.info("Removing {}", mod.id);
        this.processes.put(mod.id, ProcessingType.REMOVE);
        new RemoveThread(this, callback, mod);
    }

    public void removeProcess(Mod mod) {
        this.processes.remove(mod.id);
    }

    public void setProcessErrored(Mod mod, Exception e) {
        this.processes.put(mod.id, ProcessingType.ERRORED);
        this.errors.put(mod.id, e);
    }

    public Exception getError(Mod mod) {
        return this.errors.get(mod.id);
    }

    public ILocalStorage getLocalStorage() {
        return storage;
    }

    public List<Mod> getCompatibleMods() {
        return database.getMods();
    }

    public IDatabase getDatabase() {
        return database;
    }
}
