# CHF-2 · Set up Gradle version catalog and core dependencies

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-1 Foundation & Setup |
| Blocked By | — |
| Blocks | CHF-3, CHF-4, CHF-5, CHF-8, CHF-9, CHF-10, CHF-11, CHF-12, CHF-18, CHF-19, CHF-23, CHF-52 |

---

## Goal
Create a Gradle version catalog with pinned versions of every dependency Cha Fund will need, wire it into module `build.gradle.kts`, and enable Java 8+ desugaring so `java.time` works on min SDK 24.

---

## Files to add / modify

| Path | Action |
|---|---|
| `gradle/libs.versions.toml` | **Create** |
| `build.gradle.kts` (root) | Modify — add KSP, kotlinx.serialization plugin aliases |
| `app/build.gradle.kts` | Modify — apply plugins, dependencies via `libs.*`, enable desugaring |
| `settings.gradle.kts` | Modify if needed for plugin management |

---

## Implementation

### `libs.versions.toml`
```toml
[versions]
agp = "8.5.0"
kotlin = "1.9.24"
ksp = "1.9.24-1.0.20"
composeBom = "2024.06.00"
coreKtx = "1.13.1"
lifecycle = "2.8.2"
activityCompose = "1.9.0"
navigationCompose = "2.8.0-beta05"
room = "2.6.1"
datastore = "1.1.1"
koin = "3.5.6"
koinCompose = "3.5.6"
coroutines = "1.8.1"
timber = "5.0.1"
kotlinxSerialization = "1.7.0"
desugar = "2.0.4"
junit = "4.13.2"
mockk = "1.13.11"
turbine = "1.1.0"
composeTest = "1.6.8"

[libraries]
androidx-core-ktx           = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime  = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-compose  = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-process  = { group = "androidx.lifecycle", name = "lifecycle-process", version.ref = "lifecycle" }
androidx-activity-compose   = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom        = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui         = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3  = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-tooling    = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
room-runtime  = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx      = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing  = { group = "androidx.room", name = "room-testing", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
koin-android  = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-compose  = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koinCompose" }
kotlinx-coroutines        = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test   = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
timber  = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
desugar = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "desugar" }
junit   = { group = "junit", name = "junit", version.ref = "junit" }
mockk   = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
androidx-compose-ui-test     = { group = "androidx.compose.ui", name = "ui-test-junit4", version.ref = "composeTest" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest", version.ref = "composeTest" }

[plugins]
android-application       = { id = "com.android.application", version.ref = "agp" }
kotlin-android            = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose            = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization      = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp                       = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### `app/build.gradle.kts` (key sections)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.chafund"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.chafund"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    ksp { arg("room.schemaLocation", "$projectDir/schemas") }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.tooling.preview)
    debugImplementation(libs.androidx.compose.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.koin.android); implementation(libs.koin.compose)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

---

## Acceptance Criteria
- [ ] `gradle/libs.versions.toml` exists and contains every entry listed above.
- [ ] Module `build.gradle.kts` references dependencies and plugins via `libs.*` (no hard-coded versions).
- [ ] KSP plugin applied; Room schema export configured at `$projectDir/schemas`.
- [ ] `coreLibraryDesugaringEnabled = true` set with `desugar_jdk_libs` dependency.
- [ ] `./gradlew --refresh-dependencies assembleDebug` succeeds on a clean machine.

---

## Testing
- Manual: `./gradlew help` prints task list without resolution errors.
- Manual: `./gradlew dependencies --configuration releaseRuntimeClasspath` lists Room, Koin, Compose.

---

## Notes
- Pin all versions in the catalog — never use `+` or `latest.release`.
- If team policy requires LTS Kotlin, lock to the matching Compose compiler version (the `kotlin-compose` plugin handles this automatically).
