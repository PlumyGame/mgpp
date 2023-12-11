@file:Suppress("RemoveRedundantBackticks")

package io.github.liplum.mindustry

import io.github.liplum.dsl.*
import io.github.liplum.mindustry.LocalProperties.local
import io.github.liplum.mindustry.LocalProperties.localProperties
import io.github.liplum.mindustry.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import java.io.File

typealias Mgpp = MindustryPlugin

class MindustryPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        LocalProperties.clearCache(this)
        val ex = target.extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val assets = extensions.getOrCreate<MindustryAssetsExtension>(
            Mgpp.AssetExtensionName
        )
        /**
         * Handle [InheritFromParent].
         * Because they're initialized at the [Plugin.apply] phase, the user-code will overwrite them if it's possible.
         */
        target.parent?.let {
            it.plugins.whenHas<MindustryPlugin> {
                val parentEx = it.extensions.getOrCreate<MindustryExtension>(Mgpp.MainExtensionName)
                ex._isLib.set(parentEx._isLib)
                ex._dependency.mindustryDependency.set(parentEx._dependency.mindustryDependency)
                ex._dependency.arcDependency.set(parentEx._dependency.arcDependency)
                ex._client.location.set(parentEx._client.location)
                ex._client.keepOtherVersion.set(parentEx._client.keepOtherVersion)
                ex._client.startupArgs.set(parentEx._client.startupArgs)
                ex._server.location.set(parentEx._server.location)
                ex._server.keepOtherVersion.set(parentEx._server.keepOtherVersion)
                ex._server.startupArgs.set(parentEx._server.startupArgs)
                ex._run._dataDir.set(parentEx._run._dataDir)
                ex._run._forciblyClear.set(parentEx._run._forciblyClear)
                ex._deploy._androidSdkRoot.set(parentEx._deploy._androidSdkRoot)
                ex._deploy.enableFatJar.set(parentEx._deploy.enableFatJar)
            }
        }
        // Register this for dynamically configure tasks without class reference in groovy.
        // Eagerly configure this task in order to be added into task group in IDE
        tasks.register<AntiAlias>("antiAlias") {
            group = Mgpp.MindustryTaskGroup
        }.get()
        tasks.register<ModHjsonGenerate>("genModHjson") {
            group = Mgpp.MindustryTaskGroup
            modMeta.set(ex._modMeta)
            outputHjson.set(temporaryDir.resolve("mod.hjson"))
        }
        plugins.apply<MindustryAppPlugin>()
        plugins.whenHas<JavaPlugin> {
            plugins.apply<MindustryAssetPlugin>()
            plugins.apply<MindustryJavaPlugin>()
        }
        GroovyBridge.attach(target)
    }

    companion object {
        /**
         * The default check time(ms) for latest version.
         *
         * 1 hour as default.
         */
        const val defaultOutOfDataTime = 1000L * 60 * 60
        /**
         * The check time(ms) for latest version.
         *
         * 1 hour as default.
         */
        var outOfDataTime = defaultOutOfDataTime
        /**
         * A task group for main tasks, named `mindustry`
         */
        const val MindustryTaskGroup = "mindustry"
        /**
         * A task group for tasks related to [MindustryAssetsExtension], named `mindustry assets`
         */
        const val MindustryAssetTaskGroup = "mindustry assets"
        /**
         * The name of [MindustryExtension]
         */
        const val MainExtensionName = "mindustry"
        /**
         * The name of [MindustryAssetsExtension]
         */
        const val AssetExtensionName = "mindustryAssets"
        /**
         * The environment variable, as a folder, for Mindustry client to store data
         */
        const val MindustryDataDirEnv = "MINDUSTRY_DATA_DIR"
        /**
         * The default minGameVersion in `mod.(h)json`.
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMinGameVersion = "141.3"
        /**
         * [The default Mindustry version](https://github.com/Anuken/Mindustry/releases/tag/v141.3)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMindustryVersion = "v141.3"
        /**
         * [The default bleeding edge version](https://github.com/Anuken/MindustryBuilds/releases/tag/23770)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultMindustryBEVersion = "23770"
        /**
         * [The default Arc version](https://github.com/Anuken/Arc/releases/tag/v141.3)
         *
         * **Note:** You shouldn't pretend this version and work based on it.
         */
        const val DefaultArcVersion = "v141.3"
        /**
         * [Mindustry official release](https://github.com/Anuken/Mindustry/releases)
         */
        const val MindustryOfficialReleaseURL = "https://github.com/Anuken/Mindustry/releases"
        /**
         * GitHub API of [Mindustry official release](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val APIMindustryOfficialReleaseURL = "https://api.github.com/repos/Anuken/Mindustry/releases"
        /**
         * GitHub API of [Latest Mindustry official release](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val APIMindustryOfficialLatestReleaseURL = "https://api.github.com/repos/Anuken/Mindustry/releases/latest"
        /**
         * GitHub API of [Mindustry bleeding-edge release](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val APIMindustryBEReleaseURL = "https://api.github.com/repos/Anuken/MindustryBuilds/releases/latest"
        /**
         * GitHub API of [Latest Mindustry bleeding-edge release](https://api.github.com/repos/Anuken/Mindustry/releases/latest)
         */
        const val APIMindustryBELatestReleaseURL = "https://api.github.com/repos/Anuken/MindustryBuilds/releases/latest"
        /**
         * [Arc tags](https://github.com/Anuken/Arc/tags)
         */
        const val ArcTagURL = "https://api.github.com/repos/Anuken/arc/tags"
        /**
         * [An *Anime* cat](https://github.com/Anuken)
         */
        const val Anuken = "anuken"
        /**
         * [Mindustry game](https://github.com/Anuken/Mindustry)
         */
        const val Mindustry = "mindustry"
        /**
         * [Mindustry bleeding-edge](https://github.com/Anuken/MindustryBuilds)
         */
        const val MindustryBuilds = "MindustryBuilds"
        /**
         * [The name convention of client release](https://github.com/Anuken/Mindustry/releases)
         */
        const val ClientReleaseName = "Mindustry.jar"
        /**
         * [The name convention of server release](https://github.com/Anuken/Mindustry/releases)
         */
        const val ServerReleaseName = "server-release.jar"
        /**
         * [The Mindustry repo on Jitpack](https://github.com/anuken/mindustry)
         */
        const val MindustryJitpackRepo = "com.github.anuken.mindustry"
        /**
         * [The mirror repo of Mindustry on Jitpack](https://github.com/anuken/mindustryjitpack)
         */
        const val MindustryJitpackMirrorRepo = "com.github.anuken.mindustryjitpack"
        /**
         * [The GitHub API to fetch the latest commit of mirror](https://github.com/Anuken/MindustryJitpack/commits/main)
         */
        const val MindustryJitpackLatestCommit = "https://api.github.com/repos/Anuken/MindustryJitpack/commits/main"
        /**
         * [The GitHub API to fetch the latest commit of arc](https://github.com/Anuken/Arc/commits/master)
         */
        const val ArcLatestCommit = "https://api.github.com/repos/Anuken/Arc/commits/master"
        /**
         * [The Arc repo on Jitpack](https://github.com/anuken/arc)
         */
        const val ArcJitpackRepo = "com.github.anuken.arc"
        /**
         * The main class of desktop launcher.
         */
        const val MindustryDesktopMainClass = "mindustry.desktop.DesktopLauncher"
        /**
         * The main class of server launcher.
         */
        const val MindustrySeverMainClass = "mindustry.server.ServerLauncher"
        /**
         * An empty folder for null-check
         */
        @JvmStatic
        val DefaultEmptyFile = File("")
        /**
         * The [organization](https://github.com/mindustry-antigrief) of Foo's Client
         */
        const val AntiGrief = "mindustry-antigrief"
        /**
         * The [Foo's Client repo](https://github.com/mindustry-antigrief/mindustry-client)
         */
        const val FooClient = "mindustry-client"
    }
}
/**
 * Provides the existing `antiAlias`: [AntiAlias] task.
 */
