plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
}

group = "de.obey.crown.core"
version = "1.0.0"
val targetJavaVersion = 21
val localServerPath = "D:\\MINECRAFT LOCALHOST\\1.21.11"

val pluginYml = file("src/main/resources/plugin.yml")
val pluginVersion: String by lazy {
    val versionLine = pluginYml.readLines().find { it.trim().startsWith("version:") }
    versionLine?.split("version:")?.getOrNull(1)?.trim()
        ?: error("Could not find version in plugin.yml")
}

val pluginName: String by lazy {
    val versionLine = pluginYml.readLines().find { it.trim().startsWith("name:") }
    versionLine?.split("name:")?.getOrNull(1)?.trim()
        ?: error("Could not find name in plugin.yml")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = pluginName
            version = version

            artifact(tasks.shadowJar)
        }
    }

    repositories {
        mavenLocal()
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://repo.opencollab.dev/main/")
    maven ("https://maven.enginehub.org/repo/")
    maven("https://mvnrepository.com/artifact/javax.persistence/javax.persistence-api")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
    compileOnly ("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.h2database:h2:2.4.240")

    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("org.json:json:20251224")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-text-serializer-legacy:4.26.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation("javax.persistence:javax.persistence-api:2.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    toolchain.vendor.set(JvmVendorSpec.ADOPTIUM)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("org.bstats", "${project.group}.lib.bstats")
    relocate("org.json", "${project.group}.lib.json")
    relocate("com.j256", "${project.group}.lib.j256")
    relocate("javax.persistence", "${project.group}.lib.javax.persistence")
    relocate("com.zaxxer", "${project.group}.lib.zaxxer")
    relocate("okhttp3", "${project.group}.lib.okhttp3")
    relocate("okio", "${project.group}.lib.okio")
    relocate("kotlin", "${project.group}.lib.kotlin")
    relocate("org.intellij", "${project.group}.lib.intellij")
    relocate("org.jetbrains", "${project.group}.lib.jetbrains")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
    finalizedBy("deployToLocalServer")
}

tasks.register<Copy>("deployToLocalServer") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    into("$localServerPath//plugins")
}



