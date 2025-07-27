plugins {
    java
    `maven-publish`
}

group = "de.obey.crown.core"
version = "1.0.0"
val targetJavaVersion = 17

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
            from(components["java"])
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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.zaxxer:HikariCP:6.3.2")
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.h2database:h2:2.3.232")
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)
}




