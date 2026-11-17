plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.opends.chat.core"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test>().configureEach {
    project.findProperty("opendsRealApi")?.let { systemProperty("opends.realApi", it.toString()) }
    project.findProperty("opendsTestKeyFile")?.let { systemProperty("opends.test.key.file", it.toString()) }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.core)
    implementation(libs.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.okio)
    implementation(libs.commons.compress)
    implementation(libs.datastore.preferences)
    implementation(libs.gson)
    testImplementation(libs.junit)
    testImplementation("org.xerial:sqlite-jdbc:3.46.1.3")
}
