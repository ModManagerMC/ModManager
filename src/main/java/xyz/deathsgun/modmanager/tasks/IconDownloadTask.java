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

package xyz.deathsgun.modmanager.tasks;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.ErrorHandler;
import xyz.deathsgun.modmanager.api.manipulation.NetworkTask;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IconDownloadTask extends NetworkTask {

    private final HttpClient http = HttpClient.newHttpClient();

    public IconDownloadTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable ErrorHandler errorHandler) {
        super(id, subject, errorHandler);
        logger = LogManager.getLogger("Icon downloader");
    }

    @Override
    protected void execute() throws Exception {
        if (subject == null) {
            throw new Exception("Summarized mod is null");
        }
        debug("Downloading icon for {}", subject.slug());
        HttpRequest request = build(HttpRequest.newBuilder().GET().uri(URI.create(subject.icon())));
        HttpResponse<InputStream> response = this.http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            logger.error("Failed to download icon for {} ! received status code: {}", subject.slug(), response.statusCode());
            throw new Exception(String.format("Received invalid status code: %d", response.statusCode()));
        }
        debug("Reading icon for {}", subject.slug());
        NativeImage image = NativeImage.read(response.body());
        Identifier iconLocation = new Identifier("modmanager", subject.slug() + "_icon");
        ModManager.getIconManager().registerIcon(subject.id(), iconLocation, image);
        debug("Finished downloading icon for {}", subject.slug());
    }
}
