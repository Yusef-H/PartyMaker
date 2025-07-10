// settings.gradle.kts – Project structure and repository management

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Fail if a subproject tries to declare its own repositories
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PartyMaker"

// Include Android app module
include(":app")

// Exclude the server project from Android builds; it is built independently as a Spring Boot project
// If you want to include the server as a Gradle subproject, uncomment the following line:
// include(":server")
 