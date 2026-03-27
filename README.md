# Recipe

A Gradle settings plugin that preconfigures projects for the Recrafter ecosystem. New recipe unlocked!

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.recrafter.recipe.svg?label=Gradle+Plugin+Portal&style=for-the-badge)](https://plugins.gradle.org/plugin/io.github.recrafter.recipe) [![License: MIT](https://img.shields.io/static/v1?label=License&style=for-the-badge&message=MIT&color=yellow)](https://spdx.org/licenses/MIT)

---

## Overview

The Recipe plugin provides a unified way to configure Gradle settings and automatically prepare complex workspaces.  
It handles essential project bootstrapping tasks such as:

- Including mod projects automatically based on the detected structure
- Registering Maven repositories for supported mod loaders (Forge, Fabric, NeoForge, etc.)
- Preparing project metadata and ensuring compatibility between modules
- Integrating seamlessly with the Crafter build plugin and other Recrafter utilities

## Example Usage

```kotlin
plugins {
    id("io.github.recrafter.recipe") version "<version>"
}
```

Once applied, Recipe scans and configures your workspace automatically —
registering required repositories, including mod subprojects, and setting up consistent project metadata.

---

## License

This project is licensed under the [MIT License](https://spdx.org/licenses/MIT).
