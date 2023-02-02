package io.github.liplum.mindustry

import arc.util.serialization.Jval
import io.github.liplum.dsl.prop
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.net.URL

/**
 * You can configure the dependencies of Mindustry and Arc.
 * **NOTE:** remember to call [mindustryRepo] and [importMindustry] in your `build.gradle(.kts)`
 */
class DependencySpec(
    val target: Project,
) {
    /**
     * Import v135 as default for now.
     * DO NOT trust this behavior, it may change later.
     */
    @InheritFromParent
    @DefaultValue("on \"v135\"")
    val arcDependency = target.prop<IDependency>().apply {
        convention(ArcDependency())
    }
    /**
     * The dependency notation of Mindustry.
     *
     * Default: official "v135"
     */
    @InheritFromParent
    @DefaultValue("on \"v135\"")
    val mindustryDependency = target.prop<IDependency>().apply {
        convention(MindustryDependency())
    }
    //For Kotlin
    val arc = ArcSpec()
    //For Kotlin
    val mindustry = MindustrySpec()
    /**
     * A notation represents the latest version.
     * ## Usages
     * ```
     * mindustry be latest
     * ```
     * This is not recommended, it might not work if you faced the API limit of GitHub or
     * jitpack yet to build this version.
     */
    val latest: Notation
        get() = Notation.latest
    /**
     * A notation represents the latest release.
     * ## Usages
     * ```
     * mindustry on latestRelease
     * ```
     * **Potential Issue** It has a very small chance that it won't work when the new version was just released.
     */
    val latestRelease: Notation
        get() = Notation.latestRelease
    /**
     * A notation represents the latest tag.
     * ## Usages
     * ```
     * arc on latestTag
     * ```
     * **Potential Issue** It has a very small chance that it won't work when the new tag was just released.
     */
    val latestTag: Notation
        get() = Notation.latestRelease
    /**
     * Fetch the Arc from [arc jitpack](https://github.com/Anuken/Arc).
     */
    fun arc(version: String) {
        arcDependency.set(ArcDependency(version))
    }
    /**
     * Fetch the Mindustry from [mindustry jitpack](https://github.com/Anuken/Mindustry).
     */
    fun mindustry(version: String) {
        mindustryDependency.set(MindustryDependency(version))
    }
    /**
     * Fetch the Mindustry from [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
     */
    fun mindustryMirror(version: String) {
        mindustryDependency.set(MirrorDependency(version))
    }
    /**
     * Fetch the Arc from [arc jitpack](https://github.com/Anuken/Arc).
     * ## Supported notations:
     * - [latest]: set the [arcDependency] to the latest commit
     * - [latestTag]: set the [arcDependency] to the latest tag
     */
    fun arc(map: Map<String, Any>) {
        val version = map["version"]?.toString() ?: throw GradleException("No version specified for `arc`")
        when (version) {
            Notation.latest.toString() -> arcLatestCommit()
            Notation.latestRelease.toString() -> arcLatestTag()
            else -> arc(version)
        }
    }
    /**
     * Fetch the Mindustry from [mindustry jitpack](https://github.com/Anuken/Mindustry).
     * ## Supported notations:
     * - [latest]: set the [mindustryDependency] to the latest official release
     * - [latestRelease]: set the [mindustryDependency] to the latest official release
     */
    fun mindustry(map: Map<String, Any>) {
        val version = map["version"]?.toString() ?: throw GradleException("No version specified for `mindustry`")
        when (version) {
            Notation.latest.toString() -> mindustryLatestRelease()
            Notation.latestRelease.toString() -> mindustryLatestRelease()
            else -> mindustry(version)
        }
    }
    /**
     * Fetch the dependency of Mindustry from [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
     * ## Supported notations:
     * - [latest]: set the [mindustryDependency] to the latest jitpack mirror commit
     */
    fun mindustryMirror(map: Map<String, Any>) {
        val version = map["version"]?.toString() ?: throw GradleException("No version specified for `mindustryMirror`")
        when (version) {
            Notation.latest.toString() -> mindustryMirrorLatestCommit()
            else -> mindustryMirror(version)
        }
    }
    /**
     * Fetch the latest Mindustry from [mindustry jitpack](https://github.com/Anuken/Mindustry).
     *
     * **Potential Issue** It has a very small chance that it won't work when the new version was just released.
     */
    fun mindustryLatestRelease() {
        val latestVersion = target.fetchLatestVersion("mindustry-release-dependency") {
            try {
                val url = URL(Mgpp.APIMindustryOfficialLatestReleaseURL)
                val json = Jval.read(url.readText())
                return@fetchLatestVersion json.getString("tag_name")
            } catch (e: Exception) {
                target.logger.warn("Can't fetch the exact latest version of mindustry, so use ${R.version.defaultOfficial} instead")
                return@fetchLatestVersion R.version.defaultOfficial
            }
        }
        mindustry(latestVersion)
    }
    /**
     * Fetch the latest [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
     *
     * **Not Recommended** It may not work due to a network issue or jitpack not yet to build this version
     */
    fun mindustryMirrorLatestCommit() {
        val latestVersion = target.fetchLatestVersion("mindustry-mirror-commit-dependency") {
            try {
                val url = URL(Mgpp.MindustryJitpackLatestCommit)
                val json = Jval.read(url.readText())
                val fullSha = json.getString("sha")
                return@fetchLatestVersion fullSha.subSequence(0, 10).toString()
            } catch (e: Exception) {
                target.logger.warn("Can't fetch the exact latest version of mindustry jitpack, so use -SNAPSHOT instead")
                return@fetchLatestVersion "-SNAPSHOT"
            }
        }
        mindustryMirror(latestVersion)
    }
    /**
     * Fetch the latest Arc from [arc jitpack](https://github.com/Anuken/Arc).
     *
     * **Not Recommended** It may not work due to a network issue or jitpack not yet to build this version
     */
    fun arcLatestCommit() {
        val latestVersion = target.fetchLatestVersion("arc-commit-dependency") {
            try {
                val url = URL(Mgpp.ArcLatestCommit)
                val json = Jval.read(url.readText())
                val fullSha = json.getString("sha")
                return@fetchLatestVersion fullSha.subSequence(0, 10).toString()
            } catch (e: Exception) {
                target.logger.warn("Can't fetch the exact latest version of arc, so use -SNAPSHOT instead")
                return@fetchLatestVersion "-SNAPSHOT"
            }
        }
        arc(latestVersion)
    }
    /**
     * Fetch the latest Arc from [arc jitpack](https://github.com/Anuken/Arc).
     *
     * **Potential Issue** It has a very small chance that it won't work when the new version was just released.
     */
    fun arcLatestTag() {
        val latestVersion = target.fetchLatestVersion("arc-tag-dependency") {
            try {
                val url = URL(Mgpp.ArcTagURL)
                val json = Jval.read(url.readText())
                val all = json.asArray()
                val latestTag = all.get(0) // the latest tag
                return@fetchLatestVersion latestTag.getString("name")
            } catch (e: Exception) {
                target.logger.warn("Can't fetch the exact latest version of arc, so use ${R.version.defaultArc} instead")
                return@fetchLatestVersion R.version.defaultArc
            }
        }
        arc(latestVersion)
    }

    val ArcRepo = Mgpp.ArcJitpackRepo
    val MindustryMirrorRepo = Mgpp.MindustryJitpackMirrorRepo
    val MindustryRepo = Mgpp.MindustryJitpackRepo
    /**
     * Declare an Arc dependency from [arc jitpack](https://github.com/Anuken/Arc).
     */
    fun ArcDependency(
        version: String = R.version.defaultOfficial,
    ) = Dependency(Mgpp.ArcJitpackRepo, version)
    /**
     * Declare a Mindustry dependency from [mindustry jitpack](https://github.com/Anuken/Mindustry).
     */
    fun MindustryDependency(
        version: String = R.version.defaultOfficial,
    ) = Dependency(Mgpp.MindustryJitpackRepo, version)
    /**
     * Declare a dependency.
     */
    fun Dependency(
        fullName: String = "",
        version: String = "",
    ) = io.github.liplum.mindustry.Dependency(fullName, version)
    /**
     * Declare a Mindustry dependency from [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
     */
    fun MirrorDependency(
        version: String = "",
    ) = MirrorJitpackDependency(Mgpp.MindustryJitpackMirrorRepo, version)
    /**
     * To configure Arc dependency
     */
    // For Kotlin
    inner class ArcSpec {
        /**
         * Fetch the Arc from [arc jitpack](https://github.com/Anuken/Arc).
         */
        infix fun on(version: String) {
            arcDependency.set(ArcDependency(version))
        }
        /**
         * Fetch the Arc from [arc jitpack](https://github.com/Anuken/Arc).
         * ## Supported notations:
         * - [latest]: set the [arcDependency] to the latest commit
         * - [latestTag]: set the [arcDependency] to the latest tag
         */
        infix fun on(notation: Notation) {
            when(notation) {
                Notation.latest -> arcLatestCommit()
                Notation.latestRelease -> arcLatestTag()
                else -> throw GradleException("Unknown dependency notation of arc $notation")
            }
        }
    }
    /**
     * To configure Mindustry dependency
     */
    // For Kotlin
    inner class MindustrySpec {
        /**
         * Fetch the dependency of Mindustry from [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
         */
        infix fun mirror(version: String) {
            mindustryDependency.set(MirrorDependency(version))
        }
        /**
         * Fetch the Mindustry from [mindustry jitpack](https://github.com/Anuken/Mindustry).
         */
        infix fun on(version: String) {
            mindustryDependency.set(MindustryDependency(version))
        }
        /**
         * Fetch the Mindustry from [mindustry jitpack](https://github.com/Anuken/Mindustry).
         * ## Supported notations:
         * - [latest]: set the [mindustryDependency] to the latest official release
         * - [latestRelease]: set the [mindustryDependency] to the latest official release
         */
        infix fun on(notation: Notation) {
            when (notation) {
                Notation.latest -> mindustryLatestRelease()
                Notation.latestRelease -> mindustryLatestRelease()
                else -> throw GradleException("Unknown dependency notation of mindustry $notation")
            }
        }
        /**
         * Fetch the Mindustry from [mindustry jitpack mirror](https://github.com/Anuken/MindustryJitpack).
         * ## Supported notations:
         * - [latest]: set the [mindustryDependency] to the latest jitpack mirror commit
         */
        infix fun mirror(notation: Notation) {
            when(notation) {
                Notation.latest -> mindustryMirrorLatestCommit()
                else -> throw GradleException("Unknown dependency notation of mindustry mirror $notation")
            }
        }
    }
}