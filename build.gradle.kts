plugins {
    idea
    kotlin("jvm") version "2.1.0"
    id("net.neoforged.gradle.userdev") version "7.0.171"
    id("com.gradleup.shadow") version "9.3.0"
}

val shadowImplementation by configurations.creating {
    configurations["implementation"].extendsFrom(this)
}

base {
    archivesName = "craftify-neoforge"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.teamresourceful.com/repository/maven-public")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.msrandom.net/repository/root")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    implementation(libs.neoforge)

    implementation(libs.kotlin.stdlib)

    runtimeOnly(libs.devauth)

    shadowImplementation(libs.vigilance) {
        isTransitive = false
    }
    shadowImplementation(libs.elementa) {
        isTransitive = false
    }
    shadowImplementation(libs.universalcraft) {
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains.kotlin")
        exclude("net.fabricmc")
    }
    implementation(libs.resourceful.lib) {
        "include"(this)
    }
    implementation(libs.olympus) {
        "include"(this)
    }

    implementation(libs.jukebox) {
        "include"(this)
        isTransitive = false
    }
    implementation(libs.ktor.cio) {
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.ktor.core) {
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains.kotlin")
        exclude("org.slf4j")
    }
    libs.bundles.ktor.get().forEach {
        "include"(provider { it })
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("dev")
    configurations = listOf(shadowImplementation)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    relocate("gg.essential", "tech.thatgravyboat.craftify.libs.essential")
    exclude("pack.mcmeta")
    exclude("META-INF/maven/**")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}
