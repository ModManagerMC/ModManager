package xyz.deathsgun.modmanager.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionDeserializer;
import net.minecraft.MinecraftVersion;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.Asset;
import xyz.deathsgun.modmanager.api.mod.ModVersion;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.api.provider.IModProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstallationUtil {

    public static ModVersion getVersionForMod(SummarizedMod mod) throws Exception {
        IModProvider provider = ModManager.getModProvider();
        List<ModVersion> versions = provider.getVersionsForMod(mod.id()).stream()
                .filter(value -> value.gameVersions().contains(MinecraftVersion.GAME_VERSION.getReleaseTarget())).collect(Collectors.toList());
        ModVersion latest = null;
        SemanticVersion latestVersion = null;
        for (ModVersion modVersion : versions) {
            if (!modVersion.gameVersions().contains(MinecraftVersion.GAME_VERSION.getReleaseTarget())) {
                continue;
            }
            SemanticVersion version = VersionDeserializer.deserializeSemantic(modVersion.version());
            if (latestVersion == null || version.compareTo(latestVersion) > 0) {
                latest = modVersion;
                latestVersion = version;
            }
        }
        return latest;
    }

    public static void downloadMod(HttpClient http, ModVersion version) throws Exception {
        if (version == null) {
            throw new Exception("no version found!");
        }
        Optional<Asset> asset = version.assets().stream().filter(value -> value.filename().endsWith(".jar")).findFirst();
        if (asset.isEmpty()) {
            throw new Exception("jar in downloadable assets found");
        }
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(asset.get().url())).build();
        Path output = FabricLoader.getInstance().getGameDir().resolve("mods").resolve(asset.get().filename());
        HttpResponse<Path> response = http.send(request, HttpResponse.BodyHandlers.ofFile(output));
        if (response.statusCode() != 200) {
            throw new Exception("Invalid status code: " + response.statusCode());
        }
    }

    public static OS getCurrentOS() {
        String name = System.getProperty("os.name");
        if (name.contains("win")) {
            return OS.WINDOWS;
        }
        return name.contains("mac") ? OS.MACOS : OS.LINUX;
    }

}
