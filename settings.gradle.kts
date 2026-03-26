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
    id("io.github.diskria.projektor.settings") version "5.+"
}

projekt {
    version = "1.2.2"
    license = MIT
    publish = setOf(
        GITHUB_PAGES,
    )

    gradlePlugin()
}
