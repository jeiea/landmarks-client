import org.jetbrains.kotlin.gradle.tasks.*

val kotlinVersion = rootProject.extra.properties["kotlinVersion"] ?: "1.3.60-eap-25"
val ktorVersion = rootProject.extra.properties["ktorVersion"] ?: "1.3.0-beta-1"
val spekVersion = "2.0.8"

buildscript {
  repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
  }
}

plugins {
  id("idea")
  id("org.jetbrains.kotlin.jvm")
  id("com.github.ben-manes.versions")
  id("com.github.johnrengelman.shadow") version "5.1.0"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
}

repositories {
  jcenter()
  maven(url = "https://dl.bintray.com/kotlin/ktor/")
  maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
}

dependencies {
  // Kotlin standard library
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
  implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
  implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
  implementation("io.ktor:ktor-client-gson:$ktorVersion")
  implementation("com.squareup.okhttp3:okhttp:4.2.2")

  // JUnit 5 test framework
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.5.2")
  testImplementation("org.junit.platform:junit-platform-runner:1.5.2")

  // Spek, the kotlin test framework
  testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
  // spek requires kotlin-reflect, can be omitted if already in the classpath
  testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

  // Assertion framework
  testImplementation("org.amshove.kluent:kluent:1.56")
}

tasks.test {
  useJUnitPlatform {
    includeEngines("spek2")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

val compileTestKotlin by tasks.getting(KotlinCompile::class) {
  kotlinOptions.jvmTarget = "1.8"
}
