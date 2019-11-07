// Top-level build file where you can add configuration options common to all sub-projects/modules.

//val kotlinVersion by extra("1.3.60-eap-25")
extra["kotlinVersion"] = "1.3.60-eap-25"
extra["coroutineVersion"] = "1.3.2"
extra["ankoVersion"] = "0.10.8"

buildscript {
  repositories {
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.0.0-alpha01")
    classpath(kotlin("gradle-plugin", version = "1.3.60-eap-25"))

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.27.0"
}

allprojects {
  repositories {
    google()
    jcenter()
  }
}

task<Delete>("clean") {
  delete(rootProject.buildDir)
}
