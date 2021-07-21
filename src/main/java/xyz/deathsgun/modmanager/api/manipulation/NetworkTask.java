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

package xyz.deathsgun.modmanager.api.manipulation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public abstract class NetworkTask extends ManipulationTask {

    protected final HttpClient http;

    public NetworkTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable TaskCallback taskCallback) {
        super(id, subject, taskCallback);
        this.http = HttpClient.newHttpClient();
    }

    public HttpRequest build(HttpRequest.Builder builder) {
        return builder.setHeader("User-Agent", "ModManager " + ModManager.getVersion()).build();
    }

}