val TaskContainer.`antiAlias`: TaskProvider<AntiAlias>
    get() = named<AntiAlias>("antiAlias")
/**
 * Provides the existing `genModHjson`: [ModHjsonGenerate] task.
 */
val TaskContainer.`genModHjson`: TaskProvider<ModHjsonGenerate>
    get() = named<ModHjsonGenerate>("genModHjson")

fun String?.addAngleBracketsIfNeed(): String? =
    if (this == null) null
    else if (startsWith("<") && endsWith(">")) this
    else "<$this>"
/**
 * For downloading and running game.
 */
class MindustryAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ex = target.extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val resolveMods = target.tasks.register<ResolveMods>(
            "resolveMods"
        ) {
            group = Mgpp.MindustryTaskGroup
            mods.set(ex._mods.worksWith)
        }
        target.tasks.register<CleanMindustrySharedCache>("cleanMindustrySharedCache") {
            group = BasePlugin.BUILD_GROUP
        }
        target.afterEvaluateThis {
            // For client side
            val downloadClient = target.tasks.register<DownloadGame>(
                "downloadClient",
            ) {
                group = Mgpp.MindustryTaskGroup
                keepOthers.set(ex._client.keepOtherVersion)
                val localOverwrite = project.local["mgpp.client.location"]
                if (localOverwrite != null)
                    location.set(LocalGameLoc(localOverwrite))
                else location.set(ex._client.location)
            }
            // For server side
            val downloadServer = target.tasks.register<DownloadGame>(
                "downloadServer",
            ) {
                group = Mgpp.MindustryTaskGroup
                keepOthers.set(ex._client.keepOtherVersion)
                val localOverwrite = project.local["mgpp.server.location"]
                if (localOverwrite != null)
                    location.set(LocalGameLoc(localOverwrite))
                else location.set(ex._server.location)
            }
            val runClient = tasks.register<RunMindustry>("runClient") {
                group = Mgpp.MindustryTaskGroup
                dependsOn(downloadClient)
                mainClass.convention(Mgpp.MindustryDesktopMainClass)
                val doForciblyClear = project.localProperties.getProperty("mgpp.run.forciblyClear")?.let {
                    it != "false"
                } ?: ex._run._forciblyClear.get()
                forciblyClear.set(doForciblyClear)
                val resolvedDataDir = when (val dataDirConfig =
                    project.localProperties.getProperty("mgpp.run.dataDir").addAngleBracketsIfNeed()
                        ?: ex._run._dataDir.get()
                ) {
                    "<default>" -> resolveDefaultDataDir()
                    "<temp>" -> temporaryDir.resolve("data")
                    "<env>" -> System.getenv(Mgpp.MindustryDataDirEnv).let {
                        if (it == null) temporaryDir.resolve("data")
                        else File(it).run {
                            if (isFile) this
                            else temporaryDir.resolve("data")
                        }
                    }

                    else -> File(dataDirConfig) // customized data directory
                }

                logger.info("Data directory of $name is $resolvedDataDir .")
                resolvedDataDir.getOrCreateDir()
                dataDir.set(resolvedDataDir)
                mindustryFile.from(downloadClient)
                modsWorkWith.from(resolveMods)
                dataModsPath.set("mods")
                startupArgs.set(ex._client.startupArgs)
                ex._mods._extraModsFromTask.get().forEach {
                    outputtedMods.from(tasks.getByPath(it))
                }
            }
            val runServer = tasks.register<RunMindustry>(
                "runServer",
            ) {
                group = Mgpp.MindustryTaskGroup
                dependsOn(downloadServer)
                val doForciblyClear = project.localProperties.getProperty("mgpp.run.forciblyClear")?.let {
                    it != "false"
                } ?: ex._run._forciblyClear.get()
                forciblyClear.set(doForciblyClear)
                mainClass.convention(Mgpp.MindustrySeverMainClass)
                mindustryFile.from(downloadServer)
                modsWorkWith.from(resolveMods)
                dataModsPath.convention("config/mods")
                startupArgs.set(ex._server.startupArgs)
                ex._mods._extraModsFromTask.get().forEach {
                    dependsOn(tasks.getByPath(it))
                    outputtedMods.from(tasks.getByPath(it))
                }
            }
        }
    }
}
/**
 * Provides the existing `downloadClient`: [DownloadGame] task.
 *
 * Because it's registerd after project evaluating, please access it in [Project.afterEvaluate].
 */
