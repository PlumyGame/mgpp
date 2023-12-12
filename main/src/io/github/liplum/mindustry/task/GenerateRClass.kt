package io.github.liplum.mindustry.task

import io.github.liplum.dsl.*
import io.github.liplum.mindustry.Mgpp
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

open class GenerateRClass : DefaultTask() {
    val classFiles = project.configurationFileCollection()
        @InputFiles @SkipWhenEmpty @IgnoreEmptyDirectories get
    val qualifiedName = project.stringProp()
        @Input get
    val generated = project.fileProp()
        @OutputFile get

    init {
        generated.convention(project.provider {
            qualifiedName.get().qualified2FileName(
                project.layout.buildDirectory.asFile.get().resolve("generated").resolve(Mgpp.Mindustry)
            )
        })
    }
    @TaskAction
    fun generate() {
        val qualified = qualifiedName.get()
        val (packageName, className) = qualified.packageAndClassName()
        generated.get().ensure().outputStream().use { file ->
            file += "package $packageName;\n"
            file += "public final class $className {\n"
            classFiles.files.forEach {
                file += it.readBytes()
            }
            file += "}\n"
        }
    }
}