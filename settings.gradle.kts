pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Google's mirror of Maven Central first: repo.maven.apache.org rate-limits (HTTP 429)
        // shared CI/cloud hosts, and Gradle treats that as fatal instead of falling through.
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // Google's mirror of Maven Central first: repo.maven.apache.org rate-limits (HTTP 429)
        // shared CI/cloud hosts, and Gradle treats that as fatal instead of falling through.
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
        mavenCentral()
        // usb-serial-for-android and AzNavRail are published via JitPack
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MagNom"
include(":app")
