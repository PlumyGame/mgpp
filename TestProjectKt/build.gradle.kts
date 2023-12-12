import io.github.liplum.mindustry.*
import io.github.liplum.mindustry.task.AntiAlias
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    id("io.github.liplum.mgpp") version "1.3.2"
}

sourceSets {
    main {
        java.srcDirs("src")
        java.srcDirs("$buildDir/generated/mindustry")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        java.srcDirs("$buildDir/generated/mindustry")
        resources.srcDir("resources")
    }
}

group = "net.liplum"
version = "0.2"

repositories {
    mindustryRepo()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    importMindustry()
}

open class TestOutputTask : DefaultTask() {
    val outputFile = project.objects.property<File>()
        @OutputFile get
    @TaskAction
    fun test() {
    }
}
// Only for testing TaskMod(taskName)
tasks.register<TestOutputTask>("iconMaker") {
    outputFile.set(rootDir.resolve("icon.png"))
}
mindustry {
    dependency {
        mindustry on "v146"
        arc on "v146"
        // arc on latestRelease
    }
    client {
        mindustry official "v146"
        //mindustry be latest
        /*
        mindustry from GameLocation(
            user = "mindustry-antigrief",
            repo = "mindustry-client",
            version = "v8.0.0",
            release = "erekir-client.jar"
        )
        */
        //mindustry from localProperties
        // mindustry fromLocal "F:/Mindustry/Mindustry-BE-Desktop-22799.jar" named "22799.jar"
        // `clearUp` as default, it will delete other versions when download a new one
        // `keepOthers` will keep them
    }
    server {
        //mindustry be "22728"
        mindustry official "v146"
    }
    mods {
        worksWith {
            add kotlin "liplum/cyberio"
            add folder "$buildDir/mods"
            add folder "$buildDir/fakeMods"
            add hjson "BlueWolf3682/Exotic-Mod" branch "master"
            add fromTask "iconMaker"
            add localProperties "extraModPath"
        }
    }
    meta += ModMeta(
        name = "mgpp-kt",
        displayName = "MGPP Kotlin Test",
        main = "plumy.test.TestModKt",
        author = "Liplum"
    )
    meta["version"] = "Kotlin 666"
    meta.minGameVersion = "136"
    deploy {
        baseName = "KotlinMod"
        version = "666"
        // fatJar is default option unless you use another tool like shadowJar
        // fatJar
    }
    run {
        // useDefaultDataDir
    }
}
tasks.jar {
    dependsOn("iconMaker")
    archiveBaseName.set("TestRenamed")
}

tasks.dexJar {
    options.minApi = "26"
    options {
        minApi = "14"
    }
}
mindustryAssets {
    sprites {
        dir = rootDir.resolve("sprites")
        dependsOn("antiAlias")
    }
    sprites {
        dir = rootDir.resolve("sprites/data")
        rootAt(rootDir.resolve("sprites"))
        dependsOn("antiAlias")
        genClass
    }
    sounds {
        dir = rootDir.resolve("sounds")
        genClass
    }
}

tasks.named<AntiAlias>("antiAlias") {
    sourceDirectory.set(rootDir.resolve("sprites-raw"))
    destinationDirectory.set(rootDir.resolve("sprites"))
    addFilter {
        it.name != "sender.png"
    }
    //options.isIncremental = false
}

tasks.withType<Jar> {
    doLast {
        println("----------------jar----------------")
        files(this).forEach { println(it) }
        println("----------------jar----------------")
    }
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<JavaCompile>{
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}