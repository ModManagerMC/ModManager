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

package xyz.deathsgun.modmanager.api.provider

import xyz.deathsgun.modmanager.api.http.CategoriesResult
import xyz.deathsgun.modmanager.api.http.ModResult
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.mod.Category
import xyz.deathsgun.modmanager.api.mod.Mod

interface IModProvider : IModUpdateProvider {

    /**
     * Returns a list of all possible mod categories also with an translatable text
     *
     * @return a list of all mod categories
     */
    fun getCategories(): CategoriesResult

    /**
     * Returns a limited number of [Mod]'s sorted
     * in a given way.
     *
     * @param sorting the requested sorting of the mods
     * @param page    the requested from the UI starting at 1
     * @param limit   to not overfill the ui and for shorter loading times the amount of returned mods needs to limited
     * @return a list of sorted mods
     */
    fun getMods(sorting: Sorting, page: Int, limit: Int): ModsResult

    /**
     * Returns a limited number of [Mod]'s from the specified category
     *
     * @param category the category of all the mods
     * @param page     the requested from the UI starting at 1
     * @param limit    to not overfill the ui and for shorter loading times the amount of returned mods needs to limited
     * @return a list of sorted mods
     */
    fun getMods(category: Category, sorting: Sorting, page: Int, limit: Int): ModsResult

    /**
     * Returns a limited number of [Mod]'s from a given search.
     *
     * @param query the search string
     * @param page  the current requested page starts at 0
     * @param limit the amount of mods to return
     * @return a list of mods matching the search term
     */
    fun search(query: String, sorting: Sorting, page: Int, limit: Int): ModsResult

    /**
     * Returns a more detailed representation of the mod
     *
     * @param id the [Mod] id which is used to receive
     * @return a more detailed representation of [Mod]
     */
    fun getMod(id: String): ModResult
}