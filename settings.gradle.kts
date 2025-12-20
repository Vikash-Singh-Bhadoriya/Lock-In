pluginManagement {
    repositories {
        google()

        // > Task :app:checkDebugAarMetadata FAILED
        //Execution failed for task ':app:checkDebugAarMetadata'.
        //> Could not resolve all files for configuration ':app:debugRuntimeClasspath'.
        //   > Could not find com.google.firebase:firebase-crashlytics-ktx:.
        //     Required by:
        //         project :app
        //   > Could not find com.google.firebase:firebase-analytics-ktx:.
        //     Required by:
        //         project :app
        //
        //Possible solution:
        // - Declare repository providing the artifact, see the documentation at https://docs.gradle.org/current/userguide/declaring_repositories.html

//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
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

rootProject.name = "Lock In"
include(":app")
 