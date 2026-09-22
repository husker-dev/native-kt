import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class BoltFFIJar @Inject constructor(
    private val execOps: ExecOperations,
): Jar() {

    @get:InputDirectory abstract val projectDir: DirectoryProperty

    private var buildDir: Provider<RegularFile> =
        project.layout.buildDirectory.file("boltffi")

    @get:OutputDirectory
    val sourcesDir: Provider<RegularFile> =
        project.layout.buildDirectory.file("boltffi/java/sources")

    @get:OutputDirectory
    val resourcesDir: Provider<RegularFile> =
        project.layout.buildDirectory.file("boltffi/java/native")

    @TaskAction
    fun action() {
        fun exec(
            dir: File,
            command: String
        ) {
            logger.info(command)
            execOps.exec {
                workingDir = dir
                if(Os.isFamily(Os.FAMILY_WINDOWS))
                    commandLine("cmd.exe", "/c", command)
                else
                    commandLine("/bin/bash", "-c", command)
            }
        }

        val buildDir = buildDir.get().asFile.resolve("java")
        buildDir.deleteRecursively()
        buildDir.mkdirs()

        exec(projectDir.get().asFile, "boltffi pack java --release")

        buildDir.resolve("com").copyRecursively(buildDir.resolve("sources/com"))
    }
}