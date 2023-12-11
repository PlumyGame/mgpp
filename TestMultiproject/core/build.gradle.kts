@file:Suppress("SpellCheckingInspection")

import io.github.liplum.mindustry.*

plugins {
    java
    id("io.github.liplum.mgpp")
}
repositories {
    mindustryRepo()
    mavenCentral()
}

dependencies {
    importMindustry()
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}
sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}

version = "1.0"
group = "net.liplum"

mindustry {
    meta += ModMeta(
        name = "core",
        displayName = "Core Mod",
        minGameVersion = "146",
        main = "plumy.test.CoreMod"
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