val TaskContainer.`downloadClient`: TaskProvider<DownloadGame>
    get() = named<DownloadGame>("downloadClient")

/**
 * Provides the existing `downloadServer`: [RunMindustry] task.
 *
 * Because it's registerd after project evaluating, please access it in [Project.afterEvaluate].
 */
val TaskContainer.`downloadServer`: TaskProvider<DownloadGame>
    get() = named<DownloadGame>("downloadServer")

/**
 * Provides the existing `runClient`: [RunMindustry] task.
 *
 * Because it's registerd after project evaluating, please access it in [Project.afterEvaluate].
 */
val TaskContainer.`runClient`: TaskProvider<RunMindustry>
    get() = named<RunMindustry>("runClient")

/**
 * Provides the existing `runServer`: [RunMindustry] task.
 *
 * Because it's registerd after project evaluating, please access it in [Project.afterEvaluate].
 */
val TaskContainer.`runServer`: TaskProvider<RunMindustry>
    get() = named<RunMindustry>("runServer")

/**
 * For deployment.
 */
@DisableIfWithout("java")
class MindustryJavaPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        val ex = extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val jar = tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME)
        @DisableIfWithout("java")
        val dexJar = tasks.register<DexJar>("dexJar") {
            dependsOn("jar")
            group = Mgpp.MindustryTaskGroup
            dependsOn(JavaPlugin.JAR_TASK_NAME)
            classpath.from(
                configurations.compileClasspath,
                configurations.runtimeClasspath
            )
            jarFiles.from(jar)
            sdkRoot.set(ex._deploy._androidSdkRoot)
        }
        val deploy = tasks.register<Jar>("deploy") {
            group = Mgpp.MindustryTaskGroup
            dependsOn(jar)
            dependsOn(dexJar)
            destinationDirectory.set(temporaryDir)
            archiveBaseName.set(ex._deploy._baseName)
            archiveVersion.set(ex._deploy._version)
            archiveClassifier.set(ex._deploy._classifier)
        }
        target.afterEvaluateThis {
            deploy.configure { deploy ->
                deploy.from(
                    *jar.get().outputs.files.map { project.zipTree(it) }.toTypedArray(),
                    *dexJar.get().outputs.files.map { project.zipTree(it) }.toTypedArray(),
                )
            }
            if (ex._deploy.enableFatJar.get()) {
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    from(
                        configurations.runtimeClasspath.get().map {
                            if (it.isDirectory) it else zipTree(it)
                        }
                    )
                }
            }
        }
        // Set the convention to ex._deploy
        ex._deploy._baseName.convention(provider {
            ex._modMeta.get().name
        })
        ex._deploy._version.convention(provider {
            ex._modMeta.get().version
        })
    }
}
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`dexJar`: TaskProvider<DexJar>
    get() = named<DexJar>("dexJar")
