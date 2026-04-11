import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.GsonBuilder
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.temporal.ChronoUnit

buildscript {
    dependencies {
        classpath("com.google.code.gson:gson:2.10")
    }
}

plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "net.kunmc.lab"
version = "1.0.0"

val mcVersion = "1.16.5"

data class PaperDependencyProps(val url: String, val javaVersion: Int)

val paperDepends = mapOf(
    "1.16.5" to PaperDependencyProps("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT", 8),
    "1.17" to PaperDependencyProps("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT", 17),
    "1.17.1" to PaperDependencyProps("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT", 17),
    "1.18" to PaperDependencyProps("io.papermc.paper:paper-api:1.18-R0.1-SNAPSHOT", 17),
    "1.18.1" to PaperDependencyProps("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT", 17),
    "1.18.2" to PaperDependencyProps("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT", 17),
    "1.19" to PaperDependencyProps("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT", 17),
    "1.19.1" to PaperDependencyProps("io.papermc.paper:paper-api:1.19.1-R0.1-SNAPSHOT", 17),
    "1.19.2" to PaperDependencyProps("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT", 17),
    "1.19.3" to PaperDependencyProps("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT", 17),
    "1.19.4" to PaperDependencyProps("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT", 17),
    "1.20" to PaperDependencyProps("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT", 21),
    "1.20.1" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT", 21),
    "1.20.2" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT", 21),
    "1.20.3" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.3-R0.1-SNAPSHOT", 21),
    "1.20.4" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT", 21),
    "1.20.5" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.5-R0.1-SNAPSHOT", 21),
    "1.20.6" to PaperDependencyProps("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT", 21),
    "1.21" to PaperDependencyProps("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT", 21),
    "1.21.1" to PaperDependencyProps("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT", 21),
    "1.21.3" to PaperDependencyProps("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT", 21),
    "1.21.4" to PaperDependencyProps("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT", 21),
)

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "enginehub-maven"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://jitpack.io") }
    flatDir { dirs("server/cache", "libs") }
}

dependencies {
    compileOnly(paperDepends[mcVersion]!!.url)

    //compileOnly("com.sk89q.worldedit:worldedit-bukkit:latest.release")
    //compileOnly("com.sk89q.worldedit:worldedit-core:latest.release")
    //compileOnly("com.comphenix.protocol:ProtocolLib:latest.release")
    //compileOnly(":patched_1.16.5")

    // https://jitpack.io/#Maru32768/CommandLib
    implementation("com.github.Maru32768.CommandLib:bukkit:0.17.2")
    // https://jitpack.io/#Maru32768/ConfigLib
    implementation("com.github.Maru32768.ConfigLib:bukkit:0.21.0")
}

val paperDepend = paperDepends[mcVersion]
if (paperDepends[mcVersion] == null) {
    throw IllegalArgumentException("paperDepends[$mcVersion] is not set")
}
val targetJavaVersion = paperDepends[mcVersion]!!.javaVersion
configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    doFirst {
        project.copy {
            from(".")
            into(layout.buildDirectory.dir("resources/main/"))
            include("LICENSE*")
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveFileName = "${rootProject.name}-${project.version}.jar"
    relocate("net.kunmc.lab.commandlib", "${project.group}.${project.name.lowercase()}.commandlib")
    relocate("net.kunmc.lab.configlib", "${project.group}.${project.name.lowercase()}.configlib")
}
tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf(
        "name" to rootProject.name,
        "version" to version,
        "MainClass" to getMainClassFQCN(projectDir.toPath())
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register("copyToServer") {
    group = "copy"
    mustRunAfter("build")

    doLast {
        project.copy {
            from(layout.buildDirectory.file("libs/${rootProject.name}-${project.version}.jar"))
            into("./server/plugins")
            rename { fileName ->
                fileName.replace("-${project.version}", "")
            }
        }
    }
}

tasks.register<Copy>("copyProtocolLibToServer") {
    group = "copy"
    val fileNamePattern = ".*ProtocolLib.*.jar".toRegex()

    val alreadyExists = File("server/plugins/").listFiles()?.any { it.name.matches(fileNamePattern) } ?: false
    if (!alreadyExists) {
        configurations.compileClasspath.get().files
            .firstOrNull { it.name.matches(fileNamePattern) }
            ?.let {
                from(it)
                into("server/plugins")
            }
    }
}

tasks.register("buildAndCopy") {
    group = "build"
    dependsOn("build", "copyToServer")
}

tasks.register("downloadServerJar") {
    val serverJarFile = File("${projectDir.toPath().toAbsolutePath()}/server/server.jar")
    if (serverJarFile.exists()) {
        return@register
    }

    val linksFile = Paths.get("./paper-version-links.json").toFile()
    if (!linksFile.exists() || Instant.ofEpochMilli(linksFile.lastModified())
            .isBefore(Instant.now().plus(1, ChronoUnit.DAYS))
    ) {
        val paperVersionLinksUrl =
            URI.create("https://raw.githubusercontent.com/liebki/MinecraftServerForkDownloads/refs/heads/main/paper_downloads.json")
                .toURL()
        linksFile.createNewFile()
        paperVersionLinksUrl.openStream().use { stream ->
            linksFile.writer().use { writer ->
                writer.write(String(stream.readAllBytes()))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val versionMap = linksFile.reader().use { reader ->
        GsonBuilder().create().fromJson(reader, Map::class.java) as Map<String, String>
    }
    val serverUrl = versionMap[mcVersion] ?: error("Server URL not found for MC_VERSION: $mcVersion")

    URI.create(serverUrl).toURL().openStream().use { stream ->
        Files.copy(stream, serverJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

tasks.register("generatePatchedJar") {
    group = "setup"
    dependsOn("downloadServerJar")

    val serverDir = "${projectDir.toPath().toAbsolutePath()}/server"
    val file = File("$serverDir/cache/patched_1.16.5.jar")
    if (file.exists()) {
        return@register
    }

    val p = Runtime.getRuntime()
        .exec(arrayOf("java", "-jar", "$serverDir/server.jar", "nogui"), arrayOf(), File(serverDir))
    p.waitFor()
    p.destroy()
}

tasks.register("copyDefaultServerProperties") {
    group = "setup"
    dependsOn("downloadServerJar")

    val serverDir = "${projectDir.toPath().toAbsolutePath()}/server"
    val dst = Path.of(serverDir, "server.properties")
    if (dst.toFile().exists()) {
        return@register
    }
    Files.copy(Path.of(serverDir, "server.default.properties"), dst)
}

fun getMainClassFQCN(projectPath: Path): String {
    val mainClassFile = Files.walk(projectPath)
        .filter { it.fileName.toString().endsWith(".java") }
        .filter { path -> Files.lines(path).anyMatch { str -> str.contains("extends JavaPlugin") } }
        .findFirst()
        .get()
    return mainClassFile.toString()
        .replace("\\", ".")
        .replace("/", ".")
        .replace(Regex(".*src.main.java.|.java$"), "")
}
