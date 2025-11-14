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
