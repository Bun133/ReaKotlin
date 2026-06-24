plugins {
    kotlin("jvm") version "2.4.0"
}

sourceSets.main {
    kotlin.srcDirs("src/main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
}