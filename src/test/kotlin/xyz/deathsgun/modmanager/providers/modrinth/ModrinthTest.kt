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

package xyz.deathsgun.modmanager.providers.modrinth

import net.minecraft.text.TranslatableText
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import xyz.deathsgun.modmanager.api.http.CategoriesResult
import xyz.deathsgun.modmanager.api.http.ModResult
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.http.VersionResult
import xyz.deathsgun.modmanager.api.mod.Category
import xyz.deathsgun.modmanager.api.mod.Mod
import xyz.deathsgun.modmanager.api.mod.VersionType
import xyz.deathsgun.modmanager.api.provider.Sorting
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ModrinthTest {

    private val modrinth = Modrinth()

    @Test
    fun getCategories() {
        val result = modrinth.getCategories()
        if (result is CategoriesResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val categories = (result as CategoriesResult.Success).categories
        assertTrue(categories.isNotEmpty())
        categories.forEach {
            assertTrue(it.id.isNotEmpty())
            assertEquals(String.format("modmanager.category.%s", it.id), it.text.key)
        }
    }

    @Test
    fun getModsBySorting() {
        val result = modrinth.getMods(Sorting.NEWEST, 0, 10)
        if (result is ModsResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val mods = (result as ModsResult.Success).mods
        checkMods(mods)
    }

    @Test
    fun getModsByCategory() {
        val result = modrinth.getMods(Category("misc", TranslatableText("")), 0, 10)
        if (result is ModsResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val mods = (result as ModsResult.Success).mods
        checkMods(mods)
    }

    @Test
    fun getModsByQuery() {
        val result = modrinth.getMods("Mod", 0, 10)
        if (result is ModsResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val mods = (result as ModsResult.Success).mods
        checkMods(mods)
    }

    private fun checkMods(mods: List<Mod>) {
        assertTrue(mods.isNotEmpty())
        assertEquals(mods.size, 10)
        mods.forEach {
            assertTrue(it.id.isNotEmpty())
            assertTrue(it.slug.isNotEmpty())
            assertTrue(it.name.isNotEmpty())
            assertTrue(it.author.isNotEmpty())
            assertNotNull(it.iconUrl)
            assertTrue(it.shortDescription.isNotEmpty())
            assertTrue(it.categories.isNotEmpty())

            // Only filled when getMod(id) is called
            assertNull(it.description, "description should be null as it's only loaded by getMod")
            assertNull(it.license, "description should be null as it's only loaded by getMod")
        }
    }

    @Test
    fun getMod() {
        val testMod = modrinth.getMods(Sorting.NEWEST, 0, 1)
        if (testMod is ModsResult.Error) {
            testMod.cause?.let {
                fail(testMod.text.key, it)
            }
            fail(testMod.text.key)
        }
        val result = modrinth.getMod((testMod as ModsResult.Success).mods[0])
        if (result is ModResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val mod = (result as ModResult.Success).mod
        assertTrue(mod.id.isNotEmpty())
        assertTrue(mod.slug.isNotEmpty())
        assertTrue(mod.name.isNotEmpty())
        assertTrue(mod.author.isNotEmpty())
        assertNotNull(mod.iconUrl)
        assertTrue(mod.shortDescription.isNotEmpty())
        assertTrue(mod.categories.isNotEmpty())
        assertNotEquals(mod.description, null)
        assertTrue(mod.description!!.isNotEmpty())
        assertNotEquals(mod.license, null)
        assertTrue(mod.license!!.isNotEmpty())
    }

    @Test
    fun getVersionsForMod() {
        val result = modrinth.getVersionsForMod("6kq7BzRK")
        if (result is VersionResult.Error) {
            result.cause?.let {
                fail(result.text.key, it)
            }
            fail(result.text.key)
        }
        val versions = (result as VersionResult.Success).versions
        assertTrue(versions.isNotEmpty())
        versions.forEach {
            assertTrue(it.gameVersions.isNotEmpty())
            assertContains(it.gameVersions, "1.17.1")
            assertTrue(it.version.isNotEmpty())
            assertTrue(it.changelog.isNotEmpty())
            assertEquals(VersionType.ALPHA, it.type)
            assertTrue(it.assets.isNotEmpty())
            it.assets.forEach { asset ->
                assertTrue(asset.filename.isNotEmpty())
                assertTrue(asset.filename.endsWith(".jar"))
                assertTrue(asset.url.isNotEmpty())
                assertTrue(asset.hashes.isNotEmpty())
                assertContains(asset.hashes, "sha512")
            }
        }
    }
}