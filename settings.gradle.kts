pluginManagement {
    repositories {
        google{
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.repsy.io/mvn/chrynan/public")
        maven(url = "https://jitpack.io")

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://repo.repsy.io/mvn/chrynan/public")
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "TabsLite"
include(":app")
