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

package xyz.deathsgun.modmanager.api.mod;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A short representation for Mods used to
 */
public record SummarizedMod(
        String id,
        String slug,
        String name,
        List<String> latest,
        String description,
        String icon
) {
    public CompletableFuture<NativeImageBackedTexture> getIcon() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(icon)).build();
        return HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenCompose(response -> {
            try {
                NativeImage image = NativeImage.read(response.body());
                NativeImageBackedTexture icon = new NativeImageBackedTexture(image);
                return CompletableFuture.completedFuture(icon);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }
}
