pluginManagement {
    repositories {
        google() // بدون قيود content لضمان تحميل كل الـ plugins من Google
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
}

rootProject.name = "Harvest Money"
include(":app")
