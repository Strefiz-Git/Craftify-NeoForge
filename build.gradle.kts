plugins {
    java
    idea
    kotlin("jvm") version "1.9.22"
    id("net.neoforged.gradle.userdev") version "7.1.38"
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
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21"
    }
}