/**
 * Provides the existing [compileGroovy][org.gradle.api.tasks.compile.GroovyCompile] task.
 */
val TaskContainer.`deploy`: TaskProvider<Jar>
    get() = named<Jar>("deploy")
/**
 * For generating resource class.
 */
@DisableIfWithout("java")
class MindustryAssetPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.func {
        val main = extensions.getOrCreate<MindustryExtension>(
            Mgpp.MainExtensionName
        )
        val assets = extensions.getOrCreate<MindustryAssetsExtension>(
            Mgpp.AssetExtensionName
        )
        // Doesn't register the tasks if no resource needs to generate its class.
        @DisableIfWithout("java")
        val genResourceClass by lazy {
            tasks.register<GenerateRClass>("genResourceClass") {
                this.group = Mgpp.MindustryAssetTaskGroup
                val name = assets.qualifiedName.get()
                if (name == "default") {
                    val modMeta = main._modMeta.get()
                    val (packageName, _) = modMeta.main.packageAndClassName()
                    qualifiedName.set("$packageName.R")
                } else {
                    qualifiedName.set(name)
                }
            }
        }
        target.afterEvaluateThis {
            val assetsRoot = assets.assetsRoot.get()
            if (assetsRoot != Mgpp.DefaultEmptyFile) {
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    from(assetsRoot)
                }
            }
            if (!main.isLib) {
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    from(assets._icon)
                }
                tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
                    dependsOn("genModHjson")
                    from(tasks.getByPath("genModHjson"))
                }
            }
            // Resolve all batches
            val group2Batches = assets.batches.get().resolveBatches()
            var genResourceClassCounter = 0
            val jar = tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME)
            for ((type, batches) in group2Batches) {
                if (batches.isEmpty()) continue
                jar.configure {
                    batches.forEach { batch ->
                        val dir = batch.dir
                        val root = batch.root
                        if (root == Mgpp.DefaultEmptyFile) {
                            val dirParent = dir.parentFile
                            if (dirParent != null) {
                                it.from(dirParent) {
                                    it.include("${dir.name}/**")
                                }
                            } else {
                                it.include("${dir.name}/**")
                            }
                        } else { // relative path
                            it.from(root) {
                                it.include("$dir/**")
                            }
                        }
                    }
                }
                if (!batches.any { it.enableGenClass }) continue
                val groupPascal = type.group.lowercase().capitalized()
                @DisableIfWithout("java")
                val gen = tasks.register<GenerateResourceClass>("gen${groupPascal}Class") {
                    this.group = Mgpp.MindustryAssetTaskGroup
                    dependsOn(batches.flatMap { it.dependsOn }.distinct().toTypedArray())
                    args.put("ModName", main._modMeta.get().name)
                    args.put("ResourceNameRule", type.nameRule.name)
                    args.putAll(assets.args)
                    args.putAll(type.args)
                    generator.set(type.generator)
                    className.set(type.className)
                    resources.from(batches.filter { it.enableGenClass }.map { it.dir })
                }
                genResourceClass.get().apply {
                    dependsOn(gen)
                    classFiles.from(gen)
                }
                genResourceClassCounter++
            }
            if (genResourceClassCounter > 0) {
                safeRun {
                    tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
                        it.dependsOn(genResourceClass)
                    }
                }
                safeRun {
                    tasks.named("compileKotlin") {
                        it.dependsOn(genResourceClass)
                    }
                }
                safeRun {
                    tasks.named("compileGroovy") {
                        it.dependsOn(genResourceClass)
                    }
                }
            }
        }
    }
}

inline fun safeRun(func: () -> Unit) {
    try {
        func()
    } catch (_: Throwable) {
    }
}
/**
 * Provides the existing [resolveMods][ResolveMods] task.
 */
val TaskContainer.`resolveMods`: TaskProvider<ResolveMods>
    get() = named<ResolveMods>("resolveMods")

fun Project.resolveDefaultDataDir(): File {
    return when (getOs()) {
        OS.Unknown -> {
            logger.warn("Can't recognize your operation system.")
            Mgpp.DefaultEmptyFile
        }

        OS.Windows -> FileAt(System.getenv("AppData"), "Mindustry")
        OS.Linux -> FileAt(System.getenv("XDG_DATA_HOME") ?: System.getenv("HOME"), ".local", "share", "Mindustry")
        OS.Mac -> FileAt(System.getenv("HOME"), "Library", "Application Support", "Mindustry")
    }
}