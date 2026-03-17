import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.projektor)
}

dependencies {
    implementation(libs.foojay.resolver.plugin)
    implementation(libs.bundles.diskria.utils)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bedrock)
}

projekt {
    gradlePlugin {
        jvmTarget = JvmTarget.JVM_21
    }
}
