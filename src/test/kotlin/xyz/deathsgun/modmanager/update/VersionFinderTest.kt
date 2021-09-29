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

package xyz.deathsgun.modmanager.update

import net.fabricmc.loader.api.VersionParsingException
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import xyz.deathsgun.modmanager.api.http.VersionResult
import xyz.deathsgun.modmanager.api.provider.IModUpdateProvider
import xyz.deathsgun.modmanager.config.Config
import xyz.deathsgun.modmanager.dummy.DummyModrinthVersionProvider
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class VersionFinderTest {

    private val logger = LogManager.getLogger()
    private val provider: IModUpdateProvider = DummyModrinthVersionProvider()

    @TestFactory
    fun findUpdateByFallback() = listOf(
        Scenario(
            "lithium",
            "mc1.17.1-0.7.4",
            "mc1.17.1-0.7.1",
            Config.UpdateChannel.STABLE,
            "1.17"
        ),
        Scenario(
            "dynamic-fps",
            "v2.0.5",
            "2.0.5",
            Config.UpdateChannel.STABLE,
            "1.17"
        ),
        Scenario(
            "iris",
            "mc1.17.1-1.1.2",
            "mc1.17-v1.1.1",
            Config.UpdateChannel.STABLE,
            "1.17"
        )
    ).map { scenario ->
        dynamicTest("${scenario.mod} ${scenario.expectedVersion} ${scenario.channel}") {
            val versions = when (val result = provider.getVersionsForMod(scenario.mod)) {
                is VersionResult.Error -> fail(result.text.key, result.cause)
                is VersionResult.Success -> result.versions
            }
            val latest = VersionFinder.findUpdateFallback(
                scenario.installedVersion,
                scenario.mcVersion,
                scenario.channel,
                versions
            )
            assertNotNull(latest)
            assertEquals(scenario.expectedVersion, latest.version)
            assertTrue(latest.assets.isNotEmpty())
        }
    }

    @TestFactory
    fun findUpdateByVersionError() = listOf(
        Scenario(
            "lithium",
            "mc1.17.1-0.7.4",
            "mc1.17.1-0.7.1",
            Config.UpdateChannel.STABLE,
            "1.17"
        ),
        Scenario(
            "terra",
            "5.4.1-BETA+40e95073",
            "fabric-5.3.3-BETA+6027c282",
            Config.UpdateChannel.ALL,
            "1.17"
        )
    ).map { scenario ->
        dynamicTest("${scenario.mod} ${scenario.expectedVersion} ${scenario.channel}") {
            val versions = when (val result = provider.getVersionsForMod(scenario.mod)) {
                is VersionResult.Error -> fail(result.text.key, result.cause)
                is VersionResult.Success -> result.versions
            }
            val latest = try {
                VersionFinder.findUpdateByVersion(
                    scenario.installedVersion,
                    scenario.mcVersion,
                    scenario.channel,
                    versions
                )
            } catch (e: VersionParsingException) {
                null
            }
            assertNull(latest)
        }
    }

    @TestFactory
    fun findUpdateByVersion() = listOf(
        Scenario(
            "modmanager",
            "1.0.2+1.17-alpha",
            "1.0.0-alpha",
            Config.UpdateChannel.ALL,
            "1.17"
        ),
        Scenario(
            "terra",
            "5.4.1-BETA+40e95073",
            "5.3.3-BETA+6027c282", // actually fabric-5.3.3-BETA+6027c282
            Config.UpdateChannel.ALL,
            "1.17"
        ),
        Scenario(
            "modmenu",
            "2.0.13",
            "2.0.5",
            Config.UpdateChannel.ALL,
            "1.17"
        )
    ).map { scenario ->
        dynamicTest("${scenario.mod} ${scenario.expectedVersion} ${scenario.channel}") {
            val versions = when (val result = provider.getVersionsForMod(scenario.mod)) {
                is VersionResult.Error -> fail(result.text.key, result.cause)
                is VersionResult.Success -> result.versions
            }
            val latest = VersionFinder.findUpdateByVersion(
                scenario.installedVersion,
                scenario.mcVersion,
                scenario.channel,
                versions
            )
            assertNotNull(latest)
            assertEquals(scenario.expectedVersion, latest.version)
        }
    }

    private data class Scenario(
        val mod: String,
        val expectedVersion: String,
        val installedVersion: String,
        val channel: Config.UpdateChannel,
        val mcVersion: String
    )
}