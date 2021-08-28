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

package xyz.deathsgun.modmanager.providers.modrinth.models

import kotlinx.serialization.Serializable
import net.minecraft.text.TranslatableText
import xyz.deathsgun.modmanager.api.mod.Category
import xyz.deathsgun.modmanager.api.mod.Mod

@Serializable
data class SearchResult(
    val hits: List<ModResult>
) {
    fun toList(): List<Mod> {
        val mods = ArrayList<Mod>()
        for (mod in hits) {
            val categoriesList = ArrayList<Category>()
            mod.categories.forEach { categoryId ->
                categoriesList.add(
                    Category(
                        categoryId,
                        TranslatableText("modmanager.category.${categoryId}")
                    )
                )
            }
            mods.add(
                Mod(
                    mod.id,
                    mod.slug,
                    mod.author,
                    mod.title,
                    mod.description,
                    mod.iconUrl,
                    null,
                    null,
                    categoriesList,
                )
            )
        }
        return mods
    }
}
