import org.jetbrains.kotlin.config.*

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("android.extensions")
  kotlin("kapt")
}

android {
  compileSdkVersion(29)
  defaultConfig {
    applicationId = "kr.ac.kw.coms.globealbum"
    minSdkVersion(21)
    targetSdkVersion(29)
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
//  buildFeatures {
//    // Enables Jetpack Compose for this module
//    compose = true
//  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
  packagingOptions {
    exclude("META-INF/DEPENDENCIES")
    exclude("META-INF/LICENSE")
    exclude("META-INF/LICENSE.txt")
    exclude("META-INF/license.txt")
    exclude("META-INF/NOTICE")
    exclude("META-INF/NOTICE.txt")
    exclude("META-INF/notice.txt")
    exclude("META-INF/ASL2.0")
    exclude("META-INF/*.kotlin_module")
//    exclude("**/module-info.class")
    exclude("migrateToAndroidx/migration.xml")
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

repositories {
  jcenter()
  flatDir {
    dirs("libs")
  }
  maven(url = "https://kotlin.bintray.com/ktor")
  maven(url = "https://kotlin.bintray.com/kotlinx")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
}

dependencies {
  val kotlinVersion = rootProject.extra["kotlinVersion"] ?: KotlinCompilerVersion.VERSION
  val coroutineVersion: String by rootProject.extra
  val ankoVersion: String by rootProject.extra

  // client module
  implementation(project(":landmarks-clientkt"))

  // kotlin client custom dependencies
  implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

  // android UI libraries
  implementation("org.jetbrains.anko:anko:$ankoVersion")
  implementation("org.jetbrains.anko:anko-constraint-layout:$ankoVersion")

  // jetpack
//  implementation("androidx.ui:ui-tooling:0.1.0-dev02")
//  implementation("androidx.ui:ui-layout:0.1.0-dev02")
//  implementation("androidx.ui:ui-material:0.1.0-dev02")
  // jetpack supplement
  implementation("androidx.legacy:legacy-support-v4:1.0.0")
  implementation("androidx.appcompat:appcompat:1.1.0")
  implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta3")
  implementation("androidx.recyclerview:recyclerview:1.1.0-rc01")


  implementation("com.caverock:androidsvg:1.4")
  implementation("com.google.android:flexbox:1.1.1")


  // Map service libraries
  implementation("org.osmdroid:osmdroid-android:6.1.2")
  implementation("com.drewnoakes:metadata-extractor:2.12.0")

  // glide library for imageview
  implementation("com.github.bumptech.glide:glide:4.10.0") {
    exclude(group = "com.android.support")
  }
  kapt("androidx.lifecycle:lifecycle-compiler:2.1.0")
  // glide androix support issue: https://github.com/bumptech/glide/issues/3080
  kapt("com.android.support:support-annotations:28.0.0")
  kapt("com.github.bumptech.glide:compiler:4.10.0")

  // kotlin JSON
  implementation("com.beust:klaxon:5.2")

  // Diary animation
  implementation("at.wirecube:additive_animations:1.7.4")

  // Test libraries

  // JUnit 5 -> 4 test framework
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.5.2")
  testCompileOnly("org.junit.platform:junit-platform-runner:1.5.2")
  // For java junit4 compatible test discovery
  testCompileOnly("org.junit.vintage:junit-vintage-engine:5.5.2")
  // Spek, the kotlin test framework, with kotlin version replacement.
  testCompileOnly("org.jetbrains.spek:spek-api:1.2.1") {
    exclude(group = "org.jetbrains.kotlin")
  }
  testImplementation("org.jetbrains.spek:spek-junit-platform-engine:1.2.1") {
    exclude(group = "org.junit.platform")
    exclude(group = "org.jetbrains.kotlin")
  }
  // Assertion framework
  testImplementation("org.amshove.kluent:kluent:1.56")
  // Android test
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
