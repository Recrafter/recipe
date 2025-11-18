import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.GITHUB_PAGES

pluginManagement {
    repositories {
        maven("https://diskria.github.io/projektor") {
            name = "Projektor"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "4.+"
}

projekt {
    version = "0.1.4"
    license = MIT
    publish = setOf(
        GITHUB_PAGES,
    )

    gradlePlugin()
}
