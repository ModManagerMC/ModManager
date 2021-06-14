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

import java.util.List;

/**
 * A short representation for Mods used to
 */
public record SummarizedMod(
        String id,
        String name,
        List<String> latest,
        String description,
        String icon
) {
//    public CompletableFuture<NativeImageBackedTexture> getIcon() {
//        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(icon)).build();
//        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenCompose(response -> {
//            try {
//                NativeImage image = NativeImage.read(response.body());
//                NativeImageBackedTexture icon = new NativeImageBackedTexture(image);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        return null;
//    }
}
