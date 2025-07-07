// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) version "8.6.0" apply false
    alias(libs.plugins.jetbrains.kotlin.android) version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
