import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.GITHUB_PAGES
import io.github.diskria.projektor.settings.configurators.common.dependencyRepositories

fun RepositoryHandler.resolveRecrafterRepoMaven(repoName: String) {
    val mavenName = repoName.replaceFirstChar { it.uppercaseChar() }
    val localMavens = rootDir.parentFile
        .resolve(repoName).resolve("build/maven").listFiles().orEmpty()
    if (localMavens.isNotEmpty()) {
        maven(uri(localMavens.first())) {
            name = "$mavenName Local"
        }
    } else {
        maven("https://recrafter.github.io/$repoName") {
            name = mavenName
        }
    }
}


pluginManagement {
    repositories {
        maven("https://diskria.github.io/projektor") {
            name = "Projektor"
        }
        gradlePluginPortal()
    }
}

dependencyRepositories {
    resolveRecrafterRepoMaven("bedrock")
}

plugins {
    id("io.github.diskria.projektor.settings") version "4.+"
}

projekt {
    version = "0.2.4"
    license = MIT
    publish = setOf(
        GITHUB_PAGES,
    )

    gradlePlugin()
}
