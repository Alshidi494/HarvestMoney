enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // لتفعيل دعم النسخ النوعية

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.google\\.dagger.*") // لإتاحة تحميل Hilt plugin
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml")) // استيراد ملف النسخ مرة واحدة فقط
        }
    }
}

rootProject.name = "Harvest_Money"
include(":app")
