package xyz.deathsgun.modmanager.tasks;

import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.NetworkTask;
import xyz.deathsgun.modmanager.api.manipulation.TaskCallback;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.util.FabricMods;
import xyz.deathsgun.modmanager.util.InstallationUtil;
import xyz.deathsgun.modmanager.util.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ModUpdateTask extends NetworkTask {

    public ModUpdateTask(String taskId, SummarizedMod mod, TaskCallback taskCallback) {
        super(taskId, mod, taskCallback);
        logger = LogManager.getLogger("Mod remover");
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected void execute() throws Exception {
        if (subject == null) {
            throw new Exception("Summarized mod is empty");
        }
        Optional<ModContainer> container = FabricMods.getModContainerByMod(subject);
        String id, name;
        if (container.isPresent()) {
            id = container.get().getMetadata().getId();
            name = container.get().getMetadata().getName();
        } else {
            debug("Getting mod id from summarized mod (this may fail)");
            id = subject.slug();
            name = subject.name();
        }
        Path jar = FabricMods.getJarFromModContainer(id, name);
        if (jar == null) {
            throw new Exception(String.format("Couldn't find jar for %s", subject.name()));
        }
        if (InstallationUtil.getCurrentOS() != OS.WINDOWS) {
            Files.delete(jar);
        } else {
            jar.toFile().deleteOnExit();
        }
        InstallationUtil.downloadMod(http, InstallationUtil.getVersionForMod(subject));
        ModManager.getModManipulationManager().markManuallyUpdated(subject);
        ModManager.getUpdateChecker().removeUpdate(subject.id());
    }
}
